package com.gestion.itinerario.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

private val PAGES = listOf(
    OnboardingPage(
        icon     = Icons.Default.Widgets,
        title    = "¡Bienvenido/a!",
        subtitle = "Gestión Itinerario es tu asistente completo para organizar citas, servicios técnicos y clientes en un solo lugar."
    ),
    OnboardingPage(
        icon     = Icons.Default.CalendarMonth,
        title    = "Agenda Inteligente",
        subtitle = "Programá citas, instalaciones y mantenimientos periódicos. Recibí recordatorios automáticos para nunca perderte un servicio."
    ),
    OnboardingPage(
        icon     = Icons.Default.Receipt,
        title    = "Facturas y Reportes",
        subtitle = "Generá facturas profesionales en PDF, creá cotizaciones al instante y exportá reportes mensuales en Excel."
    ),
    OnboardingPage(
        icon     = Icons.Default.People,
        title    = "Clientes y Servicios",
        subtitle = "Manejá tu cartera de clientes y registrá órdenes de servicio completas con fotos de evidencia."
    ),
    OnboardingPage(
        icon     = Icons.Default.Notifications,
        title    = "Notificaciones",
        subtitle = "Recibí alertas de citas próximas y recordatorios de mantenimiento para estar siempre un paso adelante."
    ),
    OnboardingPage(
        icon     = Icons.Default.LocationOn,
        title    = "Acceso a Ubicación",
        subtitle = "Usá la navegación integrada para llegar más rápido al destino de tus servicios técnicos."
    ),
    OnboardingPage(
        icon     = Icons.Default.CheckCircle,
        title    = "¡Todo listo!",
        subtitle = "Tu cuenta está configurada. Comenzá a gestionar tus servicios de forma profesional ahora mismo."
    )
)

private val PAGE_NOTIF    = 4
private val PAGE_LOCATION = 5
private val PAGE_LAST     = PAGES.lastIndex   // 6

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {

    val pagerState = rememberPagerState(pageCount = { PAGES.size })
    val scope      = rememberCoroutineScope()

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> scope.launch { pagerState.animateScrollToPage(PAGE_LOCATION) } }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> scope.launch { pagerState.animateScrollToPage(PAGE_LAST) } }

    fun advance() { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1565C0),
                        Color(0xFF0D47A1)
                    )
                )
            )
            .systemBarsPadding()
    ) {

        // ── Dots indicator ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            PAGES.indices.forEach { i ->
                val selected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (selected) Color.White
                            else Color.White.copy(alpha = 0.35f)
                        )
                        .width(if (selected) 28.dp else 8.dp)
                        .height(8.dp)
                )
            }
        }

        // ── Botón "Omitir" (sólo en páginas que no son la última) ────────────
        if (pagerState.currentPage < PAGE_LAST) {
            TextButton(
                onClick  = onFinish,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
            ) {
                Text("Omitir", color = Color.White.copy(alpha = 0.70f), fontSize = 14.sp)
            }
        }

        // ── Contenido del pager ───────────────────────────────────────────────
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            val data = PAGES[page]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Ícono
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector      = data.icon,
                        contentDescription = null,
                        modifier         = Modifier.size(64.dp),
                        tint             = Color.White
                    )
                }

                Spacer(Modifier.height(36.dp))

                // Título
                Text(
                    text        = data.title,
                    fontSize    = 28.sp,
                    fontWeight  = FontWeight.Bold,
                    color       = Color.White,
                    textAlign   = TextAlign.Center
                )

                Spacer(Modifier.height(14.dp))

                // Subtítulo
                Text(
                    text      = data.subtitle,
                    fontSize  = 15.sp,
                    color     = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    lineHeight = 23.sp
                )

                Spacer(Modifier.height(44.dp))

                // ── Botones según la página ───────────────────────────────────
                when (page) {

                    PAGE_NOTIF -> {
                        // Página de notificaciones
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    advance()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.25f),
                                contentColor   = Color.White
                            ),
                            shape    = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Notifications, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Permitir notificaciones", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { advance() }) {
                            Text("Ahora no", color = Color.White.copy(alpha = 0.60f))
                        }
                    }

                    PAGE_LOCATION -> {
                        // Página de ubicación
                        Button(
                            onClick = {
                                locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.25f),
                                contentColor   = Color.White
                            ),
                            shape    = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Permitir ubicación", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { advance() }) {
                            Text("Ahora no", color = Color.White.copy(alpha = 0.60f))
                        }
                    }

                    PAGE_LAST -> {
                        // Última página — botón "Empezar"
                        Button(
                            onClick  = onFinish,
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor   = Color(0xFF1565C0)
                            ),
                            shape    = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("¡Empezar!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(20.dp))
                        }
                    }

                    else -> {
                        // Páginas normales — botón "Siguiente"
                        Button(
                            onClick  = { advance() },
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.25f),
                                contentColor   = Color.White
                            ),
                            shape    = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Siguiente", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
