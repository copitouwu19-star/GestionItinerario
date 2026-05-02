package com.gestion.itinerario.ui.services

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.*
import com.gestion.itinerario.ui.clients.ClientViewModel
import com.gestion.itinerario.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: ServiceViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editOrder by remember { mutableStateOf<ServiceOrder?>(null) }
    var filterStatus by remember { mutableStateOf<ServiceStatus?>(null) }

    val filtered = if (filterStatus != null) orders.filter { it.status == filterStatus } else orders

    Scaffold(
        topBar = { TopAppBar(title = { Text("Órdenes de Servicio", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editOrder = null; showDialog = true }, containerColor = Primary40) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(
            top = padding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding()
        )) {
            // Filter chips
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = filterStatus == null, onClick = { filterStatus = null }, label = { Text("Todos") })
                ServiceStatus.values().forEach { s ->
                    FilterChip(selected = filterStatus == s, onClick = { filterStatus = if (filterStatus == s) null else s },
                        label = { Text(s.displayName()) })
                }
            }
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Engineering, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                        Text("Sin órdenes encontradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered, key = { it.id }) { order ->
                        ServiceOrderCard(order,
                            onEdit = { editOrder = order; showDialog = true },
                            onDelete = { viewModel.delete(order) },
                            onStatusChange = { viewModel.updateStatus(order, it) })
                    }
                }
            }
        }
    }
    if (showDialog) {
        ServiceOrderFormDialog(
            initial = editOrder,
            onDismiss = { showDialog = false },
            onSave = { o ->
                if (editOrder == null) viewModel.save(o) else viewModel.update(o.copy(id = editOrder!!.id))
                showDialog = false
            }
        )
    }
}

fun ServiceStatus.displayName() = when (this) {
    ServiceStatus.PENDING -> "Pendiente"
    ServiceStatus.IN_PROGRESS -> "En Proceso"
    ServiceStatus.COMPLETED -> "Finalizado"
}

fun ServiceStatus.color() = when (this) {
    ServiceStatus.PENDING -> StatusPending
    ServiceStatus.IN_PROGRESS -> StatusInRepair
    ServiceStatus.COMPLETED -> StatusCompleted
}

fun ServiceType.displayName() = when (this) {
    ServiceType.MAINTENANCE -> "Mantenimiento"
    ServiceType.REPAIR -> "Reparación"
    ServiceType.INSTALLATION -> "Instalación"
}

@Composable
fun ServiceOrderCard(order: ServiceOrder, onEdit: () -> Unit, onDelete: () -> Unit, onStatusChange: (ServiceStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val statusColor = order.status.color()

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, null, tint = Primary80, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(order.description, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Tipo: ${order.type.displayName()}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(order.status.displayName(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Medium)
                }
            }
            if (order.diagnosis.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("Diagnóstico: ${order.diagnosis}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Next status button
                if (order.status != ServiceStatus.COMPLETED) {
                    val nextStatus = if (order.status == ServiceStatus.PENDING) ServiceStatus.IN_PROGRESS else ServiceStatus.COMPLETED
                    OutlinedButton(onClick = { onStatusChange(nextStatus) }, modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)) {
                        Text("→ ${nextStatus.displayName()}", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Edit, null, tint = Primary80, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, null, tint = StatusLowStock, modifier = Modifier.size(18.dp)) }
            }
        }
    }
}

@Composable
fun ServiceOrderFormDialog(initial: ServiceOrder?, onDismiss: () -> Unit, onSave: (ServiceOrder) -> Unit) {
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var diagnosis by remember { mutableStateOf(initial?.diagnosis ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: ServiceType.MAINTENANCE) }
    var clientId by remember { mutableStateOf(initial?.clientId ?: 0L) }
    var equipId by remember { mutableStateOf(initial?.equipmentId ?: 0L) }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nueva Orden de Servicio" else "Editar Orden", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tipo de servicio:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ServiceType.values().forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t },
                            label = { Text(t.displayName(), style = MaterialTheme.typography.labelSmall) })
                    }
                }
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción *") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = diagnosis, onValueChange = { diagnosis = it }, label = { Text("Diagnóstico") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = clientId.toString(), onValueChange = { clientId = it.toLongOrNull() ?: 0L }, label = { Text("ID Cliente") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = equipId.toString(), onValueChange = { equipId = it.toLongOrNull() ?: 0L }, label = { Text("ID Equipo") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSave(ServiceOrder(clientId = clientId, equipmentId = equipId, type = type, description = desc, diagnosis = diagnosis)) },
                enabled = desc.isNotBlank()) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
