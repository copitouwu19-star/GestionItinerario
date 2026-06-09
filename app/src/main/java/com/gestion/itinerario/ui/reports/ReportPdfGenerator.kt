package com.gestion.itinerario.ui.reports

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.gestion.itinerario.data.entity.CompanyProfile
import com.gestion.itinerario.data.entity.Invoice
import com.gestion.itinerario.data.entity.PaymentStatus
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.entity.ServiceType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/** Genera un reporte mensual en PDF con ingresos y servicios realizados. */
object ReportPdfGenerator {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthNames = arrayOf("Enero","Febrero","Marzo","Abril","Mayo","Junio",
        "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre")

    private const val PAGE_W = 595f
    private const val PAGE_H = 842f
    private const val MARGIN_L = 40f
    private const val MARGIN_R = 555f
    private const val CONTENT_BOTTOM = 800f

    fun generate(
        context: Context,
        profile: CompanyProfile,
        year: Int,
        month: Int,
        invoices: List<Invoice>,
        services: List<ServiceOrder>
    ): File {
        val document = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val boldPaint = Paint().apply { typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val normalPaint = Paint().apply { typeface = Typeface.DEFAULT; isAntiAlias = true }
        val headerPaint = Paint().apply { typeface = Typeface.DEFAULT_BOLD; textSize = 10f; color = Color.WHITE; isAntiAlias = true }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        val bluePaint = Paint().apply { color = Color.rgb(21, 101, 192) }
        val periodLabel = "${monthNames[month]} $year"

        var y = 40f

        fun newPage() {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            y = 40f
        }

        fun ensureSpace(needed: Float) {
            if (y + needed > CONTENT_BOTTOM) newPage()
        }

        // ── Encabezado azul ───────────────────────────────────────────────────
        canvas.drawRect(0f, 0f, PAGE_W, 70f, bluePaint)
        boldPaint.color = Color.WHITE; boldPaint.textSize = 18f
        canvas.drawText(profile.companyName.ifBlank { "Mi Empresa" }, MARGIN_L, 32f, boldPaint)
        normalPaint.color = Color.rgb(220, 240, 255); normalPaint.textSize = 11f
        canvas.drawText("Reporte mensual de ingresos y servicios", MARGIN_L, 50f, normalPaint)
        boldPaint.textSize = 13f
        val periodW = boldPaint.measureText(periodLabel)
        canvas.drawText(periodLabel, MARGIN_R - periodW, 40f, boldPaint)
        y = 96f

        // ── Resumen financiero ────────────────────────────────────────────────
        val totalFacturado = invoices.sumOf { it.totalAmount }
        val totalCobrado   = invoices.filter { it.paymentStatus == PaymentStatus.PAID }.sumOf { it.totalAmount }
        val totalPendiente = invoices.filter { it.paymentStatus == PaymentStatus.PENDING }.sumOf { it.totalAmount }

        boldPaint.color = Color.rgb(21, 101, 192); boldPaint.textSize = 12f
        canvas.drawText("RESUMEN FINANCIERO", MARGIN_L, y, boldPaint)
        y += 6f
        canvas.drawLine(MARGIN_L, y, MARGIN_R, y, linePaint.apply { color = Color.rgb(21, 101, 192); strokeWidth = 2f })
        y += 22f

        val cardW = (MARGIN_R - MARGIN_L - 16f) / 3f
        drawSummaryCard(canvas, MARGIN_L, y, cardW, "Total facturado", "$${"%.2f".format(totalFacturado)}", Color.rgb(21, 101, 192))
        drawSummaryCard(canvas, MARGIN_L + cardW + 8f, y, cardW, "Cobrado", "$${"%.2f".format(totalCobrado)}", Color.rgb(46, 125, 50))
        drawSummaryCard(canvas, MARGIN_L + (cardW + 8f) * 2, y, cardW, "Pendiente de cobro", "$${"%.2f".format(totalPendiente)}", Color.rgb(230, 81, 0))
        y += 70f

        // ── Resumen de servicios ──────────────────────────────────────────────
        val completedServices = services.filter { it.status == com.gestion.itinerario.data.entity.ServiceStatus.COMPLETED }
        val byType = ServiceType.values().associateWith { t -> completedServices.count { it.type == t } }

        boldPaint.color = Color.rgb(21, 101, 192); boldPaint.textSize = 12f
        canvas.drawText("SERVICIOS COMPLETADOS (${completedServices.size})", MARGIN_L, y, boldPaint)
        y += 6f
        canvas.drawLine(MARGIN_L, y, MARGIN_R, y, linePaint.apply { color = Color.rgb(21, 101, 192); strokeWidth = 2f })
        y += 18f

        normalPaint.color = Color.DKGRAY; normalPaint.textSize = 10f
        val typeLabels = mapOf(
            ServiceType.MAINTENANCE to "Mantenimientos",
            ServiceType.REPAIR to "Reparaciones",
            ServiceType.INSTALLATION to "Instalaciones"
        )
        var tx = MARGIN_L
        typeLabels.forEach { (type, label) ->
            val text = "$label: ${byType[type] ?: 0}"
            canvas.drawText(text, tx, y, normalPaint)
            tx += normalPaint.measureText(text) + 30f
        }
        y += 26f

        // ── Detalle de facturación ────────────────────────────────────────────
        boldPaint.color = Color.rgb(21, 101, 192); boldPaint.textSize = 12f
        canvas.drawText("DETALLE DE FACTURAS (${invoices.size})", MARGIN_L, y, boldPaint)
        y += 6f
        canvas.drawLine(MARGIN_L, y, MARGIN_R, y, linePaint.apply { color = Color.rgb(21, 101, 192); strokeWidth = 2f })
        y += 4f

        val colDateX   = MARGIN_R - 360f
        val colClientX = MARGIN_R - 290f
        val colAmtX    = MARGIN_R - 130f
        val colStatX   = MARGIN_R - 60f

        fun drawTableHeader() {
            canvas.drawRect(MARGIN_L, y, MARGIN_R, y + 22f, Paint().apply { color = Color.rgb(21, 101, 192) })
            headerPaint.textSize = 9f
            canvas.drawText("N° FACTURA", MARGIN_L + 8f, y + 15f, headerPaint)
            canvas.drawText("FECHA", colDateX, y + 15f, headerPaint)
            canvas.drawText("CLIENTE", colClientX, y + 15f, headerPaint)
            canvas.drawText("MONTO", colAmtX, y + 15f, headerPaint)
            canvas.drawText("ESTADO", colStatX, y + 15f, headerPaint)
            y += 26f
        }

        if (invoices.isEmpty()) {
            normalPaint.color = Color.GRAY; normalPaint.textSize = 10f
            canvas.drawText("No se generaron facturas en este período.", MARGIN_L + 4f, y + 14f, normalPaint)
            y += 24f
        } else {
            drawTableHeader()
            invoices.sortedBy { it.endDate.takeIf { d -> d > 0 } ?: it.createdAt }.forEachIndexed { i, inv ->
                ensureSpace(20f)
                if (y < 80f) drawTableHeader() // se reinició página y ya no hay encabezado dibujado
                val bg = if (i % 2 == 0) Color.rgb(240, 248, 255) else Color.WHITE
                canvas.drawRect(MARGIN_L, y - 4f, MARGIN_R, y + 14f, Paint().apply { color = bg })
                normalPaint.color = Color.BLACK; normalPaint.textSize = 9f
                canvas.drawText(inv.invoiceNumber.take(18), MARGIN_L + 8f, y + 8f, normalPaint)
                val date = inv.endDate.takeIf { d -> d > 0 } ?: inv.createdAt
                canvas.drawText(sdf.format(Date(date)), colDateX, y + 8f, normalPaint)
                canvas.drawText(inv.clientName.take(22), colClientX, y + 8f, normalPaint)
                val amtStr = "$${"%.2f".format(inv.totalAmount)}"
                canvas.drawText(amtStr, colAmtX, y + 8f, normalPaint)
                val (statusColor, statusText) = when (inv.paymentStatus) {
                    PaymentStatus.PAID -> Color.rgb(46, 125, 50) to "PAGADO"
                    PaymentStatus.PENDING -> Color.rgb(230, 81, 0) to "PENDIENTE"
                    else -> Color.GRAY to "S/E"
                }
                normalPaint.color = statusColor
                canvas.drawText(statusText, colStatX, y + 8f, normalPaint)
                y += 18f
            }
        }

        // ── Pie de página final ───────────────────────────────────────────────
        ensureSpace(40f)
        y += 16f
        canvas.drawLine(MARGIN_L, y, MARGIN_R, y, linePaint.apply { color = Color.LTGRAY; strokeWidth = 1f })
        y += 16f
        normalPaint.color = Color.DKGRAY; normalPaint.textSize = 9f
        canvas.drawText("Generado el ${sdf.format(Date())} · ${profile.companyName.ifBlank { "Mi Empresa" }}",
            MARGIN_L, y, normalPaint)

        document.finishPage(page)

        val fileName = "Reporte_${monthNames[month]}_${year}.pdf"
        val file = File(context.getExternalFilesDir("Reportes"), fileName)
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun drawSummaryCard(canvas: Canvas, x: Float, y: Float, w: Float, label: String, value: String, color: Int) {
        canvas.drawRoundRect(RectF(x, y, x + w, y + 56f), 8f, 8f, Paint().apply {
            this.color = Color.argb(28, Color.red(color), Color.green(color), Color.blue(color))
        })
        val labelPaint = Paint().apply { typeface = Typeface.DEFAULT; textSize = 9f; this.color = Color.DKGRAY; isAntiAlias = true }
        val valuePaint = Paint().apply { typeface = Typeface.DEFAULT_BOLD; textSize = 14f; this.color = color; isAntiAlias = true }
        canvas.drawText(label, x + 10f, y + 20f, labelPaint)
        canvas.drawText(value, x + 10f, y + 42f, valuePaint)
    }

    fun shareViaWhatsApp(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
        } catch (_: Exception) {
            context.startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }, "Compartir reporte"
            ))
        }
    }
}
