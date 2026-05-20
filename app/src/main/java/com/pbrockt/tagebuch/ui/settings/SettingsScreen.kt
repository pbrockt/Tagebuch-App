package com.pbrockt.tagebuch.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import com.pbrockt.tagebuch.data.remote.SyncResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val authMethod by viewModel.authMethod.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val webDavUrl by viewModel.webDavUrl.collectAsState()
    val webDavUser by viewModel.webDavUser.collectAsState()
    val webDavPassword by viewModel.webDavPassword.collectAsState()
    val webDavEncPass by viewModel.webDavEncPass.collectAsState()
    val syncEnabled by viewModel.syncEnabled.collectAsState()
    val themeChoice by viewModel.themeChoice.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    var newPin by remember { mutableStateOf("") }
    var showPinInput by remember { mutableStateOf(false) }
    var webDavUrlInput by remember(webDavUrl) { mutableStateOf(webDavUrl) }
    var webDavUserInput by remember(webDavUser) { mutableStateOf(webDavUser) }
    var webDavPassInput by remember(webDavPassword) { mutableStateOf(webDavPassword) }
    var webDavEncPassInput by remember(webDavEncPass) { mutableStateOf(webDavEncPass) }
    var passVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- AUTH ---
            SectionTitle("Authentifizierung")
            if (authMethod == SecurePrefs.AUTH_PIN || authMethod == SecurePrefs.AUTH_BIOMETRIC) {
                OutlinedButton(onClick = { viewModel.clearPin() }, modifier = Modifier.fillMaxWidth()) {
                    Text("PIN entfernen / Keine Authentifizierung")
                }
            }
            if (showPinInput) {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6) newPin = it },
                    label = { Text("Neuer PIN (4-6 Stellen)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (newPin.length in 4..6) {
                            viewModel.setPin(newPin)
                            newPin = ""
                            showPinInput = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("PIN speichern") }
            } else {
                OutlinedButton(
                    onClick = { showPinInput = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (authMethod == SecurePrefs.AUTH_PIN) "PIN ändern" else "PIN festlegen") }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Fingerabdruck aktivieren")
                Switch(checked = biometricEnabled, onCheckedChange = { viewModel.setBiometricEnabled(it) })
            }

            HorizontalDivider()

            // --- WEBDAV ---
            SectionTitle("WebDAV-Synchronisation")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sync aktiviert")
                Switch(checked = syncEnabled, onCheckedChange = { viewModel.setSyncEnabled(it) })
            }
            OutlinedTextField(
                value = webDavUrlInput,
                onValueChange = { webDavUrlInput = it },
                label = { Text("WebDAV Server URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = webDavUserInput,
                onValueChange = { webDavUserInput = it },
                label = { Text("Benutzername") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = webDavPassInput,
                onValueChange = { webDavPassInput = it },
                label = { Text("Passwort") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passVisible = !passVisible }) {
                        Icon(if (passVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                }
            )
            OutlinedTextField(
                value = webDavEncPassInput,
                onValueChange = { webDavEncPassInput = it },
                label = { Text("Verschlüsselungs-Passphrase") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Button(
                onClick = {
                    viewModel.saveWebDavConfig(webDavUrlInput, webDavUserInput, webDavPassInput, webDavEncPassInput)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("WebDAV-Einstellungen speichern") }
            OutlinedButton(
                onClick = { viewModel.syncNow() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Jetzt synchronisieren") }
            when (syncState) {
                is SyncResult.Success -> Text("Sync erfolgreich", color = MaterialTheme.colorScheme.primary)
                is SyncResult.Error -> Text("Fehler: ${(syncState as SyncResult.Error).message}", color = MaterialTheme.colorScheme.error)
                is SyncResult.NotConfigured -> Text("WebDAV nicht konfiguriert", color = MaterialTheme.colorScheme.onSurfaceVariant)
                null -> {}
            }

            HorizontalDivider()

            // --- THEME ---
            SectionTitle("Design")
            val themes = listOf(
                SecurePrefs.THEME_SYSTEM to "Systemstandard",
                SecurePrefs.THEME_LIGHT to "Immer hell",
                SecurePrefs.THEME_DARK to "Immer dunkel"
            )
            themes.forEach { (key, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = themeChoice == key, onClick = { viewModel.setTheme(key) })
                    Spacer(Modifier.width(8.dp))
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}
