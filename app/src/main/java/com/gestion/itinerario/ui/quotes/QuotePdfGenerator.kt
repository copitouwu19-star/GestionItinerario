package com.gestion.itinerario.ui.quotes

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.util.Base64
import androidx.core.content.FileProvider
import com.gestion.itinerario.data.entity.CompanyProfile
import com.gestion.itinerario.data.entity.Quote
import com.gestion.itinerario.data.entity.QuoteStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object QuotePdfGenerator {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun generate(context: Context, quote: Quote, profile: CompanyProfile, logoBitmap: Bitmap? = null): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val boldPaint   = Paint().apply { typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val normalPaint = Paint().apply { typeface = Typeface.DEFAULT; isAntiAlias = true }
        val headerPaint = Paint().apply { typeface = Typeface.DEFAULT_BOLD; textSize = 10f; color = Color.WHITE; isAntiAlias = true }
        val linePaint   = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        val themeBlue   = Color.rgb(21, 101, 192)

        var y = 40f
        val marginL = 40f
        val marginR = 555f

        // ── Encabezado: Logo izquierda + Datos empresa derecha ─────────────────
        var infoX = marginL

        if (logoBitmap != null) {
            val logoH = 60f
            val logoW = (logoBitmap.width.toFloat() / logoBitmap.height.toFloat()) * logoH
            val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoW.toInt(), logoH.toInt(), true)
            canvas.drawBitmap(scaledLogo, marginL, 10f, null)
            infoX = marginL + logoW + 14f
        }

        boldPaint.color = themeBlue; boldPaint.textSize = 17f
        canvas.drawText(profile.companyName.ifBlank { "Mi Empresa" }, infoX, 30f, boldPaint)
        normalPaint.color = Color.DKGRAY; normalPaint.textSize = 10f
        val taxLine = buildString {
            if (profile.taxId.isNotBlank()) append("NIT: ${profile.taxId}")
            if (profile.phone.isNotBlank()) { if (isNotEmpty()) append("  ·  "); append("Tel: ${profile.phone}") }
        }
        if (taxLine.isNotBlank()) canvas.drawText(taxLine, infoX, 46f, normalPaint)
        if (profile.address.isNotBlank()) {
            normalPaint.textSize = 9f
            canvas.drawText(profile.address.take(60), infoX, 60f, normalPaint)
        }

        canvas.drawLine(marginL, 76f, marginR, 76f, linePaint.apply { color = themeBlue; strokeWidth = 2f })

        boldPaint.color = themeBlue; boldPaint.textSize = 13f
        canvas.drawText("COTIZACIÓN DE SERVICIO", marginL, 93f, boldPaint)
        normalPaint.color = Color.rgb(60, 60, 60); normalPaint.textSize = 11f
        canvas.drawText(quote.quoteNumber, marginL, 108f, normalPaint)
        normalPaint.color = Color.DKGRAY; normalPaint.textSize = 10f
        canvas.drawText("Fecha: ${sdf.format(Date(quote.createdAt))}", marginL, 121f, normalPaint)

        y = 138f

        // ── Validez ───────────────────────────────────────────────────────────
        if (quote.validUntil > 0L) {
            canvas.drawRect(marginL, y, marginR, y + 26f, Paint().apply { color = Color.rgb(245, 245, 245) })
            boldPaint.color = themeBlue; boldPaint.textSize = 10f
            canvas.drawText("Esta cotización es válida hasta el ${sdf.format(Date(quote.validUntil))}",
                marginL + 8f, y + 17f, boldPaint)
            y += 26f + 14f
        }

        // ── Datos del cliente ─────────────────────────────────────────────────
        boldPaint.color = themeBlue; boldPaint.textSize = 12f
        canvas.drawText("DATOS DEL CLIENTE", marginL, y, boldPaint)
        y += 4f
        canvas.drawLine(marginL, y, marginR, y, linePaint.apply { color = themeBlue; strokeWidth = 2f })
        y += 16f
        normalPaint.color = Color.BLACK; normalPaint.textSize = 11f
        canvas.drawText("Cliente: ${quote.clientName}", marginL, y, normalPaint); y += 16f
        if (quote.clientPhone.isNotBlank()) { canvas.drawText("Teléfono: ${quote.clientPhone}", marginL, y, normalPaint); y += 16f }
        if (quote.clientAddress.isNotBlank()) { canvas.drawText("Dirección: ${quote.clientAddress.take(60)}", marginL, y, normalPaint); y += 16f }
        if (quote.equipmentType.isNotBlank()) { canvas.drawText("Equipo: ${quote.equipmentType}", marginL, y, normalPaint); y += 16f }
        if (quote.description.isNotBlank()) { canvas.drawText("Descripción: ${quote.description.take(70)}", marginL, y, normalPaint); y += 16f }
        y += 8f

        // ── Tabla de ítems ────────────────────────────────────────────────────
        boldPaint.color = themeBlue; boldPaint.textSize = 12f
        canvas.drawText("DETALLE ESTIMADO", marginL, y, boldPaint)
        y += 6f
        canvas.drawLine(marginL, y, marginR, y, linePaint.apply { color = themeBlue; strokeWidth = 2f })
        y += 4f

        val colQtyX = marginR - 220f
        val colUPX  = marginR - 150f
        val colTotX = marginR - 75f

        canvas.drawRect(marginL, y, marginR, y + 24f, Paint().apply { color = themeBlue })
        headerPaint.textSize = 10f
        canvas.drawText("DESCRIPCIÓN",  marginL + 8f, y + 16f, headerPaint)
        canvas.drawText("CANT.",        colQtyX,      y + 16f, headerPaint)
        canvas.drawText("VLR. UNIT.",   colUPX,       y + 16f, headerPaint)
        canvas.drawText("VLR. TOTAL",   colTotX,      y + 16f, headerPaint)
        y += 28f

        quote.items.forEachIndexed { i, item ->
            val bg = if (i % 2 == 0) Color.rgb(240, 248, 255) else Color.WHITE
            canvas.drawRect(marginL, y - 4f, marginR, y + 16f, Paint().apply { color = bg })
            normalPaint.color = Color.BLACK; normalPaint.textSize = 10f
            canvas.drawText(item.description.take(42), marginL + 8f, y + 10f, normalPaint)
            canvas.drawText(String.format("%.0f", item.quantity),  colQtyX, y + 10f, normalPaint)
            canvas.drawText(String.format("%.2f", item.unitPrice), colUPX,  y + 10f, normalPaint)
            canvas.drawText("\$${String.format("%.2f", item.amount)}", colTotX, y + 10f, normalPaint)
            y += 20f
        }
        canvas.drawLine(marginL, y, marginR, y, linePaint.apply { color = Color.LTGRAY; strokeWidth = 1f })
        y += 16f

        // ── Total estimado ────────────────────────────────────────────────────
        val boxW = 220f
        val boxL = marginR - boxW
        canvas.drawRect(boxL, y, marginR, y + 30f, Paint().apply { color = themeBlue })
        headerPaint.textSize = 13f
        canvas.drawText("TOTAL ESTIMADO", boxL + 10f, y + 20f, headerPaint)
        val totalStr = "\$${String.format("%.2f", quote.totalAmount)}"
        canvas.drawText(totalStr, marginR - headerPaint.measureText(totalStr) - 10f, y + 20f, headerPaint)
        y += 30f + 20f

        // ── Estado / aprobación ───────────────────────────────────────────────
        val (statusColor, statusText) = when (quote.status) {
            QuoteStatus.APPROVED -> Color.rgb(46, 125, 50)  to "APROBADA POR EL CLIENTE"
            QuoteStatus.REJECTED -> Color.rgb(198, 40, 40)  to "RECHAZADA POR EL CLIENTE"
            QuoteStatus.PENDING  -> Color.rgb(120, 100, 0)  to "PENDIENTE DE APROBACIÓN"
        }
        canvas.drawRect(marginL, y, marginR, y + 30f, Paint().apply {
            color = Color.argb(22, Color.red(statusColor), Color.green(statusColor), Color.blue(statusColor))
        })
        boldPaint.color = statusColor; boldPaint.textSize = 12f
        canvas.drawText("Estado: $statusText", marginL + 10f, y + 20f, boldPaint)
        quote.respondedAt?.let {
            normalPaint.color = Color.DKGRAY; normalPaint.textSize = 9f
            val respText = "El ${sdf.format(Date(it))}"
            canvas.drawText(respText, marginR - normalPaint.measureText(respText) - 10f, y + 20f, normalPaint)
        }
        y += 30f + 20f

        // ── Firma del cliente ─────────────────────────────────────────────────
        if (quote.clientSignature.isNotBlank()) {
            boldPaint.color = themeBlue; boldPaint.textSize = 11f
            canvas.drawText("FIRMA DE APROBACIÓN DEL CLIENTE", marginL, y, boldPaint)
            y += 8f
            try {
                val sigBytes = Base64.decode(quote.clientSignature, Base64.DEFAULT)
                val sigBmp = BitmapFactory.decodeByteArray(sigBytes, 0, sigBytes.size)
                if (sigBmp != null) {
                    val sigH = 80f
                    val sigW = (sigBmp.width.toFloat() / sigBmp.height.toFloat()) * sigH
                    canvas.drawBitmap(Bitmap.createScaledBitmap(sigBmp, sigW.toInt(), sigH.toInt(), true), marginL, y, null)
                }
            } catch (_: Exception) {}
            y += 90f
            canvas.drawLine(marginL, y, marginL + 160f, y, linePaint.apply { color = Color.DKGRAY; strokeWidth = 1f })
            y += 12f
            normalPaint.color = Color.DKGRAY; normalPaint.textSize = 9f
            canvas.drawText(quote.clientName, marginL, y, normalPaint)
        }

        // ── Pie de página ─────────────────────────────────────────────────────
        normalPaint.color = Color.rgb(100, 100, 100); normalPaint.textSize = 8f
        canvas.drawLine(marginL, page.info.pageHeight - 30f, marginR, page.info.pageHeight - 30f,
            linePaint.apply { color = Color.LTGRAY; strokeWidth = 1f })
        canvas.drawText("Cotización generada por ${profile.companyName.ifBlank { "Mi Empresa" }} · Sujeta a revisión final al momento del servicio",
            marginL, page.info.pageHeight - 18f, normalPaint)

        document.finishPage(page)

        val fileName = "Cotizacion_${quote.quoteNumber.replace("-", "_")}.pdf"
        val file = File(context.getExternalFilesDir("Cotizaciones"), fileName)
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun shareViaWhatsApp(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(whatsappIntent)
        } catch (_: Exception) {
            context.startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }, "Compartir cotización"
            ))
        }
    }

    fun openPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            android.widget.Toast.makeText(context, "PDF guardado en: ${file.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}
