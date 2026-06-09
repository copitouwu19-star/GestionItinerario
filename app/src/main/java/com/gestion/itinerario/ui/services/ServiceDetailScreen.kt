package com.gestion.itinerario.ui.services

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.entity.ServiceStatus
import com.gestion.itinerario.ui.dashboard.classifyServiceCategory
import com.gestion.itinerario.ui.dashboard.icon
import com.gestion.itinerario.ui.dashboard.shortLabel
import com.gestion.itinerario.ui.theme.StatusCompleted
import com.gestion.itinerario.ui.theme.StatusInRepair
import com.gestion.itinerario.ui.theme.StatusLowStock
import com.gestion.itinerario.ui.theme.StatusPending
import java.text.SimpleDateFormat
import java.util.*

private val WhatsAppGreen = Color(0xFF25D366)

private fun openWhatsApp(context: android.content.Context, phone: String, message: String = "") {
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

private fun ServiceStatus.toStep(): DetailStep = when (this) {
    ServiceStatus.PENDING     -> DetailStep.PENDIENTE
    ServiceStatus.IN_PROGRESS -> DetailStep.EN_PROCESO
    ServiceStatus.COMPLETED   -> DetailStep.FINALIZADO
}

/**
 * Pantalla de Detalle del Servicio, a pantalla completa, invocada desde la tarjeta
 * de "Próxima Cita" del Dashboard (y reutilizable desde Agenda/Servicios).
 * Acepta una [Appointment]; si existe una orden de servicio asociada para el mismo
 * cliente con fecha cercana, se complementa la vista con diagnóstico, costo y fotos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    appointment: Appointment,
    clientName: String,
    professionalName: String,
    clientPhone: String = "",
    onDismiss: () -> Unit,
    serviceViewModel: ServiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val orders by serviceViewModel.orders.collectAsStateWithLifecycle()

    val relatedOrder = remember(orders, appointment) {
        orders.filter { it.clientId == appointment.clientId }
            .minByOrNull { kotlin.math.abs(it.createdAt - appointment.dateTime) }
            ?.takeIf { kotlin.math.abs(it.createdAt - appointment.dateTime) < 3 * 24 * 60 * 60 * 1000L }
    }

    val currentStep = relatedOrder?.status?.toStep() ?: appointment.status.toStep()
    val category = classifyServiceCategory(appointment.equipmentType, appointment.notes)
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy · hh:mm a", Locale.getDefault()) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { picked ->
            relatedOrder?.let { order ->
                serviceViewModel.update(order.copy(photosAfter = order.photosAfter + picked.toString()))
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Detalle del Servicio", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                            }
                        }
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
                    // ── Stepper de estado ─────────────────────────────────────
                    StatusStepper(currentStep = currentStep)

                    // ── Información General ───────────────────────────────────
                    Card(shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()) {
                                Text("Información General",
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Surface(shape = RoundedCornerShape(50), color = currentStep.color.copy(alpha = 0.15f)) {
                                    Text(currentStep.label.uppercase(),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold, color = currentStep.color)
                                }
                            }
                            HorizontalDivider()
                            InfoRow(icon = Icons.Default.Tag, label = "ID del servicio",
                                value = (relatedOrder?.id ?: appointment.id).take(8).ifBlank { "—" })
                            InfoRow(icon = category.icon, label = "Tipo / Equipo",
                                value = buildString {
                                    append(appointment.serviceType.shortLabel())
                                    if (appointment.equipmentType.isNotBlank()) append(" · ${appointment.equipmentType}")
                                })
                            InfoRow(icon = Icons.Default.CalendarToday, label = "Fecha y hora",
                                value = sdf.format(Date(appointment.dateTime)))
                            InfoRow(icon = Icons.Default.Person, label = "Cliente",
                                value = clientName.ifBlank { "Cliente" })
                            InfoRow(icon = Icons.Default.Engineering, label = "Profesional",
                                value = professionalName)
                        }
                    }

                    // ── Detalles del Trabajo ──────────────────────────────────
                    Card(shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Detalles del Trabajo",
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            HorizontalDivider()
                            val description = relatedOrder?.description?.ifBlank { null }
                                ?: appointment.notes.ifBlank { null }
                                ?: "Sin descripción registrada para este servicio."
                            Text(description, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface)
                            relatedOrder?.diagnosis?.takeIf { it.isNotBlank() }?.let { diag ->
                                Spacer(Modifier.height(2.dp))
                                Text("Diagnóstico", style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(diag, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            relatedOrder?.totalCost?.takeIf { it > 0.0 }?.let { cost ->
                                Spacer(Modifier.height(2.dp))
                                Text("Costo total: $${"%.2f".format(cost)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // ── Progreso Visual (fotos de evidencia) ──────────────────
                    Card(shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Progreso Visual",
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            HorizontalDivider()
                            if (relatedOrder == null) {
                                Text("Las fotos de evidencia estarán disponibles cuando se genere la orden de servicio para esta cita.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                EvidencePhotoRow(title = "Antes", photos = relatedOrder.photosBefore)
                                EvidencePhotoRow(title = "Después", photos = relatedOrder.photosAfter)
                                OutlinedButton(
                                    onClick = { photoLauncher.launch("image/*") },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Adjuntar foto", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    if (clientPhone.isNotBlank()) {
                        Button(
                            onClick = {
                                openWhatsApp(context, clientPhone,
                                    "Hola $clientName, te escribimos sobre tu servicio de ${appointment.serviceType.shortLabel().lowercase()} programado para ${sdf.format(Date(appointment.dateTime))}.")
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
                        Text((index + 1).toString(), color = if (reached) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(step.label, style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal,
                    color = color, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            if (index < steps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.weight(0.6f).padding(bottom = 18.dp),
                    thickness = 2.dp,
                    color = if (index < currentIndex) steps[index + 1].color else MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun EvidencePhotoRow(title: String, photos: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (photos.isEmpty()) {
            Text("Sin fotos adjuntas.", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos) { uri ->
                    SubcomposeAsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { },
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        },
                        error = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.BrokenImage, null, tint = StatusLowStock, modifier = Modifier.size(20.dp))
                            }
                        }
                    )
                }
            }
        }
    }
}
