package com.gestion.itinerario.ui.invoice

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.gestion.itinerario.data.entity.CompanyProfile
import com.gestion.itinerario.data.entity.Invoice
import com.gestion.itinerario.data.entity.PaymentMethod
import com.gestion.itinerario.data.entity.PaymentStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object InvoicePdfGenerator {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun generate(context: Context, invoice: Invoice, profile: CompanyProfile, logoBitmap: Bitmap? = null): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val boldPaint = Paint().apply { typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val normalPaint = Paint().apply { typeface = Typeface.DEFAULT; isAntiAlias = true }
        val smallPaint = Paint().apply { typeface = Typeface.DEFAULT; textSize = 10f; isAntiAlias = true }
        val titlePaint = Paint().apply { typeface = Typeface.DEFAULT_BOLD; textSize = 20f; color = Color.rgb(21, 101, 192); isAntiAlias = true }
        val headerPaint = Paint().apply { typeface = Typeface.DEFAULT_BOLD; textSize = 12f; color = Color.WHITE; isAntiAlias = true }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        val bluePaint = Paint().apply { color = Color.rgb(21, 101, 192) }

        var y = 40f
        val marginL = 40f
        val marginR = 555f
        val pageW = 595f

        // ── Encabezado azul ──────────────────────────────────────────────────
        canvas.drawRect(0f, 0f, pageW, 80f, bluePaint)

        // Logo empresa (esquina superior derecha) — si existe
        val logoAreaRight = pageW - marginL
        val logoAreaLeft: Float
        if (logoBitmap != null) {
            val logoH = 60f
            val logoW = (logoBitmap.width.toFloat() / logoBitmap.height.toFloat()) * logoH
            val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoW.toInt(), logoH.toInt(), true)
            val logoLeft = pageW - logoW - marginL
            canvas.drawBitmap(scaledLogo, logoLeft, 10f, null)
            logoAreaLeft = logoLeft - 8f
        } else {
            logoAreaLeft = pageW - marginL
        }

        // Nombre empresa (izquierda)
        titlePaint.color = Color.WHITE
        titlePaint.textSize = 18f
        canvas.drawText(profile.companyName.ifBlank { "Mi Empresa" }, marginL, 35f, titlePaint)
        normalPaint.color = Color.WHITE; normalPaint.textSize = 10f
        if (profile.phone.isNotBlank()) canvas.drawText("Tel: ${profile.phone}", marginL, 52f, normalPaint)
        if (profile.address.isNotBlank()) canvas.drawText(profile.address.take(50), marginL, 66f, normalPaint)

        // Número de factura — se ubica a la izquierda del logo si hay logo, o al borde derecho
        boldPaint.color = Color.WHITE; boldPaint.textSize = 12f
        val facLabel = "FACTURA DE SERVICIO"
        val facW = boldPaint.measureText(facLabel)
        val facX = if (logoBitmap != null) marginL + (logoAreaLeft - marginL - facW) / 2f
                   else logoAreaRight - facW
        canvas.drawText(facLabel, facX.coerceAtLeast(marginL), 35f, boldPaint)
        normalPaint.textSize = 11f; normalPaint.color = Color.rgb(220, 240, 255)
        val numW = normalPaint.measureText(invoice.invoiceNumber)
        canvas.drawText(invoice.invoiceNumber, facX.coerceAtLeast(marginL), 52f, normalPaint)
        val dateStr = "Fecha: ${sdf.format(Date(invoice.endDate.takeIf { it > 0 } ?: invoice.createdAt))}"
        canvas.drawText(dateStr, facX.coerceAtLeast(marginL), 66f, normalPaint)

        y = 100f

        // ── Datos del cliente ────────────────────────────────────────────────
        boldPaint.color = Color.rgb(21, 101, 192); boldPaint.textSize = 12f
        canvas.drawText("DATOS DEL CLIENTE", marginL, y, boldPaint)
        y += 4f
        canvas.drawLine(marginL, y, marginR, y, linePaint.apply { color = Color.rgb(21, 101, 192); strokeWidth = 2f })
        y += 16f
        normalPaint.color = Color.BLACK; normalPaint.textSize = 11f
        canvas.drawText("Cliente: ${invoice.clientName}", marginL, y, normalPaint); y += 16f
        if (invoice.clientPhone.isNotBlank()) { canvas.drawText("Teléfono: ${invoice.clientPhone}", marginL, y, normalPaint); y += 16f }
        if (invoice.clientAddress.isNotBlank()) { canvas.drawText("Dirección: ${invoice.clientAddress.take(60)}", marginL, y, normalPaint); y += 16f }
        if (invoice.equipmentType.isNotBlank()) { canvas.drawText("Equipo: ${invoice.equipmentType}", marginL, y, normalPaint); y += 16f }
        y += 8f

        // Fechas de servicio
        if (invoice.startDate > 0L) {
            canvas.drawText("Inicio: ${sdf.format(Date(invoice.startDate))}", marginL, y, normalPaint)
            canvas.drawText("Finalizado: ${sdf.format(Date(invoice.endDate.takeIf { it > 0 } ?: invoice.createdAt))}", 300f, y, normalPaint)
            y += 20f
        }

        // ── Tabla de servicios ────────────────────────────────────────────────
        y += 8f
        boldPaint.color = Color.rgb(21, 101, 192); boldPaint.textSize = 12f
        canvas.drawText("SERVICIOS REALIZADOS", marginL, y, boldPaint)
        y += 6f
        canvas.drawLine(marginL, y, marginR, y, linePaint.apply { color = Color.rgb(21, 101, 192); strokeWidth = 2f })
        y += 4f

        // Header tabla
        canvas.drawRect(marginL, y, marginR, y + 24f, Paint().apply { color = Color.rgb(21, 101, 192) })
        headerPaint.textSize = 11f
        canvas.drawText("DESCRIPCIÓN", marginL + 8f, y + 16f, headerPaint)
        canvas.drawText("VALOR (USD)", marginR - 100f, y + 16f, headerPaint)
        y += 28f

        invoice.items.forEachIndexed { i, item ->
            val bg = if (i % 2 == 0) Color.rgb(240, 248, 255) else Color.WHITE
            canvas.drawRect(marginL, y - 4f, marginR, y + 16f, Paint().apply { color = bg })
            normalPaint.color = Color.BLACK; normalPaint.textSize = 10f
            canvas.drawText(item.description.take(60), marginL + 8f, y + 10f, normalPaint)
            val amtStr = "$${String.format("%.2f", item.amount)}"
            val amtW = normalPaint.measureText(amtStr)
            canvas.drawText(amtStr, marginR - amtW - 8f, y + 10f, normalPaint)
            y += 20f
        }
        if (invoice.diagnosis.isNotBlank()) {
            normalPaint.color = Color.DKGRAY; normalPaint.textSize = 9f
            canvas.drawText("Diagnóstico: ${invoice.diagnosis.take(70)}", marginL + 8f, y + 10f, normalPaint)
            y += 18f
        }
        canvas.drawLine(marginL, y, marginR, y, linePaint.apply { color = Color.LTGRAY; strokeWidth = 1f })
        y += 12f

        // ── Total ─────────────────────────────────────────────────────────────
        boldPaint.color = Color.BLACK; boldPaint.textSize = 14f
        val totalStr = "TOTAL: \$${String.format("%.2f", invoice.totalAmount)}"
        val totalW = boldPaint.measureText(totalStr)
        canvas.drawText(totalStr, marginR - totalW, y, boldPaint)
        y += 20f

        // Método y estado pago
        normalPaint.color = Color.DKGRAY; normalPaint.textSize = 10f
        val methodStr = "Método de pago: ${when(invoice.paymentMethod) { PaymentMethod.CASH -> "Efectivo"; PaymentMethod.TRANSFER -> "Transferencia"; else -> "No especificado" }}"
        canvas.drawText(methodStr, marginR - normalPaint.measureText(methodStr), y, normalPaint)
        y += 14f
        val statusColor = when(invoice.paymentStatus) { PaymentStatus.PAID -> Color.rgb(46, 125, 50); PaymentStatus.PENDING -> Color.rgb(230, 81, 0); else -> Color.GRAY }
        normalPaint.color = statusColor
        val statusStr = "Estado: ${when(invoice.paymentStatus) { PaymentStatus.PAID -> "PAGADO"; PaymentStatus.PENDING -> "PENDIENTE"; else -> "Sin especificar" }}"
        canvas.drawText(statusStr, marginR - normalPaint.measureText(statusStr), y, normalPaint)
        y += 24f

        // ── Firma del cliente ─────────────────────────────────────────────────
        if (invoice.clientSignature.isNotBlank()) {
            y += 8f
            boldPaint.color = Color.rgb(21, 101, 192); boldPaint.textSize = 11f
            canvas.drawText("FIRMA DEL CLIENTE", marginL, y, boldPaint)
            y += 8f
            try {
                val sigBytes = Base64.decode(invoice.clientSignature, Base64.DEFAULT)
                val sigBmp = BitmapFactory.decodeByteArray(sigBytes, 0, sigBytes.size)
                if (sigBmp != null) {
                    val sigH = 80f
                    val sigW = (sigBmp.width.toFloat() / sigBmp.height.toFloat()) * sigH
                    canvas.drawBitmap(
                        Bitmap.createScaledBitmap(sigBmp, sigW.toInt(), sigH.toInt(), true),
                        marginL, y, null
                    )
                }
            } catch (_: Exception) {}
            y += 90f
            canvas.drawLine(marginL, y, marginL + 160f, y, linePaint.apply { color = Color.DKGRAY; strokeWidth = 1f })
            y += 12f
            normalPaint.color = Color.DKGRAY; normalPaint.textSize = 9f
            canvas.drawText(invoice.clientName, marginL, y, normalPaint)
            y += 20f
        }

        // ── QR Code ──────────────────────────────────────────────────────────
        try {
            val qrContent = "Factura:${invoice.invoiceNumber}|Total:\$${String.format("%.2f",invoice.totalAmount)}|Cliente:${invoice.clientName}"
            val qrBmp = generateQrBitmap(qrContent, 120)
            canvas.drawBitmap(qrBmp, marginR - 130f, page.info.pageHeight - 160f, null)
            smallPaint.color = Color.DKGRAY
            canvas.drawText("Escanea para verificar", marginR - 130f, page.info.pageHeight - 36f, smallPaint)
        } catch (_: Exception) {}

        // ── Pie de página ─────────────────────────────────────────────────────
        normalPaint.color = Color.rgb(100, 100, 100); normalPaint.textSize = 8f
        canvas.drawLine(marginL, page.info.pageHeight - 30f, marginR, page.info.pageHeight - 30f,
            linePaint.apply { color = Color.LTGRAY; strokeWidth = 1f })
        canvas.drawText("Gracias por su preferencia · ${profile.companyName.ifBlank{"Mi Empresa"}}",
            marginL, page.info.pageHeight - 18f, normalPaint)

        document.finishPage(page)

        val fileName = "Factura_${invoice.invoiceNumber.replace("-","_")}.pdf"
        val file = File(context.getExternalFilesDir("Facturas"), fileName)
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun generateQrBitmap(content: String, size: Int): Bitmap {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) for (y in 0 until size)
            bmp.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
        return bmp
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
            context.startActivity(Intent.createChooser(intent, "Compartir factura"))
        } catch (_: Exception) {
            context.startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }, "Compartir factura"
            ))
        }
    }
}
