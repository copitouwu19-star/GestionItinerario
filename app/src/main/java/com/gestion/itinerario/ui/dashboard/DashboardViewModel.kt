package com.gestion.itinerario.ui.dashboard

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.entity.ServiceStatus
import com.gestion.itinerario.data.entity.ServiceType
import com.gestion.itinerario.data.repository.AppointmentRepository
import com.gestion.itinerario.data.repository.ClientRepository
import com.gestion.itinerario.data.repository.EquipmentRepository
import com.gestion.itinerario.data.repository.ServiceRepository
import com.gestion.itinerario.ui.theme.StatusCompleted
import com.gestion.itinerario.ui.theme.StatusInRepair
import com.gestion.itinerario.ui.theme.StatusPending
import com.gestion.itinerario.workers.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ChartDay(val label: String, val citas: Int, val mantenimientos: Int)

data class TodayStats(
    val citasCompleted: Int = 0,
    val citasTotal: Int = 0,
    val mantCompleted: Int = 0,
    val mantTotal: Int = 0,
    val totalCompleted: Int = 0,
    val totalPending: Int = 0,
    val totalCancelled: Int = 0
)

/** Categoría de servicio derivada del tipo de equipo / descripción, usada para íconos y accesos rápidos. */
enum class ServiceCategory(val label: String, val icon: ImageVector, val color: Color) {
    ELECTRICIDAD("Electricidad",       Icons.Filled.Bolt,   Color(0xFFFBC02D)),
    REFRIGERACION("Refrigeración",     Icons.Filled.AcUnit, Color(0xFF29B6F6)),
    AIRE_ACONDICIONADO("Aire Acondicionado", Icons.Filled.AcUnit, Color(0xFF26C6DA)),
    OTROS("Otros",                     Icons.Filled.MoreHoriz, Color(0xFF9575CD))
}

/** Determina la categoría visual de un servicio a partir del tipo de equipo y/o su descripción. */
fun classifyServiceCategory(equipmentType: String, text: String = ""): ServiceCategory {
    val haystack = "$equipmentType $text".lowercase()
    return when {
        "aire acondicionado" in haystack || "split" in haystack || "a/c" in haystack ->
            ServiceCategory.AIRE_ACONDICIONADO
        "nevera" in haystack || "refriger" in haystack || "congela" in haystack ->
            ServiceCategory.REFRIGERACION
        "eléctric" in haystack || "electric" in haystack || "tablero" in haystack || "cableado" in haystack ->
            ServiceCategory.ELECTRICIDAD
        else -> ServiceCategory.OTROS
    }
}

fun ServiceType.shortLabel() = when (this) {
    ServiceType.MAINTENANCE  -> "Mantenimiento"
    ServiceType.REPAIR       -> "Reparación"
    ServiceType.INSTALLATION -> "Instalación"
}

fun ServiceType.icon(): ImageVector = when (this) {
    ServiceType.REPAIR       -> Icons.Filled.Build
    ServiceType.INSTALLATION -> Icons.Filled.HomeRepairService
    ServiceType.MAINTENANCE  -> Icons.Filled.Settings
}

fun Appointment.statusLabel(): String = when (status) {
    AppointmentStatus.SCHEDULED   -> "Programada"
    AppointmentStatus.IN_PROGRESS -> "En proceso"
    AppointmentStatus.COMPLETED   -> "Completada"
    AppointmentStatus.CANCELLED   -> "Cancelada"
}

fun ServiceOrder.statusLabel(): String = when (status) {
    ServiceStatus.PENDING     -> "Pendiente"
    ServiceStatus.IN_PROGRESS -> "En proceso"
    ServiceStatus.COMPLETED   -> "Completado"
}

@Immutable
data class NotificationItem(
    val title: String,
    val subtitle: String,
    val timestamp: Long,
    val icon: ImageVector,
    val color: Color,
    val isCompleted: Boolean = false,
    val statusLabel: String = ""
)

