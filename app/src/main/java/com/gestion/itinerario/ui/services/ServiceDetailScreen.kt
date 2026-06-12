package com.gestion.itinerario.ui.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.ui.dashboard.classifyServiceCategory
import com.gestion.itinerario.ui.dashboard.icon
import com.gestion.itinerario.ui.dashboard.shortLabel
import com.gestion.itinerario.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val WhatsAppGreen = Color(0xFF25D366)

private fun openWhatsApp(context: Context, phone: String, message: String = "") {
    if (phone.isBlank()) return
    val cleaned = phone.trimStart('+').let { if (it.startsWith("0")) "58${it.drop(1)}" else it }
    val url = "https://wa.me/$cleaned${if (message.isNotBlank()) "?text=${Uri.encode(message)}" else ""}"
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

private enum class DetailStep(val label: String, val color: Color) {
    PENDIENTE("Pendiente", StatusPending),
    EN_PROCESO("En Proceso", StatusInRepair),
    FINALIZADO("Finalizado", StatusCompleted)
}

private fun AppointmentStatus.toStep(): DetailStep = when (this) {
    AppointmentStatus.SCHEDULED, AppointmentStatus.CANCELLED -> DetailStep.PENDIENTE
    AppointmentStatus.IN_PROGRESS -> DetailStep.EN_PROCESO
    AppointmentStatus.COMPLETED   -> DetailStep.FINALIZADO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    appointment: Appointment,
    clientName: String,
    professionalName: String,
    clientPhone: String = "",
    onDismiss: () -> Unit,
    onCompleted: ((Appointment) -> Unit)? = null,
    serviceViewModel: ServiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val allAppointments by serviceViewModel.allAppointments.collectAsStateWithLifecycle()
    val liveAppt = allAppointments.firstOrNull { it.id == appointment.id } ?: appointment

    val currentStep = liveAppt.status.toStep()
    val category = classifyServiceCategory(liveAppt.equipmentType, liveAppt.notes)
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy · hh:mm a", Locale.getDefault()) }

    var currentPhotoTarget by remember { mutableStateOf("") }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var fullscreenPhoto by remember { mutableStateOf<String?>(null) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    var confirmTitle   by remember { mutableStateOf("") }
    var confirmMessage by remember { mutableStateOf("") }
    var confirmAction  by remember { mutableStateOf<(() -> Unit)?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { picked ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    picked, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val latest = serviceViewModel.allAppointments.value.firstOrNull { it.id == appointment.id } ?: return@let
            val uriStr = picked.toString()
            val updated = when (currentPhotoTarget) {
                "before" -> latest.copy(photosBefore = latest.photosBefore + uriStr)
                "during" -> latest.copy(photosDuring = latest.photosDuring + uriStr)
                "after"  -> latest.copy(photosAfter  = latest.photosAfter  + uriStr)
                else -> return@let
            }
            serviceViewModel.updateAppointment(updated)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val uriStr = cameraPhotoUri?.toString() ?: return@rememberLauncherForActivityResult
            val latest = serviceViewModel.allAppointments.value.firstOrNull { it.id == appointment.id }
                ?: return@rememberLauncherForActivityResult
            val updated = when (currentPhotoTarget) {
                "before" -> latest.copy(photosBefore = latest.photosBefore + uriStr)
                "during" -> latest.copy(photosDuring = latest.photosDuring + uriStr)
                "after"  -> latest.copy(photosAfter  = latest.photosAfter  + uriStr)
                else -> return@rememberLauncherForActivityResult
            }
            serviceViewModel.updateAppointment(updated)
        }
    }

    fun launchCamera(phase: String) {
        val photoFile = File(context.cacheDir, "appt_${System.currentTimeMillis()}.jpg")
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.provider", photoFile
        )
        currentPhotoTarget = phase
        cameraPhotoUri = uri
        cameraLauncher.launch(uri)
    }

    fun launchGallery(phase: String) {
        currentPhotoTarget = phase
        galleryLauncher.launch("image/*")
    }

    // Diálogo de confirmación antes de completar — pregunta si añadir fotos finales
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            containerColor = Color.White,
            tonalElevation = 0.dp,
            icon = { Icon(Icons.Default.CameraAlt, null, tint = Primary40) },
            title = { Text("¿Añadir fotos antes de finalizar?", fontWeight = FontWeight.Bold) },
            text  = { Text("¿Deseas añadir fotos del resultado final del servicio (\"Después\") antes de generar la factura?") },
            confirmButton = {
                Button(
                    onClick = {
                        showCompleteDialog = false
                        // Abre directamente el selector de fotos en la fase "after"
                        currentPhotoTarget = "after"
                        showPhotoSourceDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary40)
                ) { Text("Añadir fotos") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCompleteDialog = false
                    serviceViewModel.completeAppointment(liveAppt)
                    onCompleted?.invoke(liveAppt)
                }) { Text("Ir a factura") }
            }
        )
    }

    if (confirmAction != null) {
        AlertDialog(
            onDismissRequest = { confirmAction = null },
            title = { Text(confirmTitle, fontWeight = FontWeight.Bold) },
            text  = { Text(confirmMessage) },
            confirmButton = {
                Button(onClick = { confirmAction?.invoke(); confirmAction = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary40)) {
                    Text("Confirmar")
                }
            },
            dismissButton = { TextButton(onClick = { confirmAction = null }) { Text("Cancelar") } }
        )
    }

    // Visor de foto a pantalla completa
    fullscreenPhoto?.let { uri ->
        Dialog(
            onDismissRequest = { fullscreenPhoto = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { fullscreenPhoto = null },
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    loading = {
                        CircularProgressIndicator(color = Color.White)
                    },
                    error = {
                        Icon(Icons.Default.BrokenImage, null,
                            tint = Color.White, modifier = Modifier.size(64.dp))
                    }
                )
                IconButton(
                    onClick = { fullscreenPhoto = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            containerColor = Color.White,
            tonalElevation = 0.dp,
            title = { Text("Agregar foto", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val phaseLabel = when (currentPhotoTarget) {
                        "before" -> "Antes"
                        "during" -> "Durante"
                        else     -> "Después"
                    }
                    Text("Fuente para foto «$phaseLabel»:",
                        style = MaterialTheme.typography.bodyMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { showPhotoSourceDialog = false; launchCamera(currentPhotoTarget) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Cámara")
                        }
                        Button(
                            onClick = { showPhotoSourceDialog = false; launchGallery(currentPhotoTarget) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary40)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Galería")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showPhotoSourceDialog = false }) { Text("Cancelar") } }
        )
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text("Detalle del Servicio", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Stepper ───────────────────────────────────────────────
                    StatusStepper(currentStep = currentStep)

                    // ── Información General ───────────────────────────────────
                    Card(
                        shape  = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Información General",
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Surface(shape = RoundedCornerShape(50), color = currentStep.color.copy(alpha = 0.15f)) {
                                    Text(
                                        currentStep.label.uppercase(),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold, color = currentStep.color
                                    )
                                }
                            }
                            HorizontalDivider()
                            InfoRow(Icons.Default.Tag, "ID del servicio",
                                liveAppt.id.take(8).ifBlank { "—" })
                            InfoRow(category.icon, "Tipo / Equipo", buildString {
                                append(liveAppt.serviceType.shortLabel())
                                if (liveAppt.equipmentType.isNotBlank()) append(" · ${liveAppt.equipmentType}")
                            })
                            InfoRow(Icons.Default.CalendarToday, "Fecha y hora",
                                sdf.format(Date(liveAppt.dateTime)))
                            InfoRow(Icons.Default.Person, "Cliente",
                                clientName.ifBlank { "Cliente" })
                            InfoRow(Icons.Default.Engineering, "Profesional", professionalName)
                        }
                    }

                    // ── Detalles del Trabajo (solo si hay descripción real, sin nombre de cliente) ──
                    val workDescription = liveAppt.notes.substringAfter(" — ", "")
                    if (workDescription.isNotBlank()) {
                        Card(
                            shape  = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Detalles del Trabajo",
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                HorizontalDivider()
                                Text(workDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    // ── Progreso Visual ───────────────────────────────────────
                    Card(
                        shape  = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Progreso Visual",
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            HorizontalDivider()
                            PhotoPhaseSection(
                                title        = "Antes",
                                description  = "Fotos antes de iniciar el servicio",
                                photos       = liveAppt.photosBefore,
                                canAdd       = liveAppt.status == AppointmentStatus.SCHEDULED,
                                onAddPhoto   = { currentPhotoTarget = "before"; showPhotoSourceDialog = true },
                                onPhotoClick = { uri -> fullscreenPhoto = uri }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                            PhotoPhaseSection(
                                title        = "Durante",
                                description  = "Fotos durante el proceso",
                                photos       = liveAppt.photosDuring,
                                canAdd       = liveAppt.status == AppointmentStatus.IN_PROGRESS,
                                onAddPhoto   = { currentPhotoTarget = "during"; showPhotoSourceDialog = true },
                                onPhotoClick = { uri -> fullscreenPhoto = uri }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                            PhotoPhaseSection(
                                title        = "Después",
                                description  = "Fotos al finalizar el servicio",
                                photos       = liveAppt.photosAfter,
                                canAdd       = liveAppt.status == AppointmentStatus.COMPLETED,
                                onAddPhoto   = { currentPhotoTarget = "after"; showPhotoSourceDialog = true },
                                onPhotoClick = { uri -> fullscreenPhoto = uri }
                            )
                        }
                    }

                    // ── Botones de transición de fase ─────────────────────────
                    if (liveAppt.status != AppointmentStatus.CANCELLED) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            when (liveAppt.status) {
                                AppointmentStatus.SCHEDULED -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Brush.linearGradient(listOf(Primary40, Secondary40)))
                                            .clickable { serviceViewModel.startAppointment(liveAppt) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            Text("Iniciar Servicio", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                AppointmentStatus.IN_PROGRESS -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Brush.linearGradient(listOf(Primary40, Secondary40)))
                                            .clickable { showCompleteDialog = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            Text("Completar Servicio", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            confirmTitle   = "Volver a Pendiente"
                                            confirmMessage = "¿Regresar esta cita a estado Pendiente? Podrás agregar más fotos de «Antes»."
                                            confirmAction  = { serviceViewModel.updateAppointment(liveAppt.copy(status = AppointmentStatus.SCHEDULED)) }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(46.dp),
                                        shape  = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.4f))
                                    ) {
                                        Icon(Icons.Default.Undo, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Volver a Pendiente")
                                    }
                                }
                                AppointmentStatus.COMPLETED -> {
                                    OutlinedButton(
                                        onClick = {
                                            confirmTitle   = "Volver a En Proceso"
                                            confirmMessage = "¿Regresar esta cita a En Proceso? Podrás agregar más fotos del proceso."
                                            confirmAction  = { serviceViewModel.updateAppointment(liveAppt.copy(status = AppointmentStatus.IN_PROGRESS)) }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(46.dp),
                                        shape  = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.4f))
                                    ) {
                                        Icon(Icons.Default.Undo, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Volver a En Proceso")
                                    }
                                }
                                else -> {}
                            }
                        }
                    }

                    // ── Botón WhatsApp ────────────────────────────────────────
                    if (clientPhone.isNotBlank()) {
                        Button(
                            onClick = {
                                openWhatsApp(
                                    context, clientPhone,
                                    "Hola $clientName, te escribimos sobre tu servicio de " +
                                    "${liveAppt.serviceType.shortLabel().lowercase()} " +
                                    "programado para ${sdf.format(Date(liveAppt.dateTime))}."
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                        ) {
                            Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Contactar cliente por WhatsApp", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PhotoPhaseSection(
    title: String,
    description: String,
    photos: List<String>,
    canAdd: Boolean,
    onAddPhoto: () -> Unit,
    onPhotoClick: (String) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title,
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (canAdd) {
                TextButton(
                    onClick = onAddPhoto,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Agregar", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        if (photos.isEmpty()) {
            Text("Sin fotos adjuntas.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos) { uri ->
                    SubcomposeAsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onPhotoClick(uri) },
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        },
                        error = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.BrokenImage, null,
                                    tint = StatusLowStock, modifier = Modifier.size(20.dp))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusStepper(currentStep: DetailStep) {
    val steps = DetailStep.values()
    val currentIndex = steps.indexOf(currentStep)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { index, step ->
            val reached = index <= currentIndex
            val color = if (reached) step.color else MaterialTheme.colorScheme.outlineVariant

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (reached) step.color else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < currentIndex) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            (index + 1).toString(),
                            color = if (reached) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    step.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal,
                    color = color,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            if (index < steps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.weight(0.6f).padding(bottom = 18.dp),
                    thickness = 2.dp,
                    color = if (index < currentIndex) steps[index + 1].color
                            else MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column {
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
