package com.gestion.itinerario.ui.dashboard

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
