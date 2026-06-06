package com.gestion.itinerario.ui.inventory

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.Equipment
import com.gestion.itinerario.data.entity.EquipmentStatus
import com.gestion.itinerario.data.entity.ItemCategory
import com.gestion.itinerario.data.entity.MovementType
import com.gestion.itinerario.data.entity.SparePart
import com.gestion.itinerario.ui.theme.*

// Niveles de alerta de stock
private enum class StockLevel { NORMAL, WARNING, CRITICAL }
private fun stockLevel(quantity: Int, minStock: Int): StockLevel {
    val warningThreshold = (minStock * 1.2).toInt().coerceAtLeast(minStock + 1)
    return when {
        quantity < minStock -> StockLevel.CRITICAL          // Rojo: ya bajó del mínimo
        quantity <= warningThreshold -> StockLevel.WARNING  // Amarillo: cerca del mínimo
        else -> StockLevel.NORMAL
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    innerPadding: PaddingValues,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Equipos", "Repuestos", "Herramientas", "Stock Bajo")
    val equipment by viewModel.equipment.collectAsStateWithLifecycle()
    val spareParts by viewModel.spareParts.collectAsStateWithLifecycle()
    val lowStock by viewModel.lowStock.collectAsStateWithLifecycle()
    val repuestos = spareParts.filter { it.category == ItemCategory.SPARE_PART }
    val herramientas = spareParts.filter { it.category == ItemCategory.TOOL }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEquipmentDialog by remember { mutableStateOf(false) }
    var showSpareDialog by remember { mutableStateOf(false) }
    var editEquipment by remember { mutableStateOf<Equipment?>(null) }
    var editSpare by remember { mutableStateOf<SparePart?>(null) }

    // Selector de producto para ajuste de cantidad
    var showPlusSelector by remember { mutableStateOf(false) }
    var showMinusSelector by remember { mutableStateOf(false) }
    var movementTarget by remember { mutableStateOf<SparePart?>(null) }
    var movementType by remember { mutableStateOf(MovementType.ENTRY) }
    var showMovementDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón - (restar stock)
                SmallFloatingActionButton(
                    onClick = { movementType = MovementType.EXIT; showMinusSelector = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(Icons.Default.Remove, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                }
                // Botón + (sumar stock)
                SmallFloatingActionButton(
                    onClick = { movementType = MovementType.ENTRY; showPlusSelector = true },
                    containerColor = StatusCompleted.copy(alpha = 0.9f)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
                // FAB principal: nuevo producto
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Primary40
                ) {
                    Icon(Icons.Default.LibraryAdd, null, tint = Color.White)
                }
            }
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier.padding(
                top = scaffoldPadding.calculateTopPadding(),
                bottom = maxOf(
                    innerPadding.calculateBottomPadding(),
                    scaffoldPadding.calculateBottomPadding()
                )
            )
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, t ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(t) })
                }
            }
            when (selectedTab) {
                0 -> EquipmentList(equipment,
                    onEdit = { editEquipment = it; showEquipmentDialog = true },
                    onDelete = viewModel::deleteEquipment)
                1 -> SparePartsList(repuestos,
                    onEdit = { editSpare = it; showSpareDialog = true },
                    onDelete = viewModel::deleteSparePart,
                    onMovement = { sp, t, q, n -> viewModel.registerMovement(sp, t, q, n) })
                2 -> SparePartsList(herramientas,
                    onEdit = { editSpare = it; showSpareDialog = true },
                    onDelete = viewModel::deleteSparePart,
                    onMovement = { sp, t, q, n -> viewModel.registerMovement(sp, t, q, n) })
                3 -> LowStockList(lowStock,
                    onMovement = { sp, t, q, n -> viewModel.registerMovement(sp, t, q, n) })
            }
        }
    }

    // Diálogo: seleccionar tipo de nuevo producto
    if (showAddDialog) {
        NewProductTypeDialog(
            onDismiss = { showAddDialog = false },
            onEquipment = { showAddDialog = false; editEquipment = null; showEquipmentDialog = true },
            onSparePart = { showAddDialog = false; editSpare = null; showSpareDialog = true }
        )
    }

    // Selector de repuesto para ajuste + 
    if (showPlusSelector || showMinusSelector) {
        SparePartSelectorDialog(
            spareParts = spareParts,
            title = if (showPlusSelector) "Seleccionar repuesto — Añadir" else "Seleccionar repuesto — Quitar",
            onDismiss = { showPlusSelector = false; showMinusSelector = false },
            onSelect = { sp ->
                movementTarget = sp
                showPlusSelector = false
                showMinusSelector = false
                showMovementDialog = true
            }
        )
    }

    // Diálogo ajuste de cantidad
    if (showMovementDialog && movementTarget != null) {
        StockMovementDialog(
            sp = movementTarget!!,
            forcedType = movementType,
            onDismiss = { showMovementDialog = false; movementTarget = null },
            onSave = { t, q, n ->
                viewModel.registerMovement(movementTarget!!, t, q, n)
                showMovementDialog = false; movementTarget = null
            }
        )
    }

    if (showEquipmentDialog) {
        EquipmentFormDialog(
            initial = editEquipment,
            onDismiss = { showEquipmentDialog = false },
            onSave = { eq ->
                if (editEquipment == null) viewModel.saveEquipment(eq)
                else viewModel.updateEquipment(eq.copy(id = editEquipment!!.id))
                showEquipmentDialog = false
            }
        )
    }
    if (showSpareDialog) {
        SparePartFormDialog(
            initial = editSpare,
            onDismiss = { showSpareDialog = false },
            onSave = { sp ->
                if (editSpare == null) viewModel.saveSparePart(sp)
                else viewModel.updateSparePart(sp.copy(id = editSpare!!.id))
                showSpareDialog = false
            }
        )
    }
}

