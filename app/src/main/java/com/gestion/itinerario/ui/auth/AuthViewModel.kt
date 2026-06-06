package com.gestion.itinerario.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.repository.UsuarioMySQLRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val usuarioRepository: UsuarioMySQLRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                val partes = (user.displayName ?: "").trim().split(" ", limit = 2)
                usuarioRepository.sincronizarUsuario(
                    firebaseUid = user.uid,
                    nombre      = partes.getOrElse(0) { email.substringBefore('@') },
                    apellido    = partes.getOrElse(1) { "" },
                    email       = user.email ?: email
                )
            }
            _uiState.value = AuthUiState.Success
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error(e.toFriendlyMessage())
        }
    }

    fun register(nombre: String, apellido: String, email: String, password: String) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName("$nombre $apellido")
                    .build()
            )?.await()
            result.user?.let { user ->
                usuarioRepository.sincronizarUsuario(
                    firebaseUid = user.uid,
                    nombre      = nombre,
                    apellido    = apellido,
                    email       = email
                )
            }
            _uiState.value = AuthUiState.Success
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error(e.toFriendlyMessage())
        }
    }

    fun sendPasswordReset(email: String) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        try {
            auth.sendPasswordResetEmail(email).await()
            _uiState.value = AuthUiState.Success
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error(e.toFriendlyMessage())
        }
    }

    fun logout() {
        auth.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun deleteAccount() = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        try {
            auth.currentUser?.delete()?.await()
            _uiState.value = AuthUiState.Success
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error(e.toFriendlyMessage())
        }
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }

    private fun Exception.toFriendlyMessage(): String = when {
        this is FirebaseAuthUserCollisionException ->
            "Ese correo ya está registrado. Por favor iniciá sesión."
        this is FirebaseAuthInvalidUserException ->
            "El correo ingresado no está registrado. Por favor registrate."
        message?.contains("badly formatted", ignoreCase = true) == true ->
            "El formato del correo no es válido."
        message?.contains("password is invalid", ignoreCase = true) == true ->
            "La contraseña es incorrecta."
        message?.contains("no user record", ignoreCase = true) == true ->
            "El correo ingresado no está registrado. Por favor registrate."
        message?.contains("already in use", ignoreCase = true) == true ->
            "Ese correo ya está registrado. Por favor iniciá sesión."
        message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true ->
            "Correo o contraseña incorrectos."
        message?.contains("network error", ignoreCase = true) == true ->
            "Sin conexión a internet."
        message?.contains("too-many-requests", ignoreCase = true) == true ->
            "Demasiados intentos. Intentá más tarde."
        message?.contains("requires-recent-login", ignoreCase = true) == true ->
            "Cerrá sesión y volvé a entrar para realizar esta acción."
        message?.contains("configuration", ignoreCase = true) == true ->
            "Firebase Auth no está activado en el proyecto."
        else -> "Error: ${this.javaClass.simpleName} — ${message?.take(80) ?: "sin detalle"}"
    }
}
