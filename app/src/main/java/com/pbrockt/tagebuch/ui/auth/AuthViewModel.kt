package com.pbrockt.tagebuch.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Success : AuthState()
    data class WrongPin(val attemptsLeft: Int) : AuthState()
    data class LockedOut(val secondsLeft: Int) : AuthState()
    object NoAuth : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(private val prefs: SecurePrefs) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    private var attemptCount = 0
    private var lockedUntil = 0L
    private var countdownJob: Job? = null

    init {
        if (prefs.authMethod == SecurePrefs.AUTH_NONE) {
            _state.value = AuthState.NoAuth
        }
    }

    fun verifyPin(pin: String) {
        val now = System.currentTimeMillis()
        if (now < lockedUntil) {
            startCountdown()
            return
        }

        if (sha256(pin) == prefs.pinHash) {
            attemptCount = 0
            _state.value = AuthState.Success
        } else {
            attemptCount++
            if (attemptCount >= 5) {
                lockedUntil = now + 30_000L
                attemptCount = 0
                startCountdown()
            } else {
                _state.value = AuthState.WrongPin(5 - attemptCount)
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val remaining = ((lockedUntil - System.currentTimeMillis()) / 1000).toInt()
                if (remaining <= 0) {
                    _state.value = AuthState.Idle
                    break
                }
                _state.value = AuthState.LockedOut(remaining)
                delay(1000)
            }
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
