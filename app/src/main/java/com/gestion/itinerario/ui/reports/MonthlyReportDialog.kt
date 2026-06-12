package com.gestion.itinerario.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.gestion.itinerario.R
import com.gestion.itinerario.ui.theme.Primary40
import com.gestion.itinerario.ui.theme.Primary80
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

private val WhatsAppGreen = Color(0xFF25D366)
private val monthNames = arrayOf("Enero","Febrero","Marzo","Abril","Mayo","Junio",
    "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre")

/** Diálogo para generar un reporte mensual de ingresos y servicios en PDF. */
@Composable
fun MonthlyReportDialog(
    onDismiss: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val now = remember { Calendar.getInstance() }
    val periods = remember {
        (0 until 12).map { offset ->
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -offset) }
            cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
        }
    }
    var selected by remember { mutableStateOf(periods.first()) }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var countdownSecs by remember { mutableStateOf(0) }

    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            countdownSecs = 9
            repeat(9) { delay(1000L); countdownSecs-- }
        } else {
            countdownSecs = 0
        }
    }

    fun generate(share: Boolean) {
        isGenerating = true
        errorMsg = null
        scope.launch {
            try {
                val (year, month) = selected
                val file = viewModel.generateMonthlyReport(context, year, month)
                if (share) ReportPdfGenerator.shareViaWhatsApp(context, file)
                else       ReportPdfGenerator.openPdf(context, file)
            } catch (e: Exception) {
                errorMsg = "No se pudo generar el reporte"
            }
            isGenerating = false
        }
    }

    fun generateExcel() {
        isGenerating = true
        errorMsg = null
        scope.launch {
            try {
                val (year, month) = selected
                val file = viewModel.generateMonthlyExcel(context, year, month)
                ReportPdfGenerator.shareExcel(context, file)
            } catch (e: Exception) {
                errorMsg = "No se pudo generar el Excel"
            }
            isGenerating = false
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Summarize, null, tint = Primary80, modifier = Modifier.size(24.dp))
                    Text("Reporte mensual", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text("Selecciona el mes a reportar. Incluirá ingresos facturados, cobros y servicios completados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(periods) { (year, month) ->
                        val label = if (year == now.get(Calendar.YEAR)) monthNames[month]
                                    else "${monthNames[month]} $year"
                        FilterChip(
                            selected = selected == (year to month),
                            onClick = { selected = year to month },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                errorMsg?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = { generate(share = true) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(if (countdownSecs > 0) "Generando... ${countdownSecs}s" else "Finalizando…")
                    } else {
                        Icon(painterResource(R.drawable.ic_whatsapp), null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Generar y enviar por WhatsApp")
                    }
                }
                OutlinedButton(
                    onClick = { generate(share = false) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(if (countdownSecs > 0) "Generando... ${countdownSecs}s" else "Finalizando…")
                    } else {
                        Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Solo exportar PDF")
                    }
                }
                OutlinedButton(
                    onClick = { generateExcel() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(if (countdownSecs > 0) "Generando... ${countdownSecs}s" else "Finalizando…")
                    } else {
                        Icon(Icons.Default.TableChart, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Exportar a Excel (CSV)")
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cerrar") }
            }
        }
    }
}
