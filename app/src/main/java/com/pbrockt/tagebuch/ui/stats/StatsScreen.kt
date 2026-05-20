package com.pbrockt.tagebuch.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiken") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stat cards row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("🔥 Streak", "${stats.currentStreak} Tage", Modifier.weight(1f))
                StatCard("🏆 Rekord", "${stats.longestStreak} Tage", Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("📅 Tage", "${stats.totalDays}", Modifier.weight(1f))
                StatCard("✍️ Wörter", "${stats.totalWords}", Modifier.weight(1f))
            }

            // Monthly bar chart (last 6 months)
            if (stats.monthCounts.isNotEmpty()) {
                SectionTitle("Einträge pro Monat")
                MonthlyChart(
                    monthCounts = stats.monthCounts,
                    primaryColor = primary,
                    surfaceColor = surface,
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }

            // Mood distribution
            if (stats.moodCounts.isNotEmpty()) {
                SectionTitle("Stimmungsverteilung")
                val moodOrder = listOf("great","good","okay","bad","awful")
                val moodLabels = mapOf(
                    "great" to "😁 Super", "good" to "😊 Gut", "okay" to "😐 Ok",
                    "bad" to "😔 Schlecht", "awful" to "😢 Schrecklich"
                )
                val moodColors = mapOf(
                    "great" to Color(0xFF4CAF50), "good" to Color(0xFF8BC34A),
                    "okay" to Color(0xFFFFC107), "bad" to Color(0xFFFF9800),
                    "awful" to Color(0xFFF44336)
                )
                val total = stats.moodCounts.values.sum().toFloat()
                moodOrder.filter { stats.moodCounts.containsKey(it) }.forEach { mood ->
                    val count = stats.moodCounts[mood] ?: 0
                    val fraction = count / total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(moodLabels[mood] ?: mood, modifier = Modifier.width(120.dp),
                            style = MaterialTheme.typography.bodyMedium)
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.weight(1f).height(8.dp),
                            color = moodColors[mood] ?: primary
                        )
                        Text("$count", style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun MonthlyChart(
    monthCounts: Map<String, Int>,
    primaryColor: Color,
    surfaceColor: Color,
    modifier: Modifier = Modifier
) {
    val now = YearMonth.now()
    val months = (5 downTo 0).map { now.minusMonths(it.toLong()) }
    val maxCount = months.maxOf { monthCounts[it.toString()] ?: 0 }.coerceAtLeast(1)

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val barWidth = size.width / months.size * 0.6f
            val gap = size.width / months.size * 0.4f / 2f

            months.forEachIndexed { i, month ->
                val count = monthCounts[month.toString()] ?: 0
                val barHeight = (count.toFloat() / maxCount) * size.height
                val x = i * (size.width / months.size) + gap
                // Background bar
                drawRect(surfaceColor, Offset(x, 0f), Size(barWidth, size.height))
                // Filled bar
                if (count > 0) {
                    drawRect(primaryColor.copy(alpha = 0.85f),
                        Offset(x, size.height - barHeight), Size(barWidth, barHeight))
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            months.forEach { month ->
                Text(
                    text = month.month.getDisplayName(TextStyle.SHORT, Locale.GERMAN),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary)
}
