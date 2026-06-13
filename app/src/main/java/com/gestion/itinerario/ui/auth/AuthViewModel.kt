package com.gestion.itinerario.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.CompanyProfile
import com.gestion.itinerario.data.repository.ProfileRepository
import com.gestion.itinerario.data.repository.UsuarioMySQLRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val usuarioRepository: UsuarioMySQLRepository,
    private val profileRepository: ProfileRepository
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

    fun register(
        nombre: String,
        apellido: String,
        email: String,
        password: String,
        company: CompanyProfile
    ) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName("$nombre $apellido")
                    .build()
            )?.await()
            result.user?.let { user ->
                // Sincronización MySQL: máx 5 s, si no responde continúa igual
                try {
                    withTimeoutOrNull(5_000L) {
                        usuarioRepository.sincronizarUsuario(
                            firebaseUid = user.uid,
                            nombre      = nombre,
                            apellido    = apellido,
                            email       = email
                        )
                    }
                } catch (_: Exception) {}
            }
            // Guarda empresa en Firestore: máx 4 s, si Firestore tarda lo sincroniza en background
            try {
                withTimeoutOrNull(4_000L) { profileRepository.save(company) }
            } catch (_: Exception) {}
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

    private fun Exception.toFriendlyMessage(): String {
        // Firebase BOM 33+ usa errorCode en la clase base FirebaseAuthException.
        // En SDK 23+ ya no lanzan subclases específicas para sign-in; todo va por errorCode.
        val code = (this as? FirebaseAuthException)?.errorCode ?: ""
        return when {
            // ── Correo ya registrado ───────────────────────────────────────────
            this is FirebaseAuthUserCollisionException ||
            code == "ERROR_EMAIL_ALREADY_IN_USE" ||
            message?.contains("already in use", ignoreCase = true) == true ->
                "Usuario ya registrado"

            // ── Usuario no existe ──────────────────────────────────────────────
            this is FirebaseAuthInvalidUserException ||
            code == "ERROR_USER_NOT_FOUND" ||
            message?.contains("no user record", ignoreCase = true) == true ->
                "Usuario no registrado"

            // ── Contraseña incorrecta ──────────────────────────────────────────
            code == "ERROR_WRONG_PASSWORD" ||
            (this is FirebaseAuthInvalidCredentialsException && code != "ERROR_INVALID_CREDENTIAL") ||
            message?.contains("password is invalid", ignoreCase = true) == true ->
                "Contraseña inválida"

            // ── Credencial inválida genérica (SDK 23+: cubre contraseña/usuario) ─
            code == "ERROR_INVALID_CREDENTIAL" ||
            message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true ||
            message?.contains("invalid credential", ignoreCase = true) == true ->
                "Correo o contraseña incorrectos"

            // ── Correo mal escrito ─────────────────────────────────────────────
            code == "ERROR_INVALID_EMAIL" ||
            message?.contains("badly formatted", ignoreCase = true) == true ->
                "Correo electrónico inválido"

            // ── Contraseña débil ───────────────────────────────────────────────
            code == "ERROR_WEAK_PASSWORD" ||
            message?.contains("weak password", ignoreCase = true) == true ->
                "La contraseña es muy débil (mínimo 6 caracteres)"

            // ── Sin internet ───────────────────────────────────────────────────
            code == "ERROR_NETWORK_REQUEST_FAILED" ||
            message?.contains("network", ignoreCase = true) == true ->
                "Sin conexión a internet"

            // ── Demasiados intentos ────────────────────────────────────────────
            code == "ERROR_TOO_MANY_REQUESTS" ||
            message?.contains("too-many-requests", ignoreCase = true) == true ->
                "Demasiados intentos. Intentá más tarde"

            // ── Re-autenticación requerida ─────────────────────────────────────
            code == "ERROR_REQUIRES_RECENT_LOGIN" ||
            message?.contains("requires-recent-login", ignoreCase = true) == true ->
                "Cerrá sesión y volvé a entrar para realizar esta acción"

            else -> "Error inesperado. Intentá de nuevo"
        }
    }
}
