package au.prayer.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography

/**
 * Modern Folio dialog for setting a passphrase to seal and export an encrypted backup.
 */
@Composable
fun ExportVaultBackupDialog(
    colors: PrayerColors,
    typography: PrayerTypography,
    onDismiss: () -> Unit,
    onConfirmExport: (passphrase: String) -> Unit
) {
    var passphrase by remember { mutableStateOf("") }
    var confirmPassphrase by remember { mutableStateOf("") }
    var isPassphraseVisible by remember { mutableStateOf(false) }

    val isValidLength = passphrase.length >= 8
    val isMatching = passphrase == confirmPassphrase
    val canSubmit = isValidLength && isMatching

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = FlatSquareShape,
        containerColor = colors.surface,
        tonalElevation = PrayerSpacing.elevationNone,
        title = {
            Text(
                text = "Seal Folio Archive",
                style = typography.prayerPointTitle,
                color = colors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(PrayerSpacing.medium)
            ) {
                Text(
                    text = "Choose an encryption passphrase. Your archive will be sealed with hardware-resistant AES-256-GCM encryption. If this passphrase is lost, the archive cannot be recovered.",
                    style = typography.caption,
                    color = colors.textSubtle
                )

                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text("Passphrase (min 8 characters)", style = typography.caption) },
                    visualTransformation = if (isPassphraseVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPassphraseVisible = !isPassphraseVisible }) {
                            Icon(
                                imageVector = if (isPassphraseVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPassphraseVisible) "Hide passphrase" else "Show passphrase",
                                tint = colors.textSubtle
                            )
                        }
                    },
                    textStyle = typography.prayerPointBody.copy(color = colors.textPrimary),
                    shape = FlatSquareShape,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.leatherActive,
                        unfocusedBorderColor = colors.borderSubtle,
                        focusedLabelColor = colors.leatherActive,
                        unfocusedLabelColor = colors.textSubtle,
                        cursorColor = colors.leatherActive
                    )
                )

                OutlinedTextField(
                    value = confirmPassphrase,
                    onValueChange = { confirmPassphrase = it },
                    label = { Text("Confirm Passphrase", style = typography.caption) },
                    visualTransformation = if (isPassphraseVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    textStyle = typography.prayerPointBody.copy(color = colors.textPrimary),
                    shape = FlatSquareShape,
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPassphrase.isNotEmpty() && !isMatching,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.leatherActive,
                        unfocusedBorderColor = colors.borderSubtle,
                        focusedLabelColor = colors.leatherActive,
                        unfocusedLabelColor = colors.textSubtle,
                        cursorColor = colors.leatherActive
                    )
                )

                if (confirmPassphrase.isNotEmpty() && !isMatching) {
                    Text(
                        text = "Passphrases do not match.",
                        style = typography.caption,
                        color = colors.leatherCordovan
                    )
                } else if (passphrase.isNotEmpty() && !isValidLength) {
                    Text(
                        text = "Passphrase must be at least 8 characters.",
                        style = typography.caption,
                        color = colors.textSubtle
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canSubmit) {
                        onConfirmExport(passphrase)
                    }
                },
                enabled = canSubmit,
                shape = FlatSquareShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.leatherActive,
                    contentColor = colors.surface,
                    disabledContainerColor = colors.surfaceSubtle,
                    disabledContentColor = colors.textSubtle
                ),
                modifier = Modifier.height(PrayerSpacing.primaryActionHeight)
            ) {
                Text("Seal & Export", style = typography.button)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = FlatSquareShape,
                border = BorderStroke(PrayerSpacing.hairlineWidth, colors.borderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.textPrimary
                ),
                modifier = Modifier.height(PrayerSpacing.primaryActionHeight)
            ) {
                Text("Cancel", style = typography.button)
            }
        }
    )
}

/**
 * Modern Folio dialog for unsealing an archive file with passphrase and selecting restore strategy.
 */