// ─── Diálogo: Elegir tipo de nuevo producto ───────────────────────────────────
@Composable
fun NewProductTypeDialog(onDismiss: () -> Unit, onEquipment: () -> Unit, onSparePart: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Qué deseas agregar?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onEquipment() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary80.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.PrecisionManufacturing, null, tint = Primary80, modifier = Modifier.size(32.dp))
                        Column {
                            Text("Equipo", fontWeight = FontWeight.Bold, color = Primary80)
                            Text("Nevera, aire, lavadora, etc.", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSparePart() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Secondary80.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Settings, null, tint = Secondary80, modifier = Modifier.size(32.dp))
                        Column {
                            Text("Repuesto", fontWeight = FontWeight.Bold, color = Secondary80)
                            Text("Piezas, componentes, etc.", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ─── Diálogo: Seleccionar repuesto de la lista ────────────────────────────────
@Composable
fun SparePartSelectorDialog(
    spareParts: List<SparePart>,
    title: String,
    onDismiss: () -> Unit,
    onSelect: (SparePart) -> Unit
) {
    var selected by remember { mutableStateOf<SparePart?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
        text = {
            if (spareParts.isEmpty()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No hay repuestos registrados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(spareParts, key = { it.id }) { sp ->
                        val isSelected = selected?.id == sp.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = sp },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Primary80.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Primary80) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selected = sp }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sp.name, fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface)
                                    Text("Cantidad: ${sp.quantity} ${sp.unit} | Mín: ${sp.minStock}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (sp.quantity <= sp.minStock) StatusLowStock
                                        else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (sp.quantity <= sp.minStock) {
                                    Icon(Icons.Default.Warning, null, tint = StatusLowStock, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selected?.let { onSelect(it) } },
                enabled = selected != null
            ) { Text("Seleccionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ─── Listas ───────────────────────────────────────────────────────────────────
@Composable
fun EquipmentList(list: List<Equipment>, onEdit: (Equipment) -> Unit, onDelete: (Equipment) -> Unit) {
    if (list.isEmpty()) EmptyState("Sin equipos registrados", Icons.Default.PrecisionManufacturing)
    else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(list, key = { it.id }) { eq -> EquipmentCard(eq, onEdit, onDelete) }
    }
}

@Composable
fun SparePartsList(list: List<SparePart>, onEdit: (SparePart) -> Unit, onDelete: (SparePart) -> Unit,
                   onMovement: (SparePart, MovementType, Int, String) -> Unit) {
    if (list.isEmpty()) EmptyState("Sin repuestos registrados", Icons.Default.Settings)
    else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(list, key = { it.id }) { sp -> SparePartCard(sp, onEdit, onDelete, onMovement) }
    }
}

@Composable
fun LowStockList(list: List<SparePart>, onMovement: (SparePart, MovementType, Int, String) -> Unit) {
    if (list.isEmpty()) EmptyState("¡Sin alertas de stock bajo!", Icons.Default.CheckCircle, color = StatusAvailable)
    else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Conteo por nivel
        val critical = list.count { stockLevel(it.quantity, it.minStock) == StockLevel.CRITICAL }
        val warning = list.count { stockLevel(it.quantity, it.minStock) == StockLevel.WARNING }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (critical > 0) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Error, null, tint = StatusLowStock)
                    Text("🔴 $critical repuesto(s) con stock CRÍTICO", color = StatusLowStock, fontWeight = FontWeight.Bold)
                }
                if (warning > 0) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFFFA726))
                    Text("🟡 $warning repuesto(s) con stock BAJO", color = Color(0xFFFFA726), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        items(list, key = { it.id }) { sp -> SparePartCard(sp, onEdit = {}, onDelete = {}, onMovement = onMovement) }
    }
}

// ─── Tarjetas ─────────────────────────────────────────────────────────────────
@Composable
fun EquipmentCard(eq: Equipment, onEdit: (Equipment) -> Unit, onDelete: (Equipment) -> Unit) {
    val statusColor = when (eq.status) {
        EquipmentStatus.AVAILABLE -> StatusAvailable
        EquipmentStatus.IN_REPAIR -> StatusInRepair
        EquipmentStatus.DELIVERED -> StatusDelivered
    }
    val statusLabel = when (eq.status) {
        EquipmentStatus.AVAILABLE -> "Disponible"
        EquipmentStatus.IN_REPAIR -> "En Reparación"
        EquipmentStatus.DELIVERED -> "Entregado"
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(statusColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Air, null, tint = statusColor, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${eq.brand} ${eq.model}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text("Serial: ${eq.serial}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(statusLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Medium)
                }
            }
            IconButton(onClick = { onEdit(eq) }) { Icon(Icons.Default.Edit, null, tint = Primary80) }
            IconButton(onClick = { onDelete(eq) }) { Icon(Icons.Default.Delete, null, tint = StatusLowStock) }
        }
    }
}

