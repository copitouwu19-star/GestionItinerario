package com.gestion.itinerario.ui.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import com.gestion.itinerario.data.entity.ServiceStatus
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
    private val invoiceRepo: InvoiceRepository,
    private val serviceRepo: ServiceRepository,
    private val profileRepo: ProfileRepository
) : ViewModel() {

    /** Genera el PDF del reporte mensual para el mes/año dados (month: 0-11). */
    suspend fun generateMonthlyReport(context: Context, year: Int, month: Int): File =
        withContext(Dispatchers.IO) {
            val cal = Calendar.getInstance().apply {
                set(year, month, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val end = cal.timeInMillis

            val profile = kotlinx.coroutines.withTimeoutOrNull(8_000L) { profileRepo.getProfile().first() }
                ?: com.gestion.itinerario.data.entity.CompanyProfile()

            val invoices = invoiceRepo.getAll().first().filter {
                val date = it.endDate.takeIf { d -> d > 0 } ?: it.createdAt
                date in start until end
            }
            val services = serviceRepo.getAll().first().filter {
                it.status == ServiceStatus.COMPLETED &&
                    it.completedAt != null && it.completedAt in start until end
            }

            ReportPdfGenerator.generate(context, profile, year, month, invoices, services)
        }
}
