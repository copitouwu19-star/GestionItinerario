package com.gestion.itinerario.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val prefs: PreferencesRepository
) : ViewModel() {

    val savedPin: StateFlow<String?> = prefs.pin.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val isPinEnabled: StateFlow<Boolean> = prefs.isPinEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error

    fun onDigit(d: String) {
        if (_pinInput.value.length < 4) _pinInput.value += d
    }

    fun onDelete() {
        if (_pinInput.value.isNotEmpty()) _pinInput.value = _pinInput.value.dropLast(1)
    }

    fun verify(onSuccess: () -> Unit) {
        if (_pinInput.value == savedPin.value) {
            _error.value = false
            onSuccess()
        } else {
            _error.value = true
            _pinInput.value = ""
        }
    }

    fun setNewPin(pin: String, onDone: () -> Unit) = viewModelScope.launch {
        prefs.setPin(pin)
        onDone()
    }

    fun disablePin(onDone: () -> Unit) = viewModelScope.launch {
        prefs.disablePin()
        onDone()
    }

    fun resetError() { _error.value = false }
}
