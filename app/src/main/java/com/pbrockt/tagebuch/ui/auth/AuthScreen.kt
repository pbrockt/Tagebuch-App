package com.pbrockt.tagebuch.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pbrockt.tagebuch.ui.theme.FloralBackground

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var pin by remember { mutableStateOf("") }
    val maxPin = 4

    LaunchedEffect(state) {
        when (state) {
            is AuthState.Success, is AuthState.NoAuth -> onAuthenticated()
            is AuthState.WrongPin -> pin = ""
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FloralBackground()
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Tagebuch", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text("PIN eingeben", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))

            // 4 PIN dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(maxPin) { i ->
                    Surface(
                        shape = CircleShape,
                        color = if (i < pin.length) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(18.dp)
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
                                        else -> if (pin.length < maxPin) {
                                            pin += key
                                            if (pin.length == maxPin) viewModel.verifyPin(pin)
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
                                if (key == "⌫") Icon(Icons.Default.Backspace, null, Modifier.size(20.dp))
                                else Text(key, fontSize = 22.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
