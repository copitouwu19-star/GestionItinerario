package com.gestion.itinerario.ui.security

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.ui.theme.StatusLowStock

@Composable
fun PinScreen(
    onUnlocked: () -> Unit,
    viewModel: PinViewModel = hiltViewModel()
) {
    val pin by viewModel.pinInput.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isPinEnabled by viewModel.isPinEnabled.collectAsStateWithLifecycle()

    // If no PIN configured, go directly
    LaunchedEffect(isPinEnabled) {
        if (!isPinEnabled) onUnlocked()
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D1117), Color(0xFF1A237E), Color(0xFF0D1117))
    )

    Box(
        modifier = Modifier.fillMaxSize().background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier.size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            }

            Text(
                "Ingresa tu PIN",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            if (error) {
                Text("PIN incorrecto. Intenta de nuevo.", color = StatusLowStock, fontSize = 14.sp)
            }

            // PIN dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { i ->
                    val filled = i < pin.length
                    val dotColor by animateColorAsState(
                        if (error) StatusLowStock else if (filled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
                        label = "dot$i"
                    )
                    Box(
                        modifier = Modifier.size(16.dp).clip(CircleShape).background(dotColor)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Keypad
            PinKeypad(
                onDigit = {
                    viewModel.onDigit(it)
                    viewModel.resetError()
                    if (pin.length + 1 == 4) {
                        viewModel.verify(onUnlocked)
                    }
                },
                onDelete = viewModel::onDelete
            )
        }
    }
}

@Composable
fun PinKeypad(onDigit: (String) -> Unit, onDelete: () -> Unit) {
    val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        keys.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(Modifier.size(72.dp))
                    } else {
                        PinKey(label = key, onClick = {
                            if (key == "⌫") onDelete() else onDigit(key)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun PinKey(label: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, spring(), label = "scale")

    Surface(
        onClick = { pressed = true; onClick(); pressed = false },
        modifier = Modifier.size(72.dp).scale(scale),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.08f),
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (label == "⌫") {
                Icon(Icons.Default.Backspace, contentDescription = "Borrar", tint = Color.White.copy(alpha = 0.7f))
            } else {
                Text(label, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            }
        }
    }
}