@Composable
fun RestoreVaultBackupDialog(
    colors: PrayerColors,
    typography: PrayerTypography,
    onDismiss: () -> Unit,
    onConfirmRestore: (passphrase: String, replaceExisting: Boolean) -> Unit
) {
    var passphrase by remember { mutableStateOf("") }
    var isPassphraseVisible by remember { mutableStateOf(false) }
    var replaceExisting by remember { mutableStateOf(false) }

    val canSubmit = passphrase.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = FlatSquareShape,
        containerColor = colors.surface,
        tonalElevation = PrayerSpacing.elevationNone,
        title = {
            Text(
                text = "Unseal Folio Archive",
                style = typography.prayerPointTitle,
                color = colors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(PrayerSpacing.medium)
            ) {
                Text(
                    text = "Enter the encryption passphrase used to seal this archive.",
                    style = typography.caption,
                    color = colors.textSubtle
                )

                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text("Archive Passphrase", style = typography.caption) },
                    visualTransformation = if (isPassphraseVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPassphraseVisible = !isPassphraseVisible }) {
                            Icon(
                                imageVector = if (isPassphraseVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPassphraseVisible) "Hide passphrase" else "Show passphrase",
                                tint = colors.textSubtle
                            )
                        }
                    },
                    textStyle = typography.prayerPointBody.copy(color = colors.textPrimary),
                    shape = FlatSquareShape,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.leatherActive,
                        unfocusedBorderColor = colors.borderSubtle,
                        focusedLabelColor = colors.leatherActive,
                        unfocusedLabelColor = colors.textSubtle,
                        cursorColor = colors.leatherActive
                    )
                )

                HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)

                Text(
                    text = "RESTORE STRATEGY",
                    style = typography.marginStatus,
                    color = colors.leatherActive
                )

                // Strategy 1: Merge
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.RadioButton) { replaceExisting = false },
                    shape = FlatSquareShape,
                    color = if (!replaceExisting) colors.surfaceElevated else colors.surface,
                    border = BorderStroke(
                        if (!replaceExisting) 1.5.dp else PrayerSpacing.hairlineWidth,
                        if (!replaceExisting) colors.leatherActive else colors.borderSubtle
                    )
                ) {
                    Column(modifier = Modifier.padding(PrayerSpacing.medium)) {
                        Text(
                            text = "Merge with Existing (Recommended)",
                            style = typography.prayerPointBody.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Preserves current prayer topics and adds items from the archive without overwriting.",
                            style = typography.caption,
                            color = colors.textSubtle
                        )
                    }
                }

                // Strategy 2: Replace
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.RadioButton) { replaceExisting = true },
                    shape = FlatSquareShape,
                    color = if (replaceExisting) colors.surfaceElevated else colors.surface,
                    border = BorderStroke(
                        if (replaceExisting) 1.5.dp else PrayerSpacing.hairlineWidth,
                        if (replaceExisting) colors.leatherCordovan else colors.borderSubtle
                    )
                ) {
                    Column(modifier = Modifier.padding(PrayerSpacing.medium)) {
                        Text(
                            text = "Replace Personal Prayers",
                            style = typography.prayerPointBody.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Wipes existing personal topics and replaces them with this backup. Preloaded historic prayers are preserved.",
                            style = typography.caption,
                            color = colors.textSubtle
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canSubmit) {
                        onConfirmRestore(passphrase, replaceExisting)
                    }
                },
                enabled = canSubmit,
                shape = FlatSquareShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.leatherActive,
                    contentColor = colors.surface,
                    disabledContainerColor = colors.surfaceSubtle,
                    disabledContentColor = colors.textSubtle
                ),
                modifier = Modifier.height(PrayerSpacing.primaryActionHeight)
            ) {
                Text("Unseal & Restore", style = typography.button)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = FlatSquareShape,
                border = BorderStroke(PrayerSpacing.hairlineWidth, colors.borderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.textPrimary
                ),
                modifier = Modifier.height(PrayerSpacing.primaryActionHeight)
            ) {
                Text("Cancel", style = typography.button)
            }
        }
    )
}
