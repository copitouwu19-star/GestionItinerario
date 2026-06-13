package com.gestion.itinerario.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.ui.auth.AuthUiState
import com.gestion.itinerario.ui.auth.AuthViewModel

@Composable
fun UserMenuIconButton(
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu          by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val authVm: AuthViewModel = hiltViewModel()
    val authState by authVm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            onLogout()
        }
    }

    Box(modifier = modifier) {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Menú usuario",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Perfil de empresa") },
                leadingIcon = { Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary) },
                onClick = { showMenu = false; onNavigateToProfile() }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Cerrar sesión") },
                leadingIcon = { Icon(Icons.Default.Logout, null) },
                onClick = { showMenu = false; onLogout() }
            )
            DropdownMenuItem(
                text = { Text("Eliminar cuenta", color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                onClick = { showMenu = false; showDeleteConfirm = true }
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar cuenta", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás segura de que querés eliminar tu cuenta? Esta acción no se puede deshacer y perderás todos tus datos.") },
            confirmButton = {
                Button(
                    onClick = { authVm.deleteAccount() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}
