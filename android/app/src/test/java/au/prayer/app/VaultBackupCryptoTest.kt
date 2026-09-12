package au.prayer.app

import au.prayer.app.data.models.*
import au.prayer.app.data.security.InvalidBackupFormatException
import au.prayer.app.data.security.InvalidBackupPasswordException
import au.prayer.app.data.security.VaultBackupCrypto
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

/**
 * Unit test suite verifying the cryptographic security, AEAD integrity,
 * and serialization fidelity of the password-protected Folio vault backup system.
 */
class VaultBackupCryptoTest {

    private val samplePayload = VaultBackupPayload(
        version = 1,
        createdAt = 1726140000000L,
        appVersion = "1.0.0",
        config = AppConfig(
            localeDialect = LocaleDialect.EN_AU_UK,
            themeMode = ThemeMode.HORWEEN_CORDOVAN,
            textScale = TextScale.REGULAR,
            blendHistoricPrayers = true,
            highContrastMode = false
        ),
        entities = listOf(
            IndividualEntity(
                id = "ent-1",
                rootCode = RootCode.PEOPLE,
                displayName = "Thomas Cranmer",
                contextDescription = "Archbishop of Canterbury",
                isPinned = true,
                interactedCount = 14
            ),
            IndividualEntity(
                id = "ent-2",
                rootCode = RootCode.MISSION_PARTNERS,
                displayName = "Church Missionary Society",
                contextDescription = "Global mission gospel proclamation",
                isPinned = false,
                interactedCount = 5
            )
        ),
        prayerPoints = listOf(
            PrayerPoint(
                id = "pt-1",
                entityId = "ent-1",
                title = "Steadfast faith under persecution",
                description = "For unwavering adherence to the holy gospel and peace in Christ.",
                status = PrayerStatus.ACTIVE,
                interactedCount = 12
            ),
            PrayerPoint(
                id = "pt-2",
                entityId = "ent-1",
                title = "Thanksgiving for the Book of Common Prayer",
                description = "For liturgical reverence and sound Reformed theology.",
                status = PrayerStatus.ANSWERED,
                answeredAt = 1726139000000L,
                answeredTestimony = "Preserved across centuries to the glory of God."
            )
        ),
        libraryProgress = listOf(
            ReadingProgress(
                volumeId = "1662_bcp",
                lastSectionNumber = 4,
                lastScrollOffset = 180,
                updatedAt = 1726138000000L
            )
        )
    )

    @Test
    fun `test successful encryption and decryption roundtrip`() {
        val password = "StCranmerPassphrase1662!".toCharArray()

        val encryptedBytes = VaultBackupCrypto.encryptPayload(samplePayload, password)
        assertNotNull(encryptedBytes)
        assertTrue("Encrypted file must have valid size", encryptedBytes.size > 100)

        // Decrypt with the exact password
        val restored = VaultBackupCrypto.decryptPayload(encryptedBytes, password)

        assertEquals(samplePayload.version, restored.version)
        assertEquals(samplePayload.appVersion, restored.appVersion)
        assertEquals(samplePayload.config.themeMode, restored.config.themeMode)
        assertEquals(samplePayload.config.localeDialect, restored.config.localeDialect)
        assertEquals(samplePayload.config.blendHistoricPrayers, restored.config.blendHistoricPrayers)

        assertEquals(2, restored.entities.size)
        assertEquals("Thomas Cranmer", restored.entities[0].displayName)
        assertTrue(restored.entities[0].isPinned)
        assertEquals(RootCode.MISSION_PARTNERS, restored.entities[1].rootCode)

        assertEquals(2, restored.prayerPoints.size)
        assertEquals(PrayerStatus.ACTIVE, restored.prayerPoints[0].status)
        assertEquals(PrayerStatus.ANSWERED, restored.prayerPoints[1].status)
        assertEquals("Preserved across centuries to the glory of God.", restored.prayerPoints[1].answeredTestimony)

        assertEquals(1, restored.libraryProgress.size)
        assertEquals("1662_bcp", restored.libraryProgress[0].volumeId)
        assertEquals(4, restored.libraryProgress[0].lastSectionNumber)
    }

    @Test
    fun `test incorrect password throws InvalidBackupPasswordException`() {
        val validPassword = "CorrectPassphrase2026!".toCharArray()
        val wrongPassword = "WrongPassphrase1234!".toCharArray()

        val encryptedBytes = VaultBackupCrypto.encryptPayload(samplePayload, validPassword)

        assertThrows(InvalidBackupPasswordException::class.java) {
            VaultBackupCrypto.decryptPayload(encryptedBytes, wrongPassword)
        }
    }

    @Test
    fun `test corrupted ciphertext triggers AEAD tamper failure`() {
        val password = "TamperDetectionPass1!".toCharArray()
        val encryptedBytes = VaultBackupCrypto.encryptPayload(samplePayload, password)

        // Corrupt a byte in the ciphertext payload
        val corruptedBytes = encryptedBytes.clone()
        val tamperIndex = corruptedBytes.size - 10
        corruptedBytes[tamperIndex] = (corruptedBytes[tamperIndex].toInt() xor 0xFF).toByte()

        assertThrows(InvalidBackupPasswordException::class.java) {
            VaultBackupCrypto.decryptPayload(corruptedBytes, password)
        }
    }

    @Test
    fun `test truncated file triggers InvalidBackupFormatException`() {
        val password = "ShortFilePassphrase!".toCharArray()
        val truncatedBytes = byteArrayOf(1, 2, 3, 4, 5)

        assertThrows(InvalidBackupFormatException::class.java) {
            VaultBackupCrypto.decryptPayload(truncatedBytes, password)
        }
    }

    @Test
    fun `test invalid magic header triggers InvalidBackupFormatException`() {
        val password = "MagicHeaderPassphrase!".toCharArray()
        val encryptedBytes = VaultBackupCrypto.encryptPayload(samplePayload, password)

        // Mutate the first byte of magic header "PWCVAULT"
        val corruptedHeaderBytes = encryptedBytes.clone()
        corruptedHeaderBytes[0] = 'X'.code.toByte()

        val exception = assertThrows(InvalidBackupFormatException::class.java) {
            VaultBackupCrypto.decryptPayload(corruptedHeaderBytes, password)
        }
        assertTrue(exception.message!!.contains("header does not match"))
    }
}