@Composable
fun SparePartCard(sp: SparePart, onEdit: (SparePart) -> Unit, onDelete: (SparePart) -> Unit,
                  onMovement: (SparePart, MovementType, Int, String) -> Unit) {
    val level = stockLevel(sp.quantity, sp.minStock)
    val cardColor = when (level) {
        StockLevel.CRITICAL -> StatusLowStock.copy(alpha = 0.10f)          // fondo rojo suave
        StockLevel.WARNING  -> Color(0xFFFFA726).copy(alpha = 0.10f)        // fondo amarillo suave
        StockLevel.NORMAL   -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when (level) {
        StockLevel.CRITICAL -> StatusLowStock
        StockLevel.WARNING  -> Color(0xFFFFA726)
        StockLevel.NORMAL   -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val levelIcon = when (level) {
        StockLevel.CRITICAL -> Icons.Default.Error
        StockLevel.WARNING  -> Icons.Default.Warning
        StockLevel.NORMAL   -> null
    }
    val levelLabel = when (level) {
        StockLevel.CRITICAL -> "🔴 Stock crítico"
        StockLevel.WARNING  -> "🟡 Cerca del mínimo"
        StockLevel.NORMAL   -> null
    }
    var showMovement by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(sp.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    if (levelIcon != null) Icon(levelIcon, null, tint = textColor, modifier = Modifier.size(16.dp))
                }
                Text("Cantidad: ${sp.quantity} ${sp.unit}",
                    style = MaterialTheme.typography.bodySmall, color = textColor)
                Text("Mínimo: ${sp.minStock}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (levelLabel != null) {
                    Text(levelLabel, style = MaterialTheme.typography.labelSmall,
                        color = textColor, fontWeight = FontWeight.Medium)
                }
            }
            IconButton(onClick = { showMovement = true }) { Icon(Icons.Default.SwapVert, null, tint = Secondary80) }
            if (onEdit != {}) IconButton(onClick = { onEdit(sp) }) { Icon(Icons.Default.Edit, null, tint = Primary80) }
            if (onDelete != {}) IconButton(onClick = { onDelete(sp) }) { Icon(Icons.Default.Delete, null, tint = StatusLowStock) }
        }
    }
    if (showMovement) {
        StockMovementDialog(sp, onDismiss = { showMovement = false }, onSave = { t, q, n ->
            onMovement(sp, t, q, n); showMovement = false
        })
    }
}

