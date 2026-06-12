package com.gestion.itinerario.ui.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.ServiceStatus
import com.gestion.itinerario.data.repository.AppointmentRepository
import com.gestion.itinerario.data.repository.InvoiceRepository
import com.gestion.itinerario.data.repository.ProfileRepository
import com.gestion.itinerario.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val invoiceRepo:     InvoiceRepository,
    private val serviceRepo:     ServiceRepository,
    private val appointmentRepo: AppointmentRepository,
    private val profileRepo:     ProfileRepository
) : ViewModel() {

    /** Genera el PDF del reporte mensual para el mes/año dados (month: 0-11). */
    suspend fun generateMonthlyReport(context: Context, year: Int, month: Int): File =
        withContext(Dispatchers.IO) {
            val (start, end) = monthRange(year, month)
            val profile = kotlinx.coroutines.withTimeoutOrNull(8_000L) { profileRepo.getProfile().first() }
                ?: com.gestion.itinerario.data.entity.CompanyProfile()

            val invoices = invoiceRepo.getAll().first().filter {
                val date = it.endDate.takeIf { d -> d > 0 } ?: it.createdAt
                date in start until end
            }
            // Citas completadas — fuente principal de "servicios realizados"
            val appointments = appointmentRepo.getAll().first().filter {
                it.status == AppointmentStatus.COMPLETED && it.dateTime in start until end
            }
            // Órdenes de servicio completadas como complemento
            val services = serviceRepo.getAll().first().filter {
                it.status == ServiceStatus.COMPLETED && run {
                    val date = it.completedAt?.takeIf { d -> d > 0 } ?: it.createdAt
                    date in start until end
                }
            }
            ReportPdfGenerator.generate(context, profile, year, month, invoices, appointments, services)
        }

    /** Genera un XLS (SpreadsheetML) del reporte mensual (month: 0-11). */
    suspend fun generateMonthlyExcel(context: Context, year: Int, month: Int): File =
        withContext(Dispatchers.IO) {
            val (start, end) = monthRange(year, month)
            val profile = kotlinx.coroutines.withTimeoutOrNull(8_000L) { profileRepo.getProfile().first() }
                ?: com.gestion.itinerario.data.entity.CompanyProfile()

            val invoices = invoiceRepo.getAll().first().filter {
                val date = it.endDate.takeIf { d -> d > 0 } ?: it.createdAt
                date in start until end
            }
            val appointments = appointmentRepo.getAll().first().filter {
                it.status == AppointmentStatus.COMPLETED && it.dateTime in start until end
            }
            val services = serviceRepo.getAll().first().filter {
                it.status == ServiceStatus.COMPLETED && run {
                    val date = it.completedAt?.takeIf { d -> d > 0 } ?: it.createdAt
                    date in start until end
                }
            }
            ReportPdfGenerator.generateXls(context, profile, year, month, invoices, appointments, services)
        }

    private fun monthRange(year: Int, month: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        return start to cal.timeInMillis
    }
}
