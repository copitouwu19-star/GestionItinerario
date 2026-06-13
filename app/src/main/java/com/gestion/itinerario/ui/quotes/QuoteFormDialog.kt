package com.gestion.itinerario.ui.quotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.entity.QuoteItem

private data class DraftItem(
    var description: String = "",
    var quantity: String = "1",
    var unitPrice: String = ""
) {
    fun toQuoteItem(): QuoteItem {
        val qty   = quantity.toDoubleOrNull() ?: 1.0
        val price = unitPrice.toDoubleOrNull() ?: 0.0
        return QuoteItem(description = description, quantity = qty, unitPrice = price, amount = qty * price)
    }
}

/** Formulario para redactar una cotización previa (sin firma; el cliente la aprobará luego). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteFormDialog(
    onDismiss: () -> Unit,
    viewModel: QuoteViewModel = hiltViewModel()
) {
    val clients by viewModel.clients.collectAsStateWithLifecycle()

    var clientDropdownExpanded by remember { mutableStateOf(false) }
    var selectedClient  by remember { mutableStateOf<Client?>(null) }
    var equipmentType   by remember { mutableStateOf("") }
    var description     by remember { mutableStateOf("") }
    var validDays       by remember { mutableStateOf(15) }
    val items = remember { mutableStateListOf(DraftItem()) }
    var isSaving by remember { mutableStateOf(false) }
    var created  by remember { mutableStateOf(false) }

    val total   = items.sumOf { (it.quantity.toDoubleOrNull() ?: 0.0) * (it.unitPrice.toDoubleOrNull() ?: 0.0) }
    val canSave = selectedClient != null &&
        items.any { it.description.isNotBlank() && (it.unitPrice.toDoubleOrNull() ?: 0.0) > 0.0 }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            if (created) {
                // ── Pantalla de éxito ─────────────────────────────────────────
                Column(modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White,
                            modifier = Modifier.size(40.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Cotización creada", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Ábrela desde la lista para compartirla con el cliente y registrar su aprobación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onDismiss, shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth(0.6f)) { Text("Cerrar") }
                }
            } else {
                Scaffold(
                    containerColor = Color.White,
                    topBar = {
                        TopAppBar(
                            title = { Text("Nueva Cotización", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = onDismiss) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Cancelar")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                        )
                    },
                    bottomBar = {
                        Surface(shadowElevation = 8.dp, color = Color.White) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(50)
                                ) { Text("Cancelar") }
                                Button(
                                    onClick = {
                                        val client = selectedClient ?: return@Button
                                        isSaving = true
                                        viewModel.createQuote(
                                            clientId      = client.id,
                                            clientName    = "${client.name} ${client.lastName}".trim(),
                                            clientPhone   = client.phone,
                                            clientAddress = client.address,
                                            equipmentType = equipmentType,
                                            description   = description,
                                            items         = items.filter { it.description.isNotBlank() }.map { it.toQuoteItem() },
                                            validDays     = validDays,
                                            onDone = { isSaving = false; created = true }
                                        )
                                    },
                                    enabled = canSave && !isSaving,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.linearGradient(listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )),
                                                shape = RoundedCornerShape(50)
                                            )
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSaving) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp, color = Color.White)
                                        } else {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, null,
                                                    tint = Color.White, modifier = Modifier.size(18.dp))
                                                Text("Crear cotización", color = Color.White,
                                                    fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // ── Banner ────────────────────────────────────────
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                Box(modifier = Modifier.width(4.dp).fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary))
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.RequestQuote, null,
                                            tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    Column {
                                        Text("Nueva Cotización", style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold)
                                        Text("Detalle el servicio a presupuestar para el cliente.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // ── Cliente ───────────────────────────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("CLIENTE *", style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable { clientDropdownExpanded = !clientDropdownExpanded },
                                    shape = RoundedCornerShape(16.dp), color = Color.White,
                                    shadowElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(Icons.Default.Person, null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp))
                                        Text(
                                            selectedClient?.let { "${it.name} ${it.lastName}".trim() }
                                                ?: "Seleccione un cliente",
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (selectedClient != null)
                                                MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Icon(Icons.Default.KeyboardArrowDown, null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp))
                                    }
                                }
                                DropdownMenu(
                                    expanded = clientDropdownExpanded,
                                    onDismissRequest = { clientDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (clients.isEmpty()) {
                                        DropdownMenuItem(text = { Text("Sin clientes registrados") },
                                            onClick = { clientDropdownExpanded = false })
                                    } else {
                                        clients.forEach { client ->
                                            DropdownMenuItem(
                                                text = { Text("${client.name} ${client.lastName}".trim()) },
                                                onClick = { selectedClient = client; clientDropdownExpanded = false },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Person, null,
                                                        tint = MaterialTheme.colorScheme.primary)
                                                })
                                        }
                                    }
                                }
                            }
                        }

                        // ── Equipo y descripción ──────────────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("DETALLES DEL SERVICIO", style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                            Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                                shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = equipmentType, onValueChange = { equipmentType = it },
                                    label = { Text("Equipo (opcional)") },
                                    leadingIcon = { Icon(Icons.Default.Build, null,
                                        modifier = Modifier.size(18.dp)) },
                                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                            Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                                shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = description, onValueChange = { description = it },
                                    label = { Text("Descripción del trabajo a cotizar") },
                                    leadingIcon = { Icon(Icons.Default.Description, null,
                                        modifier = Modifier.size(18.dp)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2, maxLines = 4,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }

                        // ── Validez ───────────────────────────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("VALIDEZ DE LA COTIZACIÓN", style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(7, 15, 30, 60).forEach { d ->
                                    FilterChip(
                                        selected = validDays == d,
                                        onClick = { validDays = d },
                                        label = { Text("$d días", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }

                        // ── Ítems ─────────────────────────────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("ÍTEMS / CONCEPTOS", style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                                TextButton(onClick = { items.add(DraftItem()) }) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Agregar ítem", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            items.forEachIndexed { index, item ->
                                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                                    Column(modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = item.description,
                                                onValueChange = { v -> items[index] = item.copy(description = v) },
                                                label = { Text("Concepto") },
                                                modifier = Modifier.weight(1f), singleLine = true
                                            )
                                            if (items.size > 1) {
                                                IconButton(onClick = { items.removeAt(index) }) {
                                                    Icon(Icons.Default.Delete, null,
                                                        tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = item.quantity,
                                                onValueChange = { v ->
                                                    if (v.all { it.isDigit() || it == '.' })
                                                        items[index] = item.copy(quantity = v)
                                                },
                                                label = { Text("Cant.") },
                                                modifier = Modifier.weight(0.6f), singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                            )
                                            OutlinedTextField(
                                                value = item.unitPrice,
                                                onValueChange = { v ->
                                                    if (v.all { it.isDigit() || it == '.' })
                                                        items[index] = item.copy(unitPrice = v)
                                                },
                                                label = { Text("Vlr. unitario") },
                                                leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                                                modifier = Modifier.weight(1f), singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                            )
                                        }
                                        val amount = (item.quantity.toDoubleOrNull() ?: 0.0) *
                                            (item.unitPrice.toDoubleOrNull() ?: 0.0)
                                        if (amount > 0.0) {
                                            Text("Subtotal: $${String.format("%.2f", amount)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }

                            // Total
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(0.08f))) {
                                Row(modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text("Total estimado", style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold)
                                    Text("$${String.format("%.2f", total)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
