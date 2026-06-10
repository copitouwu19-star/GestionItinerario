package com.gestion.itinerario.ui.invoice

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SignatureLine(val points: List<Offset>)

@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    onSignatureChanged: (List<SignatureLine>) -> Unit
) {
    val lines = remember { mutableStateListOf<SignatureLine>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }

    LaunchedEffect(lines.size) { onSignatureChanged(lines.toList()) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { lines.clear(); currentPoints.clear(); onSignatureChanged(emptyList()) },
                enabled = lines.isNotEmpty()
            ) {
                Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Limpiar", style = MaterialTheme.typography.labelSmall)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset -> currentPoints.clear(); currentPoints.add(offset) },
                            onDrag = { change, _ ->
                                currentPoints.add(change.position)
                            },
                            onDragEnd = {
                                if (currentPoints.size > 1) {
                                    lines.add(SignatureLine(currentPoints.toList()))
                                }
                                currentPoints.clear()
                            }
                        )
                    }
            ) {
                // Dibujar líneas guardadas
                lines.forEach { line ->
                    val path = Path()
                    line.points.forEachIndexed { i, pt ->
                        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                    }
                    drawPath(path, color = Color(0xFF1A237E), style = Stroke(width = 3f))
                }
                // Dibujar línea actual
                if (currentPoints.size > 1) {
                    val path = Path()
                    currentPoints.forEachIndexed { i, pt ->
                        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                    }
                    drawPath(path, color = Color(0xFF1A237E), style = Stroke(width = 3f))
                }
                // Línea base
                drawLine(
                    color = Color(0xFFCCCCCC),
                    start = Offset(24f, size.height - 24f),
                    end = Offset(size.width - 24f, size.height - 24f),
                    strokeWidth = 1f
                )
            }
            if (lines.isEmpty() && currentPoints.isEmpty()) {
                Text("Firmar aquí",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFAAAAAA))
            }
        }
    }
}

fun signatureLinesToBitmap(lines: List<SignatureLine>, width: Int = 600, height: Int = 200): Bitmap? {
    if (lines.isEmpty()) return null
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bmp)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = AndroidPaint().apply {
        color = android.graphics.Color.rgb(26, 35, 126)
        strokeWidth = 4f
        style = AndroidPaint.Style.STROKE
        strokeCap = AndroidPaint.Cap.ROUND
        isAntiAlias = true
    }
    lines.forEach { line ->
        val path = AndroidPath()
        line.points.forEachIndexed { i, pt ->
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        canvas.drawPath(path, paint)
    }
    return bmp
}