// ─── Diálogos de formulario ───────────────────────────────────────────────────
@Composable
fun EmptyState(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
               color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = color.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
            Text(message, color = color.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun EquipmentFormDialog(initial: Equipment?, onDismiss: () -> Unit, onSave: (Equipment) -> Unit) {
    var brand by remember { mutableStateOf(initial?.brand ?: "") }
    var model by remember { mutableStateOf(initial?.model ?: "") }
    var serial by remember { mutableStateOf(initial?.serial ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: EquipmentStatus.AVAILABLE) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo Equipo" else "Editar Equipo", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Marca *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Modelo *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = serial, onValueChange = { serial = it }, label = { Text("Serial *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())
                Text("Estado:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EquipmentStatus.values().forEach { s ->
                        FilterChip(selected = status == s, onClick = { status = s },
                            label = { Text(s.name, style = MaterialTheme.typography.labelSmall) })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(Equipment(brand = brand, model = model, serial = serial, status = status, notes = notes)) },
                enabled = brand.isNotBlank() && model.isNotBlank() && serial.isNotBlank()) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun SparePartFormDialog(initial: SparePart?, onDismiss: () -> Unit, onSave: (SparePart) -> Unit) {
    var name        by remember { mutableStateOf(initial?.name ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var qty         by remember { mutableStateOf(initial?.quantity?.toString() ?: "0") }
    var minStock    by remember { mutableStateOf(initial?.minStock?.toString() ?: "5") }
    var unit        by remember { mutableStateOf(initial?.unit ?: "unidad") }
    var category    by remember { mutableStateOf(initial?.category ?: ItemCategory.SPARE_PART) }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { _: Uri? -> /* foto subida en producción via Storage — placeholder */ }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo Ítem de Inventario" else "Editar Ítem", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Categoría
                Text("Tipo:", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = category == ItemCategory.SPARE_PART,
                        onClick = { category = ItemCategory.SPARE_PART },
                        label = { Text("Repuesto") },
                        leadingIcon = if (category == ItemCategory.SPARE_PART) {{
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                        }} else null
                    )
                    FilterChip(
                        selected = category == ItemCategory.TOOL,
                        onClick = { category = ItemCategory.TOOL },
                        label = { Text("Herramienta") },
                        leadingIcon = if (category == ItemCategory.TOOL) {{
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                        }} else null
                    )
                }
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Nombre *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qty, onValueChange = { qty = it.filter { c -> c.isDigit() } },
                    label = { Text("Cantidad actual") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = minStock, onValueChange = { minStock = it.filter { c -> c.isDigit() } },
                    label = { Text("Stock mínimo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = unit, onValueChange = { unit = it },
                    label = { Text("Unidad (ej: unidad, kg, caja)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                // Foto
                OutlinedButton(onClick = { photoLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar foto (opcional)")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(SparePart(
                    id          = initial?.id ?: "",
                    name        = name,
                    description = description,
                    quantity    = qty.toIntOrNull() ?: 0,
                    minStock    = minStock.toIntOrNull() ?: 5,
                    unit        = unit,
                    category    = category,
                    photoUrl    = initial?.photoUrl ?: "",
                    createdAt   = initial?.createdAt ?: System.currentTimeMillis()
                ))
            }, enabled = name.isNotBlank()) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun StockMovementDialog(
    sp: SparePart,
    forcedType: MovementType? = null,
    onDismiss: () -> Unit,
    onSave: (MovementType, Int, String) -> Unit
) {
    var type by remember { mutableStateOf(forcedType ?: MovementType.ENTRY) }
    var qty by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Ajuste de Stock", fontWeight = FontWeight.Bold)
                Text(sp.name, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Stock actual: ${sp.quantity} ${sp.unit}",
                    style = MaterialTheme.typography.labelSmall, color = Primary80)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (forcedType == null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = type == MovementType.ENTRY, onClick = { type = MovementType.ENTRY },
                            label = { Text("➕ Añadir") })
                        FilterChip(selected = type == MovementType.EXIT, onClick = { type = MovementType.EXIT },
                            label = { Text("➖ Quitar") })
                    }
                } else {
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = if (forcedType == MovementType.ENTRY) StatusCompleted.copy(0.15f) else StatusLowStock.copy(0.15f)) {
                        Text(
                            if (forcedType == MovementType.ENTRY) "➕ Añadiendo stock" else "➖ Quitando stock",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (forcedType == MovementType.ENTRY) StatusCompleted else StatusLowStock,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                OutlinedTextField(value = qty, onValueChange = { qty = it },
                    label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSave(type, qty.toIntOrNull() ?: 1, notes) }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
