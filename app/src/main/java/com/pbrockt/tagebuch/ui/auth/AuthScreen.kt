package com.pbrockt.tagebuch.ui.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    val maxPin = 6

    LaunchedEffect(state) {
        when (state) {
            is AuthState.Success -> onAuthenticated()
            is AuthState.NoAuth -> onAuthenticated()
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.authMethod == SecurePrefs.AUTH_BIOMETRIC && viewModel.biometricEnabled) {
            showBiometricPrompt(context as FragmentActivity, viewModel::onBiometricSuccess)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Tagebuch", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text("PIN eingeben", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))

        // PIN Dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(maxPin) { i ->
                Surface(
                    shape = CircleShape,
                    color = if (i < pin.length) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(16.dp)
                ) {}
            }
        }

        if (state is AuthState.WrongPin) {
            Spacer(Modifier.height(8.dp))
            Text("Falscher PIN", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }

        Spacer(Modifier.height(32.dp))

        // Numpad
        val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
        keys.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(Modifier.size(72.dp))
                    } else {
                        Button(
                            onClick = {
                                when (key) {
                                    "⌫" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                    else -> {
                                        if (pin.length < maxPin) {
                                            pin += key
                                            if (pin.length == maxPin) viewModel.verifyPin(pin)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            if (key == "⌫") Icon(Icons.Default.Backspace, null, modifier = Modifier.size(20.dp))
                            else Text(key, fontSize = 22.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (viewModel.biometricEnabled) {
            Spacer(Modifier.height(16.dp))
            IconButton(onClick = {
                showBiometricPrompt(context as FragmentActivity, viewModel::onBiometricSuccess)
            }) {
                Icon(Icons.Default.Fingerprint, contentDescription = "Fingerabdruck", modifier = Modifier.size(48.dp))
            }
        }
    }
}

private fun showBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit) {
    val prompt = BiometricPrompt(activity, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess()
        }
    })
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Tagebuch entsperren")
        .setSubtitle("Fingerabdruck verwenden")
        .setNegativeButtonText("PIN verwenden")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()
    prompt.authenticate(info)
}
