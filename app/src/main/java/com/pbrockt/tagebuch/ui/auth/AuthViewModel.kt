package com.pbrockt.tagebuch.ui.auth

import androidx.lifecycle.ViewModel
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.MessageDigest
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Success : AuthState()
    object WrongPin : AuthState()
    object NoAuth : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(private val prefs: SecurePrefs) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    init {
        if (prefs.authMethod == SecurePrefs.AUTH_NONE) {
            _state.value = AuthState.NoAuth
        }
    }

    fun verifyPin(pin: String) {
        val hash = sha256(pin)
        _state.value = if (hash == prefs.pinHash) AuthState.Success else AuthState.WrongPin
        if (_state.value is AuthState.WrongPin) {
            // Reset after short delay so dots clear
            _state.value = AuthState.WrongPin
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