/** Punto de un gráfico de barras genérico (etiqueta + valor). */
data class ChartPoint(val label: String, val value: Int)

/** Estadísticas y actividad de una categoría de servicio, usadas en su mini-dashboard. */
@Immutable
data class CategoryStats(
    val category: ServiceCategory,
    val total: Int = 0,
    val completed: Int = 0,
    val inProgress: Int = 0,
    val pending: Int = 0,
    val cancelled: Int = 0,
    val revenue: Double = 0.0,
    val weekly: List<ChartPoint> = emptyList(),
    val history: List<ActivityItem> = emptyList()
)

@Immutable
data class ActivityItem(
    val title: String,
    val subtitle: String,
    val timestamp: Long,
    val amount: Double?,
    val icon: ImageVector,
    val color: Color
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val equipmentRepo: EquipmentRepository,
    private val serviceRepo: ServiceRepository,
    private val appointmentRepo: AppointmentRepository,
    private val clientRepo: ClientRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    // Nombre del usuario autenticado (primer nombre del displayName)
    private val _userName = MutableStateFlow(
        FirebaseAuth.getInstance().currentUser?.displayName?.split(" ")?.firstOrNull() ?: ""
    )
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Shared base flows — un único listener de Firestore para cada colección
    private val allAppointments: Flow<List<Appointment>> = appointmentRepo.getAll()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    private val allOrders: Flow<List<ServiceOrder>> = serviceRepo.getAll()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    // ── Contadores del dashboard ───────────────────────────────────────────────

    val inRepairCount: StateFlow<Int> = combine(
        allOrders.map { it.count { s -> s.status == ServiceStatus.IN_PROGRESS } },
        allAppointments.map { it.count { a -> a.status == AppointmentStatus.IN_PROGRESS } }
    ) { orders, appts -> orders + appts }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val todayAppointments: StateFlow<Int> = allAppointments
        .map { list -> list.count { a -> a.dateTime in todayRange() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val pendingAppointments: StateFlow<Int> = allAppointments
        .map { it.count { a -> a.status == AppointmentStatus.SCHEDULED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalServices: StateFlow<Int> = combine(
        allAppointments.map { it.count { a -> a.status == AppointmentStatus.COMPLETED } },
        allOrders.map { it.count { s -> s.status == ServiceStatus.COMPLETED } }
    ) { apptDone, ordersDone -> apptDone + ordersDone }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── Listas para bottom sheets ─────────────────────────────────────────────

    val inRepairList: StateFlow<Pair<List<Appointment>, List<ServiceOrder>>> = combine(
        allAppointments.map { it.filter { a -> a.status == AppointmentStatus.IN_PROGRESS } },
        allOrders.map { it.filter { s -> s.status == ServiceStatus.IN_PROGRESS } }
    ) { appts, orders -> Pair(appts, orders) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
            Pair(emptyList<Appointment>(), emptyList<ServiceOrder>()))

    val todayList: StateFlow<List<Appointment>> = allAppointments
        .map { list -> list.filter { a -> a.dateTime in todayRange() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pendingList: StateFlow<List<Appointment>> = allAppointments
        .map { it.filter { a -> a.status == AppointmentStatus.SCHEDULED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val completedList: StateFlow<Pair<List<Appointment>, List<ServiceOrder>>> = combine(
        allAppointments.map { it.filter { a -> a.status == AppointmentStatus.COMPLETED } },
        allOrders.map { it.filter { s -> s.status == ServiceStatus.COMPLETED } }
    ) { appts, orders -> Pair(appts, orders) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
            Pair(emptyList<Appointment>(), emptyList<ServiceOrder>()))

    // ── Próxima cita y estadísticas del día ───────────────────────────────────

    val nextAppointment: StateFlow<Appointment?> = allAppointments
        .map { list ->
            val now = System.currentTimeMillis()
            list.filter { it.status == AppointmentStatus.SCHEDULED && it.dateTime > now }
                .minByOrNull { it.dateTime }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val todayStats: StateFlow<TodayStats> = allAppointments
        .map { list ->
            val today = list.filter { it.dateTime in todayRange() }
            val citas = today.filter { it.serviceType != ServiceType.MAINTENANCE }
            val mant  = today.filter { it.serviceType == ServiceType.MAINTENANCE }
            TodayStats(
                citasCompleted = citas.count { it.status == AppointmentStatus.COMPLETED },
                citasTotal     = citas.size,
                mantCompleted  = mant.count { it.status == AppointmentStatus.COMPLETED },
                mantTotal      = mant.size,
                totalCompleted = today.count { it.status == AppointmentStatus.COMPLETED },
                totalPending   = today.count {
                    it.status == AppointmentStatus.SCHEDULED || it.status == AppointmentStatus.IN_PROGRESS
                },
                totalCancelled = today.count { it.status == AppointmentStatus.CANCELLED }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayStats())

    // ── Datos del gráfico y clientes ──────────────────────────────────────────

    val clients: StateFlow<List<Client>> = clientRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val chartData: StateFlow<List<ChartDay>> = allAppointments
        .map { buildChartData(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Total de servicios completados hoy ────────────────────────────────────

    val todayCompletedCount: StateFlow<Int> = combine(allAppointments, allOrders) { appts, orders ->
        val range = todayRange()
        appts.count { it.status == AppointmentStatus.COMPLETED && it.dateTime in range } +
            orders.count { it.status == ServiceStatus.COMPLETED && (it.completedAt ?: it.createdAt) in range }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val todayCompletedList: StateFlow<Pair<List<Appointment>, List<ServiceOrder>>> = combine(allAppointments, allOrders) { appts, orders ->
        val range = todayRange()
        Pair(
            appts.filter { it.status == AppointmentStatus.COMPLETED && it.dateTime in range },
            orders.filter { it.status == ServiceStatus.COMPLETED && (it.completedAt ?: it.createdAt) in range }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Pair(emptyList<Appointment>(), emptyList<ServiceOrder>()))

    // ── Notificaciones recientes (derivadas de la actividad existente) ────────

    val recentNotifications: StateFlow<List<NotificationItem>> = combine(
        allAppointments, allOrders, clients
    ) { appts, orders, clientList ->
        val clientMap = clientList.associateBy { it.id }
        fun clientName(id: String) = clientMap[id]?.let { "${it.name} ${it.lastName}".trim() } ?: "Cliente"

        val items = mutableListOf<NotificationItem>()
        appts.filter { it.status != AppointmentStatus.CANCELLED }.forEach { a ->
            val label = "${a.serviceType.shortLabel()} — ${clientName(a.clientId)}"
            val completed = a.status == AppointmentStatus.COMPLETED
            items += NotificationItem(
                title = when (a.status) {
                    AppointmentStatus.COMPLETED   -> "Servicio completado"
                    AppointmentStatus.IN_PROGRESS -> "En proceso"
                    else                          -> "Cita programada"
                },
                subtitle = label,
                timestamp = a.dateTime,
                icon = a.serviceType.icon(),
                color = when (a.status) {
                    AppointmentStatus.COMPLETED   -> StatusCompleted
                    AppointmentStatus.IN_PROGRESS -> StatusInRepair
                    else                          -> StatusPending
                },
                isCompleted = completed,
                statusLabel = a.statusLabel()
            )
        }
        orders.forEach { o ->
            val completed = o.status == ServiceStatus.COMPLETED
            items += NotificationItem(
                title = when (o.status) {
                    ServiceStatus.COMPLETED   -> "Orden finalizada"
                    ServiceStatus.IN_PROGRESS -> "Orden en proceso"
                    ServiceStatus.PENDING     -> "Orden pendiente"
                },
                subtitle = o.description.ifBlank { o.equipmentType }.ifBlank { "Servicio" },
                timestamp = o.completedAt ?: o.createdAt,
                icon = Icons.Filled.Build,
                color = when (o.status) {
                    ServiceStatus.COMPLETED   -> StatusCompleted
                    ServiceStatus.IN_PROGRESS -> StatusInRepair
                    ServiceStatus.PENDING     -> StatusPending
                },
                isCompleted = completed,
                statusLabel = o.statusLabel()
            )
        }
        items.sortedWith(compareBy({ it.isCompleted }, { -it.timestamp })).take(30)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Agrupa notificaciones/actividad por día: "Hoy", "Ayer", y para días más antiguos muestra la fecha real. */
    fun <T> groupByRecency(items: List<T>, timestampOf: (T) -> Long): List<Pair<String, List<T>>> {
        val dateSdf = java.text.SimpleDateFormat("EEE dd MMM", java.util.Locale.getDefault())

        fun dayKey(cal: Calendar) = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"

        val todayCal = Calendar.getInstance()
        val todayKey = dayKey(todayCal)
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayKey = dayKey(yesterdayCal)

        fun labelFor(timestamp: Long): String {
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            return when (dayKey(cal)) {
                todayKey     -> "Hoy"
                yesterdayKey -> "Ayer"
                else         -> dateSdf.format(cal.time)
                    .replaceFirstChar { it.uppercase() }
            }
        }

        // Agrupa manteniendo el orden por timestamp descendente dentro de cada grupo
        val keyToLabel = LinkedHashMap<String, String>()
        val keyToItems = LinkedHashMap<String, MutableList<T>>()
        items.sortedByDescending { timestampOf(it) }.forEach { item ->
            val cal = Calendar.getInstance().apply { timeInMillis = timestampOf(item) }
            val k = dayKey(cal)
            if (!keyToLabel.containsKey(k)) keyToLabel[k] = labelFor(timestampOf(item))
            keyToItems.getOrPut(k) { mutableListOf() }.add(item)
        }
        return keyToItems.entries.map { (k, group) -> keyToLabel[k]!! to group }
    }

    // ── Actividad reciente (últimas 24 horas) ─────────────────────────────────

    val recentActivity: StateFlow<List<ActivityItem>> = combine(
        allAppointments, allOrders, clients
    ) { appts, orders, clientList ->
        val clientMap = clientList.associateBy { it.id }
        fun clientName(id: String) = clientMap[id]?.let { "${it.name} ${it.lastName}".trim() } ?: "Cliente"
        val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L

        val items = mutableListOf<ActivityItem>()
        appts.filter { it.status == AppointmentStatus.COMPLETED && it.dateTime >= cutoff }.forEach { a ->
            val category = classifyServiceCategory(a.equipmentType, a.notes)
            items += ActivityItem(
                title = "${a.serviceType.shortLabel()} completada",
                subtitle = "${clientName(a.clientId)} · ${a.equipmentType.ifBlank { category.label }}",
                timestamp = a.dateTime,
                amount = null,
                icon = a.serviceType.icon(),
                color = category.color
            )
        }
        orders.filter { it.status == ServiceStatus.COMPLETED && (it.completedAt ?: 0L) >= cutoff }.forEach { o ->
            val category = classifyServiceCategory(o.equipmentType, o.description)
            items += ActivityItem(
                title = "${o.type.shortLabel()} completada",
                subtitle = "${clientName(o.clientId)} · ${o.description.ifBlank { o.equipmentType }}",
                timestamp = o.completedAt ?: o.createdAt,
                amount = o.totalCost.takeIf { it > 0.0 },
                icon = o.type.icon(),
                color = category.color
            )
        }
        items.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Mini-dashboard por categoría de servicio (Electricidad/Refrigeración/A.A./Otros) ──

    val categoryStats: StateFlow<Map<ServiceCategory, CategoryStats>> = combine(
        allAppointments, allOrders, clients
    ) { appts, orders, clientList ->
        val clientMap = clientList.associateBy { it.id }
        fun clientName(id: String) = clientMap[id]?.let { "${it.name} ${it.lastName}".trim() } ?: "Cliente"
        val days = (6 downTo 0).map { dayBounds(it) }

        ServiceCategory.values().associateWith { cat ->
            val catAppts  = appts.filter { classifyServiceCategory(it.equipmentType, it.notes) == cat }
            val catOrders = orders.filter { classifyServiceCategory(it.equipmentType, it.description) == cat }

            val weekly = days.map { (start, end, label) ->
                val count = catAppts.count { it.dateTime in start..end } +
                    catOrders.count { (it.completedAt ?: it.createdAt) in start..end }
                ChartPoint(label, count)
            }

            val history = mutableListOf<ActivityItem>()
            catAppts.forEach { a ->
                history += ActivityItem(
                    title = "${a.serviceType.shortLabel()} — ${a.statusLabel()}",
                    subtitle = "${clientName(a.clientId)} · ${a.equipmentType.ifBlank { cat.label }}",
                    timestamp = a.dateTime,
                    amount = null,
                    icon = a.serviceType.icon(),
                    color = cat.color
                )
            }
            catOrders.forEach { o ->
                history += ActivityItem(
                    title = "${o.type.shortLabel()} — ${o.statusLabel()}",
                    subtitle = "${clientName(o.clientId)} · ${o.description.ifBlank { o.equipmentType }}",
                    timestamp = o.completedAt ?: o.createdAt,
                    amount = o.totalCost.takeIf { it > 0.0 },
                    icon = o.type.icon(),
                    color = cat.color
                )
            }

            CategoryStats(
                category   = cat,
                total      = catAppts.size + catOrders.size,
                completed  = catAppts.count { it.status == AppointmentStatus.COMPLETED } +
                             catOrders.count { it.status == ServiceStatus.COMPLETED },
                inProgress = catAppts.count { it.status == AppointmentStatus.IN_PROGRESS } +
                             catOrders.count { it.status == ServiceStatus.IN_PROGRESS },
                pending    = catAppts.count { it.status == AppointmentStatus.SCHEDULED } +
                             catOrders.count { it.status == ServiceStatus.PENDING },
                cancelled  = catAppts.count { it.status == AppointmentStatus.CANCELLED },
                revenue    = catOrders.filter { it.status == ServiceStatus.COMPLETED }.sumOf { it.totalCost },
                weekly     = weekly,
                history    = history.sortedByDescending { it.timestamp }.take(15)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // ── Acciones ──────────────────────────────────────────────────────────────

    fun saveOrder(order: ServiceOrder) = viewModelScope.launch { serviceRepo.save(order) }

    fun saveAppointment(appointment: Appointment) = viewModelScope.launch {
        val id = appointmentRepo.save(appointment)
        alarmScheduler.schedule(appointment.copy(id = id))
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun todayRange(): LongRange {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        return start..cal.timeInMillis
    }

    /** Devuelve (inicio, fin, etiqueta) del día que está [daysAgo] días atrás (0 = hoy). */
    private fun dayBounds(daysAgo: Int): Triple<Long, Long, String> {
        val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val label = if (daysAgo == 0) "Hoy" else dayLabels[(cal.get(Calendar.DAY_OF_WEEK) + 5) % 7]
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        return Triple(start, cal.timeInMillis, label)
    }

    private fun buildChartData(appointments: List<Appointment>): List<ChartDay> {
        val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")
        return (6 downTo 0).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
            val end = cal.timeInMillis

            val dayAppts = appointments.filter { it.dateTime in start..end }
            val citas = dayAppts.count { it.serviceType != ServiceType.MAINTENANCE }
            val mant  = dayAppts.count { it.serviceType == ServiceType.MAINTENANCE }
            val label = if (i == 0) "Hoy" else dayLabels[(cal.get(Calendar.DAY_OF_WEEK) + 5) % 7]
            ChartDay(label, citas, mant)
        }
    }
}
