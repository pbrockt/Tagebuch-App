package com.pbrockt.tagebuch.ui.calendar

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.pbrockt.tagebuch.ui.theme.FloralBackground
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val moodColorMap = mapOf(
    "great" to Color(0xFF4CAF50),
    "good" to Color(0xFF8BC34A),
    "okay" to Color(0xFFFFC107),
    "bad" to Color(0xFFFF9800),
    "awful" to Color(0xFFF44336)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val datesWithEntries by viewModel.datesWithEntries.collectAsState()
    val allDays by viewModel.allDays.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val exportedFile by viewModel.exportedFile.collectAsState()
    val context = LocalContext.current

    // Share exported PDF when ready
    LaunchedEffect(exportedFile) {
        exportedFile?.let { file ->
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "PDF teilen"))
            viewModel.clearExportedFile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tagebuch") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) { Icon(Icons.Default.Search, "Suche") }
                    IconButton(onClick = onNavigateToStats) { Icon(Icons.Default.BarChart, "Statistiken") }
                    IconButton(onClick = { viewModel.exportCurrentMonthPdf() }) { Icon(Icons.Default.PictureAsPdf, "PDF Export") }
                    IconButton(onClick = { viewModel.syncNow() }) { Icon(Icons.Default.Sync, "Sync") }
                    IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, "Einstellungen") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            FloralBackground()
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
            ) {
                MonthHeader(currentMonth, viewModel::previousMonth, viewModel::nextMonth)
                Spacer(Modifier.height(8.dp))
                WeekdayHeader()
                Spacer(Modifier.height(4.dp))
                CalendarGrid(
                    month = currentMonth,
                    datesWithEntries = datesWithEntries,
                    moodMap = allDays.associate { it.date to (it.mood ?: "") },
                    weatherMap = allDays.associate { it.date to (it.weather ?: "") },
                    selectedDate = selectedDate,
                    onDayClick = viewModel::selectDate
                )
            }
        }
    }

    if (selectedDate != null) {
        DayEntryPopup(date = selectedDate!!, viewModel = viewModel, onDismiss = viewModel::closePopup)
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) { Icon(Icons.Default.ChevronLeft, null) }
        Text("${month.month.getDisplayName(TextStyle.FULL, Locale.GERMAN)} ${month.year}",
            style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, null) }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(Modifier.fillMaxWidth()) {
        listOf("Mo","Di","Mi","Do","Fr","Sa","So").forEach { day ->
            Text(day, Modifier.weight(1f), textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    datesWithEntries: Set<String>,
    moodMap: Map<String, String>,
    weatherMap: Map<String, String>,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit
) {
    val startOffset = (month.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val daysInMonth = month.lengthOfMonth()
    val rows = (startOffset + daysInMonth + 6) / 7

    Column {
        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val day = row * 7 + col - startOffset + 1
                    if (day < 1 || day > daysInMonth) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = month.atDay(day)
                        val dateStr = date.toString()
                        DayCell(
                            day = day,
                            hasEntry = datesWithEntries.contains(dateStr),
                            mood = moodMap[dateStr],
                            weather = weatherMap[dateStr],
                            isSelected = date == selectedDate,
                            isToday = date == LocalDate.now(),
                            onClick = { onDayClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    hasEntry: Boolean,
    mood: String?,
    weather: String?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moodColor = moodColorMap[mood]
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        moodColor != null -> moodColor.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier.aspectRatio(1f).clip(CircleShape)
            .background(bgColor).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Weather emoji (tiny, top)
            val weatherEmoji = when (weather) {
                "sunny" -> "☀"; "cloudy" -> "☁"; "rainy" -> "🌧"
                "snowy" -> "❄"; "stormy" -> "⛈"; else -> null
            }
            if (weatherEmoji != null) {
                Text(weatherEmoji, style = MaterialTheme.typography.labelSmall.copy(fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp)))
            }
            Text(
                text = day.toString(),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )
            if (hasEntry) {
                Box(Modifier.size(3.dp).clip(CircleShape).background(
                    if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary))
            }
        }
    }
}
