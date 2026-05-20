package com.pbrockt.tagebuch.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    val syncState by viewModel.syncState.collectAsState()
    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()

    var newPin by remember { mutableStateOf("") }
    var showPinInput by remember { mutableStateOf(false) }
    var webDavUrlInput by remember(webDavUrl) { mutableStateOf(webDavUrl) }
    var webDavUserInput by remember(webDavUser) { mutableStateOf(webDavUser) }
    var webDavPassInput by remember(webDavPassword) { mutableStateOf(webDavPassword) }
    var webDavEncPassInput by remember(webDavEncPass) { mutableStateOf(webDavEncPass) }
    var passVisible by remember { mutableStateOf(false) }
    var reminderHourInput by remember(reminderHour) { mutableStateOf(reminderHour.toString()) }
    var reminderMinuteInput by remember(reminderMinute) { mutableStateOf(reminderMinute.toString().padStart(2, '0')) }

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
            Button(onClick = { viewModel.saveWebDavConfig(webDavUrlInput, webDavUserInput, webDavPassInput, webDavEncPassInput) },
                Modifier.fillMaxWidth()) { Text("WebDAV speichern") }
            OutlinedButton(onClick = { viewModel.syncNow() }, Modifier.fillMaxWidth()) { Text("Jetzt synchronisieren") }
            when (syncState) {
                is SyncResult.Success -> Text("Sync erfolgreich", color = MaterialTheme.colorScheme.primary)
                is SyncResult.Error -> Text("Fehler: ${(syncState as SyncResult.Error).message}", color = MaterialTheme.colorScheme.error)
                is SyncResult.NotConfigured -> Text("WebDAV nicht konfiguriert", color = MaterialTheme.colorScheme.onSurfaceVariant)
                null -> {}
            }

            Spacer(Modifier.height(8.dp))
            Text("Version 0.1a", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}
