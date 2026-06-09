package com.gestion.itinerario.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var showReportDialog by remember { mutableStateOf(false) }
    var showPaletteDialog by remember { mutableStateOf(false) }
    var localLogoUri by remember { mutableStateOf<Uri?>(null) }

    val logoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { localLogoUri = it; viewModel.uploadLogoAndSave(it) }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uploadState) {
        when (uploadState) {
            is UploadState.Success -> {
                localLogoUri = null
                snackbarHostState.showSnackbar("Logo guardado correctamente")
                viewModel.clearUploadState()
            }
            is UploadState.Error -> {
                localLogoUri = null
                snackbarHostState.showSnackbar((uploadState as UploadState.Error).message)
                viewModel.clearUploadState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Perfil de Empresa",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    if (!editing) {
                        Surface(
                            onClick = { editing = true },
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit, null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    "EDITAR",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = maxOf(innerPadding.calculateBottomPadding(), padding.calculateBottomPadding())
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Logo ─────────────────────────────────────────────────────────────
            val context = LocalContext.current
            val primaryColor = MaterialTheme.colorScheme.primary

            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(primaryColor.copy(alpha = 0.07f))
                            .drawBehind {
                                drawRoundRect(
                                    color = primaryColor.copy(alpha = 0.35f),
                                    cornerRadius = CornerRadius(20.dp.toPx()),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f))
                                    )
                                )
                            }
                            .clickable { if (uploadState !is UploadState.Loading) logoLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        val logoImageData: Any? = when {
                            localLogoUri != null -> localLogoUri
                            profile.logoUrl.isNotBlank() -> remember(profile.logoUrl) {
                                if (profile.logoUrl.startsWith("data:")) {
                                    val b64 = profile.logoUrl.substringAfter(",")
                                    android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                                } else profile.logoUrl
                            }
                            else -> null
                        }

                        if (logoImageData != null) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(logoImageData)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Fit,
                                loading = {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp), color = Primary80
                                    )
                                },
                                error = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.BrokenImage, null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            "Error al cargar",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )
                            if (uploadState is UploadState.Loading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.Black.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(36.dp), color = Color.White
                                    )
                                }
                            }
                        } else if (uploadState is UploadState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp), color = Primary80
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Business, null,
                                    tint = primaryColor.copy(alpha = 0.55f),
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                    }

                    if (uploadState !is UploadState.Loading) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp),
                            shape = CircleShape,
                            color = Primary40,
                            shadowElevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { logoLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt, null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    if (uploadState is UploadState.Loading) "SUBIENDO LOGO…" else "TAP TO CHANGE LOGO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (uploadState is UploadState.Loading) Primary80
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Acciones rápidas (Reporte + Paleta en una sola tarjeta) ──────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    QuickSettingsRow(
                        icon = Icons.Default.Summarize,
                        iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Reporte mensual",
                        subtitle = "Genera un PDF con ingresos del mes",
                        onClick = { showReportDialog = true }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    QuickSettingsRow(
                        icon = Icons.Default.Palette,
                        iconBgColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = "Paleta de colores",
                        subtitle = "Elige el tema de colores de la app",
                        onClick = { showPaletteDialog = true }
                    )
                }
            }

            // ── Datos / Formulario ───────────────────────────────────────────────
            if (editing) {
                ProfileEditForm(
                    profile = profile,
                    onSave = { updated -> viewModel.save(updated); editing = false },
                    onCancel = { editing = false }
                )
            } else {
                ProfileInfoCard(profile)
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showReportDialog) {
        com.gestion.itinerario.ui.reports.MonthlyReportDialog(onDismiss = { showReportDialog = false })
    }
    if (showPaletteDialog) {
        com.gestion.itinerario.ui.theme.ColorPaletteDialog(onDismiss = { showPaletteDialog = false })
    }
}

// ─── Fila de acción rápida (Reporte / Paleta) ─────────────────────────────────
@Composable
private fun QuickSettingsRow(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.ChevronRight, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ─── Tarjeta de información de la empresa ─────────────────────────────────────
@Composable
private fun ProfileInfoCard(profile: CompanyProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título + ícono info
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Datos de la Empresa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.Default.Info, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            val rows = listOf(
                Triple(Icons.Default.Business,    "EMPRESA",                   profile.companyName.ifBlank { "Sin registrar" }),
                Triple(Icons.Default.Badge,        "NIT / IDENTIFICACIÓN FISCAL", profile.taxId.ifBlank { "Sin registrar" }),
                Triple(Icons.Default.Person,       "PROPIETARIO",               profile.ownerName.ifBlank { "Sin registrar" }),
                Triple(Icons.Default.Phone,        "TELÉFONO",                  profile.phone.ifBlank { "Sin registrar" }),
                Triple(Icons.Default.Email,        "CORREO",                    profile.email.ifBlank { "Sin registrar" }),
                Triple(Icons.Default.LocationOn,   "DIRECCIÓN",                 profile.address.ifBlank { "Sin registrar" }),
                Triple(Icons.Default.Receipt,      "PREFIJO FACTURAS",          "${profile.invoicePrefix}-XXXX")
            )

            rows.forEachIndexed { idx, (icon, label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (idx < rows.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

// ─── Formulario de edición ────────────────────────────────────────────────────
@Composable
private fun ProfileEditForm(profile: CompanyProfile, onSave: (CompanyProfile) -> Unit, onCancel: () -> Unit) {
    var companyName   by remember { mutableStateOf(profile.companyName) }
    var taxId         by remember { mutableStateOf(profile.taxId) }
    var ownerName     by remember { mutableStateOf(profile.ownerName) }
    var phone         by remember { mutableStateOf(profile.phone) }
    var email         by remember { mutableStateOf(profile.email) }
    var address       by remember { mutableStateOf(profile.address) }
    var invoicePrefix by remember { mutableStateOf(profile.invoicePrefix) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Editar Datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = companyName, onValueChange = { companyName = it },
                label = { Text("Nombre de la empresa") },
                leadingIcon = { Icon(Icons.Default.Business, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = taxId, onValueChange = { taxId = it },
                label = { Text("NIT / Identificación fiscal") },
                leadingIcon = { Icon(Icons.Default.Badge, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                supportingText = { Text("Aparecerá en el encabezado de tus facturas") }
            )
            OutlinedTextField(
                value = ownerName, onValueChange = { ownerName = it },
                label = { Text("Nombre del propietario") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("Teléfono") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = address, onValueChange = { address = it },
                label = { Text("Dirección") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = invoicePrefix, onValueChange = { invoicePrefix = it.uppercase().take(5) },
                label = { Text("Prefijo de facturas (ej: FAC)") },
                leadingIcon = { Icon(Icons.Default.Receipt, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                supportingText = { Text("Se verá como: $invoicePrefix-2026-0001") }
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onCancel) { Text("Cancelar") }
                Button(onClick = {
                    onSave(
                        CompanyProfile(
                            companyName    = companyName,
                            taxId          = taxId,
                            ownerName      = ownerName,
                            phone          = phone,
                            email          = email,
                            address        = address,
                            logoUrl        = profile.logoUrl,
                            invoicePrefix  = invoicePrefix.ifBlank { "FAC" },
                            invoiceCounter = profile.invoiceCounter
                        )
                    )
                }) { Text("Guardar") }
            }
        }
    }
}
