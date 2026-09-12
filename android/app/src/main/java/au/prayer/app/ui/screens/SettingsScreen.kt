package au.prayer.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.prayer.app.data.local.PrayerRepository
import au.prayer.app.data.models.AppConfig
import au.prayer.app.data.models.LocaleDialect
import au.prayer.app.data.models.RestoreSummary
import au.prayer.app.data.models.ThemeMode
import au.prayer.app.data.security.VaultBackupCrypto
import au.prayer.app.ui.components.ExportVaultBackupDialog
import au.prayer.app.ui.components.RestoreVaultBackupDialog
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modern Leatherbound Folio Settings Screen.
 * Secluded preference controls for:
 * - Leather Casing Finish (Saddle Tan, Horween Cordovan, Hunter Forest, Obsidian Hide)
 * - Liturgical Language & Dialect (AU/UK 1662 BCP vs US)
 * - Devotional Content (Historic Reformed Prayers in daily rotation)
 * - Display Accessibility (High-Contrast Mode)
 * - Vault Archive & Portability (Password-Protected AES-256-GCM Backup & Restore)
 *
 * Adheres strictly to the Modern Folio visual design bible:
 * - 0dp rectilinear geometry (FlatSquareShape)
 * - Standardized 8dp spacing cadence (PrayerSpacing)
 * - Baseline-anchored hairline rules (paperFeintRule)
 * - Universal 48dp+ touch boundaries for accessible handling
 */
