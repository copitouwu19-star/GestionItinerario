package com.gestion.itinerario.ui.reports

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.util.Base64
import androidx.core.content.FileProvider
import com.gestion.itinerario.data.entity.Appointment
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

    private val typeLabels = mapOf(
        ServiceType.MAINTENANCE  to "Mantenimientos",
        ServiceType.REPAIR       to "Reparaciones",
        ServiceType.INSTALLATION to "Instalaciones"
    )

    private fun String.xmlEscape() =
        replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    // ─────────────────────────────────────────────────────────────────────────
    // PDF
    // ─────────────────────────────────────────────────────────────────────────
    fun generate(
        context: Context,
        profile: CompanyProfile,
        year: Int,
        month: Int,
        invoices: List<Invoice>,
        appointments: List<Appointment>,  // citas completadas del período
        services: List<ServiceOrder>      // órdenes completadas del período
    ): File {
        val document = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val boldPaint   = Paint().apply { typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val normalPaint = Paint().apply { typeface = Typeface.DEFAULT; isAntiAlias = true }
        val headerPaint = Paint().apply { typeface = Typeface.DEFAULT_BOLD; textSize = 10f; color = Color.WHITE; isAntiAlias = true }
        val linePaint   = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        val themeBlue   = Color.rgb(21, 101, 192)
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
        fun ensureSpace(needed: Float) { if (y + needed > CONTENT_BOTTOM) newPage() }

        // ── Encabezado: Logo + Datos empresa ─────────────────────────────────
        var infoX = MARGIN_L
        val logoBitmap: Bitmap? = if (profile.logoUrl.isNotBlank()) {
            try {
                val bytes = Base64.decode(profile.logoUrl.substringAfter("base64,"), Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) { null }
        } else null
        if (logoBitmap != null) {
            val logoH = 60f
            val logoW = (logoBitmap.width.toFloat() / logoBitmap.height.toFloat()) * logoH
            canvas.drawBitmap(Bitmap.createScaledBitmap(logoBitmap, logoW.toInt(), logoH.toInt(), true), MARGIN_L, 10f, null)
            infoX = MARGIN_L + logoW + 14f
        }
        boldPaint.color = themeBlue; boldPaint.textSize = 17f
        canvas.drawText(profile.companyName.ifBlank { "Mi Empresa" }, infoX, 30f, boldPaint)
        normalPaint.color = Color.DKGRAY; normalPaint.textSize = 10f
        val rifTelLine = buildString {
            if (profile.taxId.isNotBlank()) append("NIT: ${profile.taxId}")
            if (profile.phone.isNotBlank()) { if (isNotEmpty()) append("  ·  "); append("Tel: ${profile.phone}") }
        }
        if (rifTelLine.isNotBlank()) canvas.drawText(rifTelLine, infoX, 46f, normalPaint)
        if (profile.address.isNotBlank()) {
            normalPaint.textSize = 9f
            canvas.drawText(profile.address.take(60), infoX, 60f, normalPaint)
        }
        canvas.drawLine(MARGIN_L, 76f, MARGIN_R, 76f, linePaint.apply { color = themeBlue; strokeWidth = 2f })
        boldPaint.color = themeBlue; boldPaint.textSize = 13f
        canvas.drawText("REPORTE MENSUAL DE SERVICIOS", MARGIN_L, 93f, boldPaint)
        normalPaint.color = Color.rgb(60, 60, 60); normalPaint.textSize = 11f
        canvas.drawText(periodLabel, MARGIN_L, 108f, normalPaint)
        y = 130f

        // ── Resumen financiero ────────────────────────────────────────────────
        val totalFacturado = invoices.sumOf { it.totalAmount }
        val totalCobrado   = invoices.filter { it.paymentStatus == PaymentStatus.PAID }.sumOf { it.totalAmount }
        val totalPendiente = invoices.filter { it.paymentStatus == PaymentStatus.PENDING }.sumOf { it.totalAmount }

        boldPaint.color = themeBlue; boldPaint.textSize = 12f
        canvas.drawText("RESUMEN FINANCIERO", MARGIN_L, y, boldPaint); y += 6f
        canvas.drawLine(MARGIN_L, y, MARGIN_R, y, linePaint.apply { color = themeBlue; strokeWidth = 2f }); y += 22f

        val cardW = (MARGIN_R - MARGIN_L - 16f) / 3f
        drawSummaryCard(canvas, MARGIN_L,                  y, cardW, "Total facturado",    "$${"%.2f".format(totalFacturado)}", Color.rgb(21, 101, 192))
        drawSummaryCard(canvas, MARGIN_L + cardW + 8f,     y, cardW, "Cobrado",            "$${"%.2f".format(totalCobrado)}",   Color.rgb(46, 125, 50))
        drawSummaryCard(canvas, MARGIN_L + (cardW + 8f)*2, y, cardW, "Pendiente de cobro", "$${"%.2f".format(totalPendiente)}", Color.rgb(230, 81, 0))
        y += 70f

        // ── Resumen de servicios (appointments + orders) ─────────────────────
        val totalServices = appointments.size + services.size
        val byType = ServiceType.values().associateWith { t ->
            appointments.count { it.serviceType == t } + services.count { it.type == t }
        }
        boldPaint.color = themeBlue; boldPaint.textSize = 12f
        canvas.drawText("SERVICIOS COMPLETADOS ($totalServices)", MARGIN_L, y, boldPaint); y += 6f
        canvas.drawLine(MARGIN_L, y, MARGIN_R, y, linePaint.apply { color = themeBlue; strokeWidth = 2f }); y += 18f

        normalPaint.color = Color.DKGRAY; normalPaint.textSize = 10f
        var tx = MARGIN_L
        typeLabels.forEach { (type, label) ->
            val text = "$label: ${byType[type] ?: 0}"
            canvas.drawText(text, tx, y, normalPaint)
            tx += normalPaint.measureText(text) + 30f
        }
        y += 26f

        // ── Detalle de facturas ────────────────────────────────────────────────
        boldPaint.color = themeBlue; boldPaint.textSize = 12f
        canvas.drawText("DETALLE DE FACTURAS (${invoices.size})", MARGIN_L, y, boldPaint); y += 6f
        canvas.drawLine(MARGIN_L, y, MARGIN_R, y, linePaint.apply { color = themeBlue; strokeWidth = 2f }); y += 4f

        val colDateX   = MARGIN_R - 360f
        val colClientX = MARGIN_R - 290f
        val colAmtX    = MARGIN_R - 130f
        val colStatX   = MARGIN_R - 60f

        fun drawTableHeader() {
            canvas.drawRect(MARGIN_L, y, MARGIN_R, y + 22f, Paint().apply { color = themeBlue })
            headerPaint.textSize = 9f
            canvas.drawText("N° FACTURA", MARGIN_L + 8f, y + 15f, headerPaint)
            canvas.drawText("FECHA",  colDateX,   y + 15f, headerPaint)
            canvas.drawText("CLIENTE",colClientX, y + 15f, headerPaint)
            canvas.drawText("MONTO",  colAmtX,    y + 15f, headerPaint)
            canvas.drawText("ESTADO", colStatX,   y + 15f, headerPaint)
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
                if (y < 80f) drawTableHeader()
                val bg = if (i % 2 == 0) Color.rgb(240, 248, 255) else Color.WHITE
                canvas.drawRect(MARGIN_L, y - 4f, MARGIN_R, y + 14f, Paint().apply { color = bg })
                normalPaint.color = Color.BLACK; normalPaint.textSize = 9f
                canvas.drawText(inv.invoiceNumber.take(18), MARGIN_L + 8f, y + 8f, normalPaint)
                canvas.drawText(sdf.format(Date(inv.endDate.takeIf { d -> d > 0 } ?: inv.createdAt)), colDateX, y + 8f, normalPaint)
                canvas.drawText(inv.clientName.take(22), colClientX, y + 8f, normalPaint)
                canvas.drawText("$${"%.2f".format(inv.totalAmount)}", colAmtX, y + 8f, normalPaint)
                val (statusColor, statusText) = when (inv.paymentStatus) {
                    PaymentStatus.PAID    -> Color.rgb(46, 125, 50) to "PAGADO"
                    PaymentStatus.PENDING -> Color.rgb(230, 81, 0)  to "PENDIENTE"
                    else                  -> Color.GRAY              to "S/E"
                }
                normalPaint.color = statusColor
                canvas.drawText(statusText, colStatX, y + 8f, normalPaint)
                y += 18f
            }
        }

        // ── Pie de página ─────────────────────────────────────────────────────
        ensureSpace(40f); y += 16f
        canvas.drawLine(MARGIN_L, y, MARGIN_R, y, linePaint.apply { color = Color.LTGRAY; strokeWidth = 1f }); y += 16f
        normalPaint.color = Color.DKGRAY; normalPaint.textSize = 9f
        canvas.drawText("Generado el ${sdf.format(Date())} · ${profile.companyName.ifBlank { "Mi Empresa" }}", MARGIN_L, y, normalPaint)

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
        val labelPaint = Paint().apply { typeface = Typeface.DEFAULT;      textSize = 9f;  this.color = Color.DKGRAY; isAntiAlias = true }
        val valuePaint = Paint().apply { typeface = Typeface.DEFAULT_BOLD; textSize = 14f; this.color = color;        isAntiAlias = true }
        canvas.drawText(label, x + 10f, y + 20f, labelPaint)
        canvas.drawText(value, x + 10f, y + 42f, valuePaint)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Excel (SpreadsheetML — sin dependencias, Excel lo abre nativo)
    // ─────────────────────────────────────────────────────────────────────────
    fun generateXls(
        context: Context,
        profile: CompanyProfile,
        year: Int,
        month: Int,
        invoices: List<Invoice>,
        appointments: List<Appointment>,
        services: List<ServiceOrder>
    ): File {
        val periodLabel   = "${monthNames[month]} $year"
        val totalFacturado = invoices.sumOf { it.totalAmount }
        val totalCobrado   = invoices.filter { it.paymentStatus == PaymentStatus.PAID }.sumOf { it.totalAmount }
        val totalPendiente = invoices.filter { it.paymentStatus == PaymentStatus.PENDING }.sumOf { it.totalAmount }
        val totalServices  = appointments.size + services.size
        val byType = ServiceType.values().associateWith { t ->
            appointments.count { it.serviceType == t } + services.count { it.type == t }
        }

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:x="urn:schemas-microsoft-com:office:excel">
 <Styles>
  <Style ss:ID="s_title">
   <Font ss:Bold="1" ss:Size="16" ss:Color="#1565C0"/>
  </Style>
  <Style ss:ID="s_period">
   <Font ss:Bold="1" ss:Size="11" ss:Color="#555555"/>
  </Style>
  <Style ss:ID="s_info">
   <Font ss:Size="10" ss:Color="#333333"/>
  </Style>
  <Style ss:ID="s_section">
   <Font ss:Bold="1" ss:Size="11" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#1565C0" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="s_col_hdr">
   <Font ss:Bold="1" ss:Size="10" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#1976D2" ss:Pattern="Solid"/>
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="s_col_hdr_r">
   <Font ss:Bold="1" ss:Size="10" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#1976D2" ss:Pattern="Solid"/>
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
  </Style>
  <Style ss:ID="s_even">
   <Interior ss:Color="#E3F2FD" ss:Pattern="Solid"/>
   <Font ss:Size="10"/>
  </Style>
  <Style ss:ID="s_odd">
   <Interior ss:Color="#FFFFFF" ss:Pattern="Solid"/>
   <Font ss:Size="10"/>
  </Style>
  <Style ss:ID="s_money_even">
   <Interior ss:Color="#E3F2FD" ss:Pattern="Solid"/>
   <Font ss:Size="10"/>
   <NumberFormat ss:Format="#,##0.00"/>
   <Alignment ss:Horizontal="Right"/>
  </Style>
  <Style ss:ID="s_money_odd">
   <Interior ss:Color="#FFFFFF" ss:Pattern="Solid"/>
   <Font ss:Size="10"/>
   <NumberFormat ss:Format="#,##0.00"/>
   <Alignment ss:Horizontal="Right"/>
  </Style>
  <Style ss:ID="s_total_lbl">
   <Font ss:Bold="1" ss:Size="11" ss:Color="#1565C0"/>
   <Interior ss:Color="#BBDEFB" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="s_total_num">
   <Font ss:Bold="1" ss:Size="11" ss:Color="#1565C0"/>
   <Interior ss:Color="#BBDEFB" ss:Pattern="Solid"/>
   <NumberFormat ss:Format="#,##0.00"/>
   <Alignment ss:Horizontal="Right"/>
  </Style>
  <Style ss:ID="s_paid_even">
   <Font ss:Bold="1" ss:Color="#2E7D32" ss:Size="10"/>
   <Interior ss:Color="#E3F2FD" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="s_paid_odd">
   <Font ss:Bold="1" ss:Color="#2E7D32" ss:Size="10"/>
   <Interior ss:Color="#FFFFFF" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="s_pend_even">
   <Font ss:Bold="1" ss:Color="#E65100" ss:Size="10"/>
   <Interior ss:Color="#E3F2FD" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="s_pend_odd">
   <Font ss:Bold="1" ss:Color="#E65100" ss:Size="10"/>
   <Interior ss:Color="#FFFFFF" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="s_se_even">
   <Font ss:Color="#777777" ss:Size="10"/>
   <Interior ss:Color="#E3F2FD" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="s_se_odd">
   <Font ss:Color="#777777" ss:Size="10"/>
   <Interior ss:Color="#FFFFFF" ss:Pattern="Solid"/>
  </Style>
 </Styles>
 <Worksheet ss:Name="${monthNames[month]} $year">
  <Table>
   <Column ss:Width="170"/>
   <Column ss:Width="85"/>
   <Column ss:Width="150"/>
   <Column ss:Width="110"/>
   <Column ss:Width="80"/>
   <Column ss:Width="80"/>
""")

        fun cell(styleId: String, type: String, value: String) =
            "    <Cell ss:StyleID=\"$styleId\"><Data ss:Type=\"$type\">${value.xmlEscape()}</Data></Cell>\n"
        fun cellNum(styleId: String, value: Double) =
            "    <Cell ss:StyleID=\"$styleId\"><Data ss:Type=\"Number\">$value</Data></Cell>\n"
        fun cellMerged(styleId: String, merge: Int, value: String) =
            "    <Cell ss:StyleID=\"$styleId\" ss:MergeAcross=\"$merge\"><Data ss:Type=\"String\">${value.xmlEscape()}</Data></Cell>\n"
        fun row(height: Int, content: String) = "   <Row ss:Height=\"$height\">\n$content   </Row>\n"
        fun emptyRow() = "   <Row ss:Height=\"10\"/>\n"

        // Título
        sb.append(row(30, cellMerged("s_title",  5, "REPORTE MENSUAL DE SERVICIOS")))
        sb.append(row(20, cellMerged("s_period", 5, "$periodLabel — ${profile.companyName}")))

        if (profile.taxId.isNotBlank())   sb.append(row(16, cell("s_info", "String", "NIT: ${profile.taxId}")))
        if (profile.phone.isNotBlank())   sb.append(row(16, cell("s_info", "String", "Teléfono: ${profile.phone}")))
        if (profile.address.isNotBlank()) sb.append(row(16, cell("s_info", "String", "Dirección: ${profile.address}")))

        sb.append(emptyRow())

        // Resumen financiero
        sb.append(row(22, cellMerged("s_section", 5, "  RESUMEN FINANCIERO")))
        sb.append(row(20, cell("s_col_hdr", "String", "Concepto") + cell("s_col_hdr_r", "String", "Monto")))
        sb.append(row(18, cell("s_even", "String", "Total Facturado")    + cellNum("s_money_even", totalFacturado)))
        sb.append(row(18, cell("s_odd",  "String", "Total Cobrado")      + cellNum("s_money_odd",  totalCobrado)))
        sb.append(row(18, cell("s_even", "String", "Pendiente de Cobro") + cellNum("s_money_even", totalPendiente)))

        sb.append(emptyRow())

        // Servicios completados
        sb.append(row(22, cellMerged("s_section", 5, "  SERVICIOS COMPLETADOS ($totalServices)")))
        sb.append(row(20, cell("s_col_hdr", "String", "Tipo") + cell("s_col_hdr_r", "String", "Cantidad")))
        typeLabels.entries.forEachIndexed { i, (type, label) ->
            val bg = if (i % 2 == 0) "s_even" else "s_odd"
            sb.append(row(18, cell(bg, "String", label) + cell(bg, "Number", (byType[type] ?: 0).toString())))
        }

        sb.append(emptyRow())

        // Detalle de facturas
        val sorted = invoices.sortedBy { it.endDate.takeIf { d -> d > 0 } ?: it.createdAt }
        sb.append(row(22, cellMerged("s_section", 5, "  DETALLE DE FACTURAS (${invoices.size})")))
        sb.append(row(20,
            cell("s_col_hdr",   "String", "N° Factura") +
            cell("s_col_hdr",   "String", "Fecha") +
            cell("s_col_hdr",   "String", "Cliente") +
            cell("s_col_hdr",   "String", "Teléfono") +
            cell("s_col_hdr_r", "String", "Monto") +
            cell("s_col_hdr",   "String", "Estado")
        ))

        sorted.forEachIndexed { i, inv ->
            val even = i % 2 == 0
            val bg   = if (even) "s_even" else "s_odd"
            val mBg  = if (even) "s_money_even" else "s_money_odd"
            val date = sdf.format(Date(inv.endDate.takeIf { d -> d > 0 } ?: inv.createdAt))
            val (statusStyle, statusText) = when (inv.paymentStatus) {
                PaymentStatus.PAID    -> (if (even) "s_paid_even" else "s_paid_odd") to "PAGADO"
                PaymentStatus.PENDING -> (if (even) "s_pend_even" else "s_pend_odd") to "PENDIENTE"
                else                  -> (if (even) "s_se_even"   else "s_se_odd")   to "S/E"
            }
            sb.append(row(18,
                cell(bg,          "String", inv.invoiceNumber) +
                cell(bg,          "String", date) +
                cell(bg,          "String", inv.clientName) +
                cell(bg,          "String", inv.clientPhone) +
                cellNum(mBg, inv.totalAmount) +
                cell(statusStyle, "String", statusText)
            ))
        }

        // Fila total
        if (invoices.isNotEmpty()) {
            val grandTotal = invoices.sumOf { it.totalAmount }
            sb.append(row(22,
                "    <Cell ss:StyleID=\"s_total_lbl\" ss:MergeAcross=\"3\"><Data ss:Type=\"String\">TOTAL</Data></Cell>\n" +
                cellNum("s_total_num", grandTotal) +
                cell("s_total_lbl", "String", "")
            ))
        }

        sb.append("  </Table>\n </Worksheet>\n</Workbook>")

        val fileName = "Reporte_${monthNames[month]}_${year}.xls"
        val file = File(context.getExternalFilesDir("Reportes"), fileName)
        file.parentFile?.mkdirs()
        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Compartir / Abrir
    // ─────────────────────────────────────────────────────────────────────────
    fun openPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Abrir PDF"))
        } catch (_: Exception) {
            android.widget.Toast.makeText(context, "No hay app para abrir PDF instalada.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun shareExcel(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.ms-excel")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Abrir en Excel"))
        } catch (_: Exception) {
            android.widget.Toast.makeText(context,
                "Instala Excel o WPS Office para abrir el archivo.", android.widget.Toast.LENGTH_LONG).show()
        }
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
