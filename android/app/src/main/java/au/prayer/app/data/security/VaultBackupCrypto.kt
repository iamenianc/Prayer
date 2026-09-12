package au.prayer.app.data.security

import au.prayer.app.data.models.VaultBackupPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Thrown when an archive file does not conform to the expected Folio wire format or header.
 */
class InvalidBackupFormatException(message: String) : Exception(message)

/**
 * Thrown when decryption fails due to an incorrect passphrase or corrupted ciphertext.
 */
class InvalidBackupPasswordException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Platform-standard, zero-telemetry cryptographic engine for sealing and unsealing
 * personal prayer vaults.
 *
 * Wire Specification:
 * - Bytes 0..7:   Magic Header ASCII: "PWCVAULT" (8 bytes)
 * - Bytes 8..11:  Format Version Int32 (currently 1)
 * - Bytes 12..27: Random Salt (16 bytes)
 * - Bytes 28..39: Random GCM Nonce / IV (12 bytes)
 * - Bytes 40..N:  AES-256-GCM Ciphertext + 128-bit Authentication Tag
 */
object VaultBackupCrypto {

    private val MAGIC_HEADER = "PWCVAULT".toByteArray(StandardCharsets.US_ASCII)
    const val CURRENT_VERSION = 1

    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12
    private const val PBKDF2_ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val TAG_LENGTH_BITS = 128

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    /**
     * Encrypts a [VaultBackupPayload] into an authenticated, password-protected byte array.
     */
    fun encryptPayload(payload: VaultBackupPayload, password: CharArray): ByteArray {
        val secureRandom = SecureRandom()

        // 1. Generate random salt and IV
        val salt = ByteArray(SALT_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH_BYTES).also { secureRandom.nextBytes(it) }

        // 2. Derive 256-bit AES key via PBKDF2WithHmacSHA256
        val secretKey = deriveKey(password, salt)

        // 3. Serialize payload to UTF-8 JSON bytes
        val jsonString = json.encodeToString(payload)
        val plaintextBytes = jsonString.toByteArray(StandardCharsets.UTF_8)

        // 4. Encrypt with AES-256-GCM and bind magic header as Associated Authenticated Data (AAD)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        cipher.updateAAD(MAGIC_HEADER)
        val ciphertext = cipher.doFinal(plaintextBytes)

        // 5. Pack into binary wire format
        val byteOut = ByteArrayOutputStream()
        val dataOut = DataOutputStream(byteOut)

        dataOut.write(MAGIC_HEADER)
        dataOut.writeInt(CURRENT_VERSION)
        dataOut.write(salt)
        dataOut.write(iv)
        dataOut.write(ciphertext)
        dataOut.flush()

        return byteOut.toByteArray()
    }

    /**
     * Decrypts an authenticated, password-protected byte array back into a [VaultBackupPayload].
     *
     * @throws InvalidBackupFormatException if the file header or size is invalid.
     * @throws InvalidBackupPasswordException if the password is incorrect or ciphertext has been altered.
     */
    fun decryptPayload(encryptedBytes: ByteArray, password: CharArray): VaultBackupPayload {
        val minExpectedLength = MAGIC_HEADER.size + 4 + SALT_LENGTH_BYTES + IV_LENGTH_BYTES + 16
        if (encryptedBytes.size < minExpectedLength) {
            throw InvalidBackupFormatException("The selected file is too small or incomplete to be a valid Folio vault archive.")
        }

        val byteIn = ByteArrayInputStream(encryptedBytes)
        val dataIn = DataInputStream(byteIn)

        // 1. Verify Magic Header
        val header = ByteArray(MAGIC_HEADER.size)
        dataIn.readFully(header)
        if (!header.contentEquals(MAGIC_HEADER)) {
            throw InvalidBackupFormatException("Unrecognized file format. The archive header does not match the Folio vault signature.")
        }

        // 2. Verify Version
        val version = dataIn.readInt()
        if (version != CURRENT_VERSION) {
            throw InvalidBackupFormatException("Unsupported archive version ($version). Please update the application to restore this backup.")
        }

        // 3. Read Salt and IV
        val salt = ByteArray(SALT_LENGTH_BYTES)
        dataIn.readFully(salt)

        val iv = ByteArray(IV_LENGTH_BYTES)
        dataIn.readFully(iv)

        // 4. Read Ciphertext (including GCM tag)
        val ciphertext = ByteArray(dataIn.available())
        dataIn.readFully(ciphertext)

        // 5. Derive key and decrypt
        val secretKey = deriveKey(password, salt)
        val plaintextBytes = try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            cipher.updateAAD(MAGIC_HEADER)
            cipher.doFinal(ciphertext)
        } catch (e: AEADBadTagException) {
            throw InvalidBackupPasswordException("Incorrect passphrase or corrupted archive. Authentication verification failed.", e)
        } catch (e: BadPaddingException) {
            throw InvalidBackupPasswordException("Incorrect passphrase or corrupted archive.", e)
        } catch (e: Exception) {
            throw InvalidBackupPasswordException("Decryption failed: ${e.localizedMessage ?: "Unknown error"}", e)
        }

        // 6. Deserialize JSON
        val jsonString = String(plaintextBytes, StandardCharsets.UTF_8)
        return try {
            json.decodeFromString<VaultBackupPayload>(jsonString)
        } catch (e: Exception) {
            throw InvalidBackupFormatException("Archive payload could not be parsed: ${e.localizedMessage}")
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")
        spec.clearPassword()
        return secretKey
    }
}
