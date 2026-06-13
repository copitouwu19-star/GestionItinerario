package com.gestion.itinerario.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.CompanyProfile

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ── Campos de cuenta ──────────────────────────────────────────────────────
    var nombre          by remember { mutableStateOf("") }
    var apellido        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }

    // ── Campos de empresa ─────────────────────────────────────────────────────
    var companyName          by remember { mutableStateOf("") }
    var taxId                by remember { mutableStateOf("") }
    var ownerName            by remember { mutableStateOf("") }
    var ownerNameEditedManually by remember { mutableStateOf(false) }
    var phone                by remember { mutableStateOf("") }
    var companyEmail         by remember { mutableStateOf("") }
    var companyEmailEditedManually by remember { mutableStateOf(false) }
    var address              by remember { mutableStateOf("") }

    // ── Errores de cuenta ─────────────────────────────────────────────────────
    var nombreError      by remember { mutableStateOf("") }
    var apellidoError    by remember { mutableStateOf("") }
    var emailError       by remember { mutableStateOf("") }
    var passwordError    by remember { mutableStateOf("") }
    var confirmError     by remember { mutableStateOf("") }

    // ── Errores de empresa ────────────────────────────────────────────────────
    var companyNameError  by remember { mutableStateOf("") }
    var taxIdError        by remember { mutableStateOf("") }
    var ownerNameError    by remember { mutableStateOf("") }
    var phoneError        by remember { mutableStateOf("") }
    var companyEmailError by remember { mutableStateOf("") }
    var addressError      by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is AuthUiState.Success -> { viewModel.resetState(); onRegisterSuccess() }
            is AuthUiState.Error   -> {
                if (s.message.contains("ya registrado", ignoreCase = true)) {
                    emailError = s.message
                    viewModel.resetState()
                }
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        Icon(
            Icons.Default.PersonAdd, null,
            modifier = Modifier.size(52.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text("Crear cuenta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Completá tus datos para registrarte",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        // ── Sección 1: Datos de tu cuenta ─────────────────────────────────────
        Text(
            "DATOS DE TU CUENTA",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { v ->
                if (v.all { c -> c.isLetter() || c == ' ' }) {
                    nombre = v
                    nombreError = ""
                    if (!ownerNameEditedManually)
                        ownerName = "${v.trim()} ${apellido.trim()}".trim()
                }
            },
            label = { Text("Nombre *") },
            isError = nombreError.isNotEmpty(),
            supportingText = { if (nombreError.isNotEmpty()) Text(nombreError) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = apellido,
            onValueChange = { v ->
                if (v.all { c -> c.isLetter() || c == ' ' }) {
                    apellido = v
                    apellidoError = ""
                    if (!ownerNameEditedManually)
                        ownerName = "${nombre.trim()} ${v.trim()}".trim()
                }
            },
            label = { Text("Apellido *") },
            isError = apellidoError.isNotEmpty(),
            supportingText = { if (apellidoError.isNotEmpty()) Text(apellidoError) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { v ->
                email = v
                emailError = ""
                if (!companyEmailEditedManually) companyEmail = v
            },
            label = { Text("Correo electrónico *") },
            isError = emailError.isNotEmpty(),
            supportingText = { if (emailError.isNotEmpty()) Text(emailError) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordError = "" },
            label = { Text("Contraseña *") },
            isError = passwordError.isNotEmpty(),
            supportingText = { if (passwordError.isNotEmpty()) Text(passwordError) else Text("Mínimo 6 caracteres") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmError = "" },
            label = { Text("Confirmar contraseña *") },
            isError = confirmError.isNotEmpty(),
            supportingText = { if (confirmError.isNotEmpty()) Text(confirmError) },
            singleLine = true,
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                    Icon(if (confirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // ── Sección 2: Datos de la empresa ────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "DATOS DE LA EMPRESA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                "Todos los campos son obligatorios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it; companyNameError = "" },
            label = { Text("Nombre de la empresa *") },
            isError = companyNameError.isNotEmpty(),
            supportingText = { if (companyNameError.isNotEmpty()) Text(companyNameError) },
            leadingIcon = { Icon(Icons.Default.Business, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = taxId,
            onValueChange = { taxId = it; taxIdError = "" },
            label = { Text("RIF / Registro Único de Información Fiscal *") },
            isError = taxIdError.isNotEmpty(),
            supportingText = { if (taxIdError.isNotEmpty()) Text(taxIdError) },
            leadingIcon = { Icon(Icons.Default.Badge, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = ownerName,
            onValueChange = { ownerName = it; ownerNameEditedManually = true; ownerNameError = "" },
            label = { Text("Nombre del propietario *") },
            isError = ownerNameError.isNotEmpty(),
            supportingText = { if (ownerNameError.isNotEmpty()) Text(ownerNameError) },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; phoneError = "" },
            label = { Text("Teléfono *") },
            isError = phoneError.isNotEmpty(),
            supportingText = { if (phoneError.isNotEmpty()) Text(phoneError) },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = companyEmail,
            onValueChange = { companyEmail = it; companyEmailEditedManually = true; companyEmailError = "" },
            label = { Text("Correo de la empresa *") },
            isError = companyEmailError.isNotEmpty(),
            supportingText = { if (companyEmailError.isNotEmpty()) Text(companyEmailError) },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it; addressError = "" },
            label = { Text("Dirección *") },
            isError = addressError.isNotEmpty(),
            supportingText = { if (addressError.isNotEmpty()) Text(addressError) },
            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState is AuthUiState.Error) {
            Spacer(Modifier.height(8.dp))
            Text(
                (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                var valid = true
                val campo = "Campo obligatorio"

                // Validar cuenta
                if (nombre.isBlank())    { nombreError = campo;                      valid = false }
                if (apellido.isBlank())  { apellidoError = campo;                    valid = false }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                    emailError = "Ingresá un correo válido.";                        valid = false
                }
                if (password.length < 6) { passwordError = "Mínimo 6 caracteres."; valid = false }
                if (password != confirmPassword) {
                    confirmError = "Las contraseñas no coinciden.";                  valid = false
                }

                // Validar empresa
                if (companyName.isBlank())  { companyNameError  = campo; valid = false }
                if (taxId.isBlank())        { taxIdError        = campo; valid = false }
                if (ownerName.isBlank())    { ownerNameError    = campo; valid = false }
                if (phone.isBlank())        { phoneError        = campo; valid = false }
                if (companyEmail.isBlank()) { companyEmailError = campo; valid = false }
                if (address.isBlank())      { addressError      = campo; valid = false }

                if (valid) {
                    viewModel.register(
                        nombre   = nombre.trim(),
                        apellido = apellido.trim(),
                        email    = email.trim(),
                        password = password,
                        company  = CompanyProfile(
                            companyName = companyName.trim(),
                            taxId       = taxId.trim(),
                            ownerName   = ownerName.trim(),
                            phone       = phone.trim(),
                            email       = companyEmail.trim(),
                            address     = address.trim()
                        )
                    )
                }
            },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Crear cuenta", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tenés cuenta?", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onGoToLogin) { Text("Iniciar sesión") }
        }
        Spacer(Modifier.height(24.dp))
    }
}
