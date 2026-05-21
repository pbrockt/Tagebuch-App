package com.pbrockt.tagebuch.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.text.KeyboardOptions as FoundationKeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import com.pbrockt.tagebuch.data.remote.SyncResult
import com.pbrockt.tagebuch.ui.theme.accentMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val authMethod by viewModel.authMethod.collectAsState()
    val webDavUrl by viewModel.webDavUrl.collectAsState()
    val webDavUser by viewModel.webDavUser.collectAsState()
    val webDavPassword by viewModel.webDavPassword.collectAsState()
    val webDavEncPass by viewModel.webDavEncPass.collectAsState()
    val syncEnabled by viewModel.syncEnabled.collectAsState()
    val themeChoice by viewModel.themeChoice.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val calendarIconMode by viewModel.calendarIconMode.collectAsState()
    val fontChoice by viewModel.fontChoice.collectAsState()
    val ownBirthday by viewModel.ownBirthday.collectAsState()
    val lockTimeout by viewModel.lockTimeout.collectAsState()
    val periodTracking by viewModel.periodTracking.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val testState by viewModel.testState.collectAsState()
    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()

    var newPin by remember { mutableStateOf("") }
    var ownBirthdayInput by remember(ownBirthday) { mutableStateOf(ownBirthday) }
    var showPinInput by remember { mutableStateOf(false) }
    var webDavUrlInput by remember(webDavUrl) { mutableStateOf(webDavUrl) }
    var webDavUserInput by remember(webDavUser) { mutableStateOf(webDavUser) }
    var webDavPassInput by remember(webDavPassword) { mutableStateOf(webDavPassword) }
    var webDavEncPassInput by remember(webDavEncPass) { mutableStateOf(webDavEncPass) }
    var passVisible by remember { mutableStateOf(false) }
    var reminderHourInput by remember(reminderHour) { mutableStateOf(reminderHour.toString()) }
    var reminderMinuteInput by remember(reminderMinute) { mutableStateOf(reminderMinute.toString().padStart(2, '0')) }
    val updateState by viewModel.updateState.collectAsState()
    val context = LocalContext.current

    // Update-Check beim Öffnen des Screens
    LaunchedEffect(Unit) { viewModel.checkForUpdate() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- VERSIONS-KARTE ---
            VersionCard(updateState = updateState, context = context)

            // --- PIN ---
            SectionTitle("PIN-Schutz")
            if (authMethod != SecurePrefs.AUTH_NONE) {
                OutlinedButton(onClick = { viewModel.clearPin() }, Modifier.fillMaxWidth()) {
                    Text("PIN entfernen (kein Schutz)")
                }
            }
            if (showPinInput) {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPin = it },
                    label = { Text("Neuer PIN (genau 4 Stellen)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Button(
                    onClick = { if (newPin.length == 4) { viewModel.setPin(newPin); newPin = ""; showPinInput = false } },
                    enabled = newPin.length == 4, modifier = Modifier.fillMaxWidth()
                ) { Text("PIN speichern") }
            } else {
                OutlinedButton(onClick = { showPinInput = true }, Modifier.fillMaxWidth()) {
                    Text(if (authMethod == SecurePrefs.AUTH_PIN) "PIN ändern" else "4-stelligen PIN festlegen")
                }
            }

            HorizontalDivider()

            // --- DESIGN ---
            SectionTitle("Design")
            val themes = listOf(SecurePrefs.THEME_SYSTEM to "Systemstandard",
                SecurePrefs.THEME_LIGHT to "Immer hell", SecurePrefs.THEME_DARK to "Immer dunkel")
            themes.forEach { (key, label) ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = themeChoice == key, onClick = { viewModel.setTheme(key) })
                    Spacer(Modifier.width(8.dp)); Text(label)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Akzentfarbe", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            val colorOptions = listOf(
                SecurePrefs.ACCENT_PURPLE to "Lila", SecurePrefs.ACCENT_BLUE to "Blau",
                SecurePrefs.ACCENT_GREEN to "Grün", SecurePrefs.ACCENT_ORANGE to "Orange",
                SecurePrefs.ACCENT_PINK to "Rosa", SecurePrefs.ACCENT_TEAL to "Türkis"
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                colorOptions.forEach { (key, label) ->
                    val isDark = themeChoice == SecurePrefs.THEME_DARK
                    val swatchColor = (accentMap[key]?.let { if (isDark) it.dark else it.light }
                        ?: accentMap[SecurePrefs.ACCENT_PURPLE]!!.light).primary
                    val selected = accentColor == key
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(swatchColor)
                                .then(if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                                .clickable { viewModel.setAccentColor(key) },
                            contentAlignment = Alignment.Center
                        ) { if (selected) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = Color.White) }
                        Spacer(Modifier.height(4.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // --- Schriftart ---
            Spacer(Modifier.height(4.dp))
            Text("Schriftart", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            val fonts = listOf(
                SecurePrefs.FONT_DEFAULT to "Standard (Roboto)",
                SecurePrefs.FONT_SERIF to "Serif (klassisches Tagebuch)",
                SecurePrefs.FONT_MONO to "Schreibmaschine (Monospace)",
                SecurePrefs.FONT_CURSIVE to "Kursiv (Cursive)"
            )
            fonts.forEach { (key, label) ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = fontChoice == key, onClick = { viewModel.setFont(key) })
                    Spacer(Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Kalender-Anzeige", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            val iconModes = listOf(
                SecurePrefs.CALENDAR_MODE_MOOD to "Stimmung (Farbige Tages-Hintergrundfarbe)",
                SecurePrefs.CALENDAR_MODE_WEATHER to "Wetter (Wetter-Icon pro Tag)",
                SecurePrefs.CALENDAR_MODE_BOTH to "Beides (Farbe + Wetter-Icon)"
            )
            iconModes.forEach { (key, label) ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = calendarIconMode == key, onClick = { viewModel.setCalendarIconMode(key) })
                    Spacer(Modifier.width(8.dp)); Text(label, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Perioden-Tracking", style = MaterialTheme.typography.bodyMedium)
                    Text("🩸 Periode im Kalender eintragen", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = periodTracking, onCheckedChange = { viewModel.setPeriodTracking(it) })
            }

            HorizontalDivider()

            // --- ERINNERUNGEN ---
            SectionTitle("Erinnerungen")
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Tägliche Erinnerung")
                Switch(checked = reminderEnabled, onCheckedChange = { viewModel.setReminderEnabled(it) })
            }
            if (reminderEnabled) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = reminderHourInput, onValueChange = { if (it.length <= 2) reminderHourInput = it },
                        label = { Text("Stunde (0–23)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = reminderMinuteInput, onValueChange = { if (it.length <= 2) reminderMinuteInput = it },
                        label = { Text("Minute (0–59)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f), singleLine = true)
                }
                Button(onClick = {
                    val h = reminderHourInput.toIntOrNull()?.coerceIn(0, 23) ?: 21
                    val m = reminderMinuteInput.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    viewModel.saveReminderTime(h, m)
                }, Modifier.fillMaxWidth()) { Text("Erinnerungszeit speichern") }
            }

            HorizontalDivider()

            // --- WEBDAV ---
            SectionTitle("WebDAV-Synchronisation")
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Sync aktiviert")
                Switch(checked = syncEnabled, onCheckedChange = { viewModel.setSyncEnabled(it) })
            }
            OutlinedTextField(value = webDavUrlInput, onValueChange = { webDavUrlInput = it },
                label = { Text("Server URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = webDavUserInput, onValueChange = { webDavUserInput = it },
                label = { Text("Benutzername") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = webDavPassInput, onValueChange = { webDavPassInput = it },
                label = { Text("Passwort") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { passVisible = !passVisible }) {
                    Icon(if (passVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }})
            OutlinedTextField(value = webDavEncPassInput, onValueChange = { webDavEncPassInput = it },
                label = { Text("Verschlüsselungs-Passphrase") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, visualTransformation = PasswordVisualTransformation())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.saveWebDavConfig(webDavUrlInput, webDavUserInput, webDavPassInput, webDavEncPassInput) },
                    modifier = Modifier.weight(1f)
                ) { Text("Speichern") }
                OutlinedButton(
                    onClick = { viewModel.testConnection(webDavUrlInput, webDavUserInput, webDavPassInput) },
                    modifier = Modifier.weight(1f)
                ) { Text("Verbindung testen") }
            }

            // Test-Ergebnis
            when (testState) {
                is SyncResult.Success -> Text(
                    "✓ Verbindung erfolgreich",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
                is SyncResult.Error -> Text(
                    "✗ ${(testState as SyncResult.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                null, is SyncResult.NotConfigured -> {}
            }

            OutlinedButton(onClick = { viewModel.syncNow() }, Modifier.fillMaxWidth()) { Text("Jetzt synchronisieren") }
            when (syncState) {
                is SyncResult.Success -> Text("Sync erfolgreich ✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                is SyncResult.Error -> Text("Sync-Fehler: ${(syncState as SyncResult.Error).message}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                is SyncResult.NotConfigured -> Text("WebDAV nicht konfiguriert", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                null -> {}
            }

            HorizontalDivider()

            // --- BERECHTIGUNGEN & GEBURTSTAGE ---
            PermissionsSection(
                ownBirthdayInput = ownBirthdayInput,
                onBirthdayChanged = { ownBirthdayInput = it },
                onBirthdaySaved = { viewModel.setOwnBirthday(ownBirthdayInput) }
            )

        }
    }
}

@Composable
private fun VersionCard(updateState: UpdateState, context: android.content.Context) {
    val releasesUrl = "https://github.com/pbrockt/Tagebuch-App/releases/latest"
    Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .background(
                color = when (updateState) {
                    is UpdateState.UpdateAvailable -> MaterialTheme.colorScheme.tertiaryContainer
                    is UpdateState.UpToDate -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        when (updateState) {
            is UpdateState.Loading -> Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(modifier = androidx.compose.ui.Modifier.size(16.dp), strokeWidth = 2.dp)
                Text("Auf Updates prüfen...", style = MaterialTheme.typography.bodySmall)
            }
            is UpdateState.UpToDate -> Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = androidx.compose.ui.Modifier.size(18.dp))
                Text("Aktuelle Version ${updateState.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            is UpdateState.UpdateAvailable -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.NewReleases, null, tint = MaterialTheme.colorScheme.tertiary,
                        modifier = androidx.compose.ui.Modifier.size(18.dp))
                    Text("🆕 Neue Version ${updateState.latest} verfügbar!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releasesUrl))
                        context.startActivity(intent)
                    },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) { Text("Jetzt herunterladen →", style = MaterialTheme.typography.labelSmall) }
            }
            is UpdateState.Error -> Text("Update-Check: ${updateState.msg}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            is UpdateState.Idle -> {}
        }
    }
}

@Composable
private fun PermissionsSection(
    ownBirthdayInput: String,
    onBirthdayChanged: (String) -> Unit,
    onBirthdaySaved: () -> Unit
) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }

    val notifGranted = remember(refreshKey) {
        if (Build.VERSION.SDK_INT >= 33)
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        else true
    }
    val contactsGranted = remember(refreshKey) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refreshKey++ }
    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refreshKey++ }

    SectionTitle("Berechtigungen & Geburtstage")

    PermissionRow(
        title = "Benachrichtigungen",
        description = "Für tägliche Erinnerungen",
        icon = Icons.Default.Notifications,
        granted = notifGranted,
        onRequest = {
            if (Build.VERSION.SDK_INT >= 33) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    )
    PermissionRow(
        title = "Kontakte",
        description = "Geburtstage 🎈 im Kalender anzeigen",
        icon = Icons.Default.Contacts,
        granted = contactsGranted,
        onRequest = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) }
    )

    // Eigener Geburtstag
    Spacer(Modifier.height(8.dp))
    Text("Eigener Geburtstag (für 👑 im Kalender)",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = ownBirthdayInput,
            onValueChange = { if (it.length <= 5) onBirthdayChanged(it) },
            label = { Text("TT.MM (z.B. 15.03)") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        )
        Button(
            onClick = onBirthdaySaved,
            modifier = Modifier.align(Alignment.CenterVertically)
        ) { Text("Speichern") }
    }
    if (ownBirthdayInput.isNotEmpty()) {
        Text("👑 erscheint am $ownBirthdayInput im Kalender",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    granted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (granted) {
            Icon(Icons.Default.CheckCircle, "Erlaubt", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        } else {
            OutlinedButton(onClick = onRequest, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                Text("Erlauben", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}
