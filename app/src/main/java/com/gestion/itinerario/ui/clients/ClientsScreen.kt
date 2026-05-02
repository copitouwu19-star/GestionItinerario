package com.gestion.itinerario.ui.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.ui.theme.Primary40
import com.gestion.itinerario.ui.theme.Primary80
import com.gestion.itinerario.ui.theme.StatusLowStock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: ClientViewModel = hiltViewModel()
) {
    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val search by viewModel.search.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editClient by remember { mutableStateOf<Client?>(null) }
    var detailClient by remember { mutableStateOf<Client?>(null) }

    if (detailClient != null) {
        ClientDetailScreen(client = detailClient!!, viewModel = viewModel, onBack = { detailClient = null })
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Clientes", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editClient = null; showDialog = true }, containerColor = Primary40) {
                Icon(Icons.Default.PersonAdd, null, tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(
                top = padding.calculateTopPadding(),
                bottom = maxOf(
                    innerPadding.calculateBottomPadding(),
                    padding.calculateBottomPadding()
                )
            )
            .fillMaxSize()) {
            OutlinedTextField(
                value = search, onValueChange = viewModel::onSearch,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar por nombre o teléfono…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            if (clients.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.People, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                        Text("Sin clientes registrados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(clients, key = { it.id }) { c ->
                        ClientCard(c,
                            onView = { detailClient = c },
                            onEdit = { editClient = c; showDialog = true },
                            onDelete = { viewModel.delete(c) })
                    }
                }
            }
        }
    }
    if (showDialog) {
        ClientFormDialog(initial = editClient, onDismiss = { showDialog = false }, onSave = { c ->
            if (editClient == null) viewModel.save(c) else viewModel.update(c.copy(id = editClient!!.id))
            showDialog = false
        })
    }
}

@Composable
fun ClientCard(c: Client, onView: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onView) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape)
                .background(Primary80.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Text(c.name.take(1).uppercase(), color = Primary80, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(c.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                if (c.phone.isNotBlank()) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Text(c.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (c.email.isNotBlank()) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Text(c.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Primary80) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = StatusLowStock) }
        }
    }
}

@Composable
fun ClientDetailScreen(client: Client, viewModel: ClientViewModel, onBack: () -> Unit) {
    val equipment by viewModel.getEquipmentForClient(client.id).collectAsState(initial = emptyList())
    val services by viewModel.getServicesForClient(client.id).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(client.name, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Información del Cliente", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        if (client.phone.isNotBlank()) InfoRow(Icons.Default.Phone, client.phone)
                        if (client.email.isNotBlank()) InfoRow(Icons.Default.Email, client.email)
                        if (client.address.isNotBlank()) InfoRow(Icons.Default.LocationOn, client.address)
                    }
                }
            }
            item { Text("Equipos (${equipment.size})", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall) }
            items(equipment) { eq ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Air, null, tint = Primary80)
                        Text("${eq.brand} ${eq.model} — ${eq.serial}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item { Text("Historial de Servicios (${services.size})", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall) }
            items(services) { svc ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, null, tint = Primary80)
                        Column {
                            Text(svc.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Tipo: ${svc.type.name} • Estado: ${svc.status.name}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = Primary80, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormDialog(initial: Client?, onDismiss: () -> Unit, onSave: (Client) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var lastName by remember { mutableStateOf(initial?.lastName ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo Cliente" else "Editar Cliente", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Apellido") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas adicionales") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Client(
                            name = name.trim(),
                            lastName = lastName.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                            notes = notes.trim()
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