@Composable
fun SettingsScreen(
    config: AppConfig,
    colors: PrayerColors,
    typography: PrayerTypography,
    onUpdateConfig: (AppConfig) -> Unit,
    repository: PrayerRepository? = null,
    onVaultRestored: ((RestoreSummary) -> Unit)? = null,
    onShowMessage: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showExportDialog by remember { mutableStateOf(false) }
    var pendingExportPassphrase by remember { mutableStateOf<String?>(null) }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        val passphrase = pendingExportPassphrase
        pendingExportPassphrase = null
        if (uri != null && passphrase != null && repository != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val payload = repository.createBackupPayload()
                    val bytes = VaultBackupCrypto.encryptPayload(payload, passphrase.toCharArray())
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(bytes)
                        output.flush()
                    }
                    onShowMessage?.invoke("Prayer vault successfully sealed and exported")
                } catch (e: Exception) {
                    onShowMessage?.invoke("Export failed: ${e.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreDialog = true
        }
    }

    if (showExportDialog) {
        ExportVaultBackupDialog(
            colors = colors,
            typography = typography,
            onDismiss = { showExportDialog = false },
            onConfirmExport = { passphrase ->
                showExportDialog = false
                pendingExportPassphrase = passphrase
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.ENGLISH).format(Date())
                createDocumentLauncher.launch("PrayerVault_$timeStamp.folio")
            }
        )
    }

    if (showRestoreDialog) {
        RestoreVaultBackupDialog(
            colors = colors,
            typography = typography,
            onDismiss = {
                showRestoreDialog = false
                pendingRestoreUri = null
            },
            onConfirmRestore = { passphrase, replaceExisting ->
                showRestoreDialog = false
                val uri = pendingRestoreUri
                pendingRestoreUri = null
                if (uri != null && repository != null) {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                ?: throw IllegalStateException("Could not open selected archive file.")
                            val payload = VaultBackupCrypto.decryptPayload(bytes, passphrase.toCharArray())
                            val summary = repository.restoreBackupPayload(payload, replaceExisting)
                            onVaultRestored?.invoke(summary)
                        } catch (e: Exception) {
                            onShowMessage?.invoke(e.localizedMessage ?: "Failed to restore archive.")
                        }
                    }
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = PrayerSpacing.large, vertical = PrayerSpacing.large)
        ) {
            // Folio Colophon Header
            Text(
                text = "FOLIO PREFERENCES & BINDING",
                style = typography.caption.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                ),
                color = colors.inkMuted
            )
            Text(
                text = "Configure the tactile materials, liturgical dialect, and devotional rotation of your prayer journal.",
                style = typography.caption,
                color = colors.textSubtle,
                modifier = Modifier.padding(top = PrayerSpacing.extraSmall, bottom = PrayerSpacing.large)
            )

            HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
            Spacer(modifier = Modifier.height(PrayerSpacing.large))

            // --- Section 1: Leather Finish ---
            SettingsSectionHeader(
                title = "Leather Finish",
                subtitle = "Select the vegetable-tanned leather dye for your outer casing and accents.",
                colors = colors,
                typography = typography
            )

            val leatherOptions = listOf(
                LeatherOption(
                    mode = ThemeMode.SADDLE_TAN,
                    title = "Saddle Tan",
                    leatherColor = colors.leatherPrimary,
                    description = "Classic warm oiled saddle leather; organic and comforting."
                ),
                LeatherOption(
                    mode = ThemeMode.HORWEEN_CORDOVAN,
                    title = "Horween Cordovan",
                    leatherColor = colors.leatherCordovan,
                    description = "Deep oxblood leather; contemplative evening vigil tone."
                ),
                LeatherOption(
                    mode = ThemeMode.HUNTER_FOREST,
                    title = "Hunter Forest",
                    leatherColor = colors.leatherForest,
                    description = "Slate-moss dyed leather; grounded, meditative intercession."
                ),
                LeatherOption(
                    mode = ThemeMode.OBSIDIAN_HIDE,
                    title = "Obsidian Hide",
                    leatherColor = colors.leatherObsidian,
                    description = "Archival black leather with rich charcoal undertones."
                )
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(PrayerSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                leatherOptions.forEach { opt ->
                    val isSelected = config.themeMode == opt.mode ||
                            (opt.mode == ThemeMode.SADDLE_TAN && config.themeMode == ThemeMode.WARM_VINTAGE_WHITE)

                    val cardBorder by animateColorAsState(
                        targetValue = if (isSelected) opt.leatherColor else colors.borderSubtle,
                        animationSpec = tween(200),
                        label = "LeatherCardBorder"
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = PrayerSpacing.primaryActionHeight)
                            .clickable(role = Role.RadioButton) {
                                onUpdateConfig(config.copy(themeMode = opt.mode))
                            },
                        shape = FlatSquareShape,
                        color = if (isSelected) colors.surfaceElevated else colors.surface,
                        border = BorderStroke(if (isSelected) 1.5.dp else PrayerSpacing.hairlineWidth, cardBorder),
                        tonalElevation = PrayerSpacing.elevationNone
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PrayerSpacing.medium, vertical = PrayerSpacing.medium),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.medium)
                        ) {
                            // Circular Leather Color Swatch Medallion
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(opt.leatherColor, CircleShape)
                                    .border(1.dp, colors.borderSubtle, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                                ) {
                                    Text(
                                        text = opt.title,
                                        style = typography.prayerPointBody.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "ACTIVE",
                                            style = typography.marginStatus,
                                            color = opt.leatherColor
                                        )
                                    }
                                }
                                Text(
                                    text = opt.description,
                                    style = typography.caption,
                                    color = colors.textSubtle
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(PrayerSpacing.large))
            HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
            Spacer(modifier = Modifier.height(PrayerSpacing.large))

            // --- Section 2: Language & Liturgical Dialect ---
            SettingsSectionHeader(
                title = "Language & Orthography",
                subtitle = "Liturgical vocabulary and spelling conventions for application copy and prayers.",
                colors = colors,
                typography = typography
            )


            val dialectOptions = listOf(
                DialectOption(
                    dialect = LocaleDialect.EN_AU_UK,
                    title = "English (Australian / UK)",
                    badge = "Default",
                    description = "Historic 1662 Book of Common Prayer orthography (e.g., Saviour, honour)."
                ),
                DialectOption(
                    dialect = LocaleDialect.EN_US,
                    title = "US English",
                    badge = null,
                    description = "American spelling conventions (e.g., Savior, honor)."
                )
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(PrayerSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                dialectOptions.forEach { opt ->
                    val isSelected = config.localeDialect == opt.dialect

                    val cardBorder by animateColorAsState(
                        targetValue = if (isSelected) colors.leatherActive else colors.borderSubtle,
                        animationSpec = tween(200),
                        label = "DialectCardBorder"
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = PrayerSpacing.primaryActionHeight)
                            .clickable(role = Role.RadioButton) {
                                onUpdateConfig(config.copy(localeDialect = opt.dialect))
                            },
                        shape = FlatSquareShape,
                        color = if (isSelected) colors.surfaceElevated else colors.surface,
                        border = BorderStroke(if (isSelected) 1.5.dp else PrayerSpacing.hairlineWidth, cardBorder),
                        tonalElevation = PrayerSpacing.elevationNone
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PrayerSpacing.medium, vertical = PrayerSpacing.medium),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.medium)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                                ) {
                                    Text(
                                        text = opt.title,
                                        style = typography.prayerPointBody.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary
                                    )
                                    if (opt.badge != null) {
                                        Text(
                                            text = opt.badge,
                                            style = typography.marginStatus,
                                            color = colors.inkMuted
                                        )
                                    }
                                }
                                Text(
                                    text = opt.description,
                                    style = typography.caption,
                                    color = colors.textSubtle
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = colors.leatherActive,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(PrayerSpacing.large))
            HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
            Spacer(modifier = Modifier.height(PrayerSpacing.large))

            // --- Section 3: Devotional Content (Historic Prayers) ---
            SettingsSectionHeader(
                title = "Historic Prayers",
                subtitle = "Classic Protestant collects, creeds, and liturgical prayers.",
                colors = colors,
                typography = typography
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = PrayerSpacing.primaryActionHeight)
                    .clickable(role = Role.Switch) {
                        onUpdateConfig(config.copy(blendHistoricPrayers = !config.blendHistoricPrayers))
                    },
                shape = FlatSquareShape,
                color = colors.surface,
                border = BorderStroke(PrayerSpacing.hairlineWidth, colors.borderSubtle),
                tonalElevation = PrayerSpacing.elevationNone
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PrayerSpacing.medium, vertical = PrayerSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.medium)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Blend into daily prayer",
                            style = typography.prayerPointBody.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Interleaves classic 1662 Book of Common Prayer collects, the Apostles' Creed, and the Lord's Prayer into your daily prayer rotation alongside personal burdens.",
                            style = typography.caption,
                            color = colors.textSubtle
                        )
                        Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                        Text(
                            text = if (config.blendHistoricPrayers) "• Active in daily prayer rotation" else "• Available in Journal directory only",
                            style = typography.caption,
                            color = if (config.blendHistoricPrayers) colors.leatherActive else colors.inkMuted
                        )
                    }
                    Switch(
                        checked = config.blendHistoricPrayers,
                        onCheckedChange = { isChecked ->
                            onUpdateConfig(config.copy(blendHistoricPrayers = isChecked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.surface,
                            checkedTrackColor = colors.leatherActive,
                            uncheckedThumbColor = colors.textSubtle,
                            uncheckedTrackColor = colors.surfaceSubtle
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(PrayerSpacing.large))
            HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
            Spacer(modifier = Modifier.height(PrayerSpacing.large))

            // --- Section 4: Accessibility ---
            SettingsSectionHeader(
                title = "Accessibility",
                subtitle = "Display contrast and readability tuning.",
                colors = colors,
                typography = typography
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = PrayerSpacing.primaryActionHeight)
                    .clickable(role = Role.Switch) {
                        onUpdateConfig(config.copy(highContrastMode = !config.highContrastMode))
                    },
                shape = FlatSquareShape,
                color = colors.surface,
                border = BorderStroke(PrayerSpacing.hairlineWidth, colors.borderSubtle),
                tonalElevation = PrayerSpacing.elevationNone
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PrayerSpacing.medium, vertical = PrayerSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.medium)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "High-Contrast Mode",
                            style = typography.prayerPointBody.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Meets WCAG 2.1 AA standards with high-definition ink contrast, crisp baseline rules, and stark white paper ground.",
                            style = typography.caption,
                            color = colors.textSubtle
                        )
                        Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                        Text(
                            text = if (config.highContrastMode) "• High-contrast vellum active" else "• Singular warm vellum theme",
                            style = typography.caption,
                            color = if (config.highContrastMode) colors.leatherActive else colors.inkMuted
                        )
                    }
                    Switch(
                        checked = config.highContrastMode,
                        onCheckedChange = { isChecked ->
                            onUpdateConfig(config.copy(highContrastMode = isChecked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.surface,
                            checkedTrackColor = colors.leatherActive,
                            uncheckedThumbColor = colors.textSubtle,
                            uncheckedTrackColor = colors.surfaceSubtle
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(PrayerSpacing.large))
            HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
            Spacer(modifier = Modifier.height(PrayerSpacing.large))

            // --- Section 5: Vault Archive & Portability ---
            SettingsSectionHeader(
                title = "Vault Archive & Portability",
                subtitle = "Manual, zero-telemetry export and restore. Seal your prayer journal into a password-encrypted .folio file.",
                colors = colors,
                typography = typography
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(PrayerSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Export Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = PrayerSpacing.primaryActionHeight)
                        .clickable(role = Role.Button) {
                            showExportDialog = true
                        },
                    shape = FlatSquareShape,
                    color = colors.surface,
                    border = BorderStroke(PrayerSpacing.hairlineWidth, colors.borderSubtle),
                    tonalElevation = PrayerSpacing.elevationNone
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PrayerSpacing.medium, vertical = PrayerSpacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.medium)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export Encrypted Archive",
                                style = typography.prayerPointBody.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Seals personal topics, prayer points, and reading progress with AES-256-GCM using your personal passphrase.",
                                style = typography.caption,
                                color = colors.textSubtle
                            )
                        }
                        Text(
                            text = "SEAL ↗",
                            style = typography.marginStatus,
                            color = colors.leatherActive
                        )
                    }
                }

                // Restore Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = PrayerSpacing.primaryActionHeight)
                        .clickable(role = Role.Button) {
                            openDocumentLauncher.launch(arrayOf("*/*"))
                        },
                    shape = FlatSquareShape,
                    color = colors.surface,
                    border = BorderStroke(PrayerSpacing.hairlineWidth, colors.borderSubtle),
                    tonalElevation = PrayerSpacing.elevationNone
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PrayerSpacing.medium, vertical = PrayerSpacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.medium)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Restore Folio Archive",
                                style = typography.prayerPointBody.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Unseal a previously exported backup file to restore or merge prayer entries across devices.",
                                style = typography.caption,
                                color = colors.textSubtle
                            )
                        }
                        Text(
                            text = "UNSEAL ↙",
                            style = typography.marginStatus,
                            color = colors.leatherActive
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(PrayerSpacing.extraLarge))

            // --- Section 6: Folio Colophon & Version Footer ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PrayerSpacing.extraSmall)
            ) {
                Text(
                    text = "─── ❧ ───",
                    style = typography.caption,
                    color = colors.inkMuted,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "PRAY WITHOUT CEASING",
                    style = typography.caption.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    ),
                    color = colors.leatherActive,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Grounded in the 1662 Book of Common Prayer & Historic Reformed Formularies",
                    style = typography.caption,
                    color = colors.inkMuted,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "100% Offline Vault • Hardware-Encrypted Device Storage • Version 1.0",
                    style = typography.caption,
                    color = colors.textSubtle,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(PrayerSpacing.huge))
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    subtitle: String,
    colors: PrayerColors,
    typography: PrayerTypography
) {
    Column(modifier = Modifier.padding(bottom = PrayerSpacing.small)) {
        Text(
            text = title.uppercase(),
            style = typography.categoryLedgerHeader,
            color = colors.leatherActive
        )
        Text(
            text = subtitle,
            style = typography.caption,
            color = colors.textSubtle,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private data class LeatherOption(
    val mode: ThemeMode,
    val title: String,
    val leatherColor: Color,
    val description: String
)

private data class DialectOption(
    val dialect: LocaleDialect,
    val title: String,
    val badge: String?,
    val description: String
)

