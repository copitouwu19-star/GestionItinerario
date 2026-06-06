package com.gestion.itinerario.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.gestion.itinerario.data.entity.CompanyProfile
import com.gestion.itinerario.ui.theme.Primary40
import com.gestion.itinerario.ui.theme.Primary80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    innerPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }

    val logoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadLogoAndSave(it) }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uploadState) {
        when (uploadState) {
            is UploadState.Success -> {
                snackbarHostState.showSnackbar("Logo guardado correctamente")
                viewModel.clearUploadState()
            }
            is UploadState.Error -> {
                snackbarHostState.showSnackbar((uploadState as UploadState.Error).message)
                viewModel.clearUploadState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Empresa", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (!editing) {
                        IconButton(onClick = { editing = true }) {
                            Icon(Icons.Default.Edit, null, tint = Primary80)
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(),
                    bottom = maxOf(innerPadding.calculateBottomPadding(), padding.calculateBottomPadding()))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Logo ────────────────────────────────────────────────────────────
            val context = LocalContext.current
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, Primary80.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable { if (uploadState !is UploadState.Loading) logoLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        uploadState is UploadState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.size(40.dp), color = Primary80)
                        }
                        profile.logoUrl.isNotBlank() -> {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(profile.logoUrl)
                                    .crossfade(true)
                                    .memoryCacheKey(profile.logoUrl)
                                    .build(),
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Fit,
                                loading = {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Primary80)
                                },
                                error = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.BrokenImage, null,
                                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                                        Text("Error al cargar",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            )
                        }
                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, null,
                                    tint = Primary80, modifier = Modifier.size(40.dp))
                                Text("Logo empresa", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (uploadState !is UploadState.Loading) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).size(32.dp),
                        shape = CircleShape,
                        color = Primary40
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White,
                            modifier = Modifier.padding(6.dp).clickable { logoLauncher.launch("image/*") })
                    }
                }
            }
            Text(
                if (uploadState is UploadState.Loading) "Subiendo logo…" else "Toca el logo para cambiarlo",
                style = MaterialTheme.typography.labelSmall,
                color = if (uploadState is UploadState.Loading) Primary80
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            HorizontalDivider()

            if (editing) {
                ProfileEditForm(
                    profile = profile,
                    onSave = { updated ->
                        viewModel.save(updated)
                        editing = false
                    },
                    onCancel = { editing = false }
                )
            } else {
                ProfileInfoCard(profile)
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(profile: CompanyProfile) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Datos de la Empresa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ProfileRow(Icons.Default.Business, "Empresa", profile.companyName.ifBlank { "Sin registrar" })
            ProfileRow(Icons.Default.Person, "Propietario", profile.ownerName.ifBlank { "Sin registrar" })
            ProfileRow(Icons.Default.Phone, "Teléfono", profile.phone.ifBlank { "Sin registrar" })
            ProfileRow(Icons.Default.Email, "Correo", profile.email.ifBlank { "Sin registrar" })
            ProfileRow(Icons.Default.LocationOn, "Dirección", profile.address.ifBlank { "Sin registrar" })
            ProfileRow(Icons.Default.Receipt, "Prefijo facturas", "${profile.invoicePrefix}-XXXX")
        }
    }
}

@Composable
private fun ProfileRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = Primary80, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ProfileEditForm(profile: CompanyProfile, onSave: (CompanyProfile) -> Unit, onCancel: () -> Unit) {
    var companyName   by remember { mutableStateOf(profile.companyName) }
    var ownerName     by remember { mutableStateOf(profile.ownerName) }
    var phone         by remember { mutableStateOf(profile.phone) }
    var email         by remember { mutableStateOf(profile.email) }
    var address       by remember { mutableStateOf(profile.address) }
    var invoicePrefix by remember { mutableStateOf(profile.invoicePrefix) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = companyName, onValueChange = { companyName = it },
            label = { Text("Nombre de la empresa") },
            leadingIcon = { Icon(Icons.Default.Business, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = ownerName, onValueChange = { ownerName = it },
            label = { Text("Nombre del propietario") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = phone, onValueChange = { phone = it },
            label = { Text("Teléfono") },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = email, onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = address, onValueChange = { address = it },
            label = { Text("Dirección") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = invoicePrefix, onValueChange = { invoicePrefix = it.uppercase().take(5) },
            label = { Text("Prefijo de facturas (ej: FAC)") },
            leadingIcon = { Icon(Icons.Default.Receipt, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            supportingText = { Text("Se verá como: $invoicePrefix-2026-0001") })
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onCancel) { Text("Cancelar") }
            Button(onClick = {
                onSave(CompanyProfile(companyName = companyName, ownerName = ownerName,
                    phone = phone, email = email, address = address,
                    logoUrl = profile.logoUrl, invoicePrefix = invoicePrefix.ifBlank { "FAC" },
                    invoiceCounter = profile.invoiceCounter))
            }) { Text("Guardar") }
        }
    }
}
