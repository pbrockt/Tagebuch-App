package com.pbrockt.tagebuch.ui.calendar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float, val y: Float,
    val vx: Float, val vy: Float,
    val color: Color, val size: Float,
    val rotation: Float
)

private val confettiColors = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
    Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1),
    Color(0xFFFFD600), Color(0xFFEC407A)
)

@Composable
fun StreakRewardDialog(milestone: Int, onDismiss: () -> Unit) {
    val (icon, title, subtitle) = when (milestone) {
        7 -> Triple("🌟", "7 Tage in Folge!", "Du schreibst schon eine ganze Woche – super!")
        30 -> Triple("🏅", "30 Tage in Folge!", "Ein ganzer Monat! Das ist wirklich beeindruckend.")
        100 -> Triple("🏆", "100 Tage in Folge!", "Unglaublich! Du bist eine echte Tagebuch-Heldin!")
        else -> Triple("🎉", "$milestone Tage!", "Fantastisch – weiter so!")
    }

    val particles = remember {
        List(60) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.5f - 0.2f,
                vx = Random.nextFloat() * 0.006f - 0.003f,
                vy = Random.nextFloat() * 0.006f + 0.004f,
                color = confettiColors.random(),
                size = Random.nextFloat() * 10f + 8f,
                rotation = Random.nextFloat() * 360f
            )
        }
    }

    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(16)
            progress = (progress + 0.005f).coerceAtMost(1f)
        }
    }

    LaunchedEffect(Unit) { delay(4000); onDismiss() }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            // Konfetti-Canvas über dem Dialog
            Canvas(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                particles.forEach { p ->
                    val currentY = p.y + p.vy * progress * 200
                    val currentX = p.x + p.vx * progress * 200 +
                            sin(progress * 10 + p.rotation) * 0.02f
                    if (currentY in 0f..1f) {
                        drawCircle(
                            color = p.color.copy(alpha = (1f - progress * 0.5f)),
                            radius = p.size * (1f - progress * 0.3f),
                            center = Offset(currentX * size.width, currentY * size.height)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(icon, fontSize = 64.sp)
                    Text(title,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary)
                    Text(subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Danke! 🎉")
                    }
                }
            }
        }
    }
}
