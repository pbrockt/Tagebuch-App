package com.pbrockt.tagebuch.ui.calendar

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val moodColorMap = mapOf(
    "great" to Color(0xFF4CAF50), "good" to Color(0xFF8BC34A),
    "okay" to Color(0xFFFFC107), "bad" to Color(0xFFFF9800), "awful" to Color(0xFFF44336)
)
private fun moodEmoji(mood: String?) = when (mood) {
    "great" -> "😁"; "good" -> "😊"; "okay" -> "😐"; "bad" -> "😔"; "awful" -> "😢"; else -> null
}
private fun moodLabel(mood: String?) = when (mood) {
    "great" -> "super Laune"; "good" -> "gute Laune"; "okay" -> "okay"
    "bad" -> "schlechte Laune"; "awful" -> "schwerer Tag"; else -> null
}
private fun weatherEmoji(weather: String?) = when (weather) {
    "sunny" -> "☀"; "cloudy" -> "☁"; "rainy" -> "🌧"; "snowy" -> "❄"; "stormy" -> "⛈"; else -> null
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val datesWithEntries by viewModel.datesWithEntries.collectAsState()
    val pageCountPerDay by viewModel.pageCountPerDay.collectAsState()
    val allDays by viewModel.allDays.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val exportedFile by viewModel.exportedFile.collectAsState()
    val birthdayMap by viewModel.birthdayMap.collectAsState()
    val calendarIconMode by viewModel.calendarIconMode.collectAsState()
    val monthSummary by viewModel.monthSummary.collectAsState()
    val streakMilestone by viewModel.streakMilestone.collectAsState()
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshSettings()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(exportedFile) {
        exportedFile?.let { file ->
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "PDF teilen"))
            viewModel.clearExportedFile()
        }
    }

    // Streak-Belohnungs-Dialog
    streakMilestone?.let { milestone ->
        StreakRewardDialog(milestone = milestone) {
            viewModel.onStreakMilestoneDismissed(milestone)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tagebuch") },
                actions = {
                    IconButton(onClick = { viewModel.exportCurrentMonthPdf() }) {
                        Icon(Icons.Default.PictureAsPdf, "PDF Export")
                    }
                    IconButton(onClick = { viewModel.syncNow() }) {
                        Icon(Icons.Default.Sync, "Sync")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding)
                .background(Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.08f), MaterialTheme.colorScheme.surface),
                    startY = 0f, endY = 600f
                ))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

                Spacer(Modifier.height(8.dp))

                // Monats-Navigation
                MonthHeader(currentMonth, viewModel::previousMonth, viewModel::nextMonth)

                // Monats-Zusammenfassung
                if (monthSummary.entryCount > 0) {
                    Spacer(Modifier.height(8.dp))
                    MonthSummaryCard(monthSummary)
                }

                Spacer(Modifier.height(8.dp))
                WeekdayHeader()
                Spacer(Modifier.height(4.dp))

                // Kalender mit Slide-Animation
                AnimatedContent(
                    targetState = currentMonth,
                    transitionSpec = {
                        if (targetState > initialState)
                            (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                        else
                            (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                    },
                    label = "MonthSlide"
                ) { month ->
                    CalendarGrid(
                        month = month,
                        datesWithEntries = datesWithEntries,
                        pageCountPerDay = pageCountPerDay,
                        moodMap = allDays.associate { it.date to (it.mood ?: "") },
                        weatherMap = allDays.associate { it.date to (it.weather ?: "") },
                        periodMap = allDays.associate { it.date to (it.period ?: "") },
                        birthdayMap = birthdayMap,
                        ownBirthdayMmDd = viewModel.getOwnBirthdayMmDd(),
                        selectedDate = selectedDate,
                        calendarIconMode = calendarIconMode,
                        periodTrackingEnabled = viewModel.periodTrackingEnabled,
                        onDayClick = viewModel::selectDate
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Tages-Motivationsspruch
                DailyQuoteCard()
            }
        }
    }

    if (selectedDate != null) {
        DayEntryPopup(date = selectedDate!!, viewModel = viewModel, onDismiss = viewModel::closePopup)
    }
}

@Composable
private fun MonthSummaryCard(summary: MonthSummary) {
    val moodText = summary.dominantMood?.let { "${moodEmoji(it)} meist ${moodLabel(it)}" }
    val streakText = if (summary.currentStreak > 0) "🔥 ${summary.currentStreak} Tage Streak" else null

    val parts = listOfNotNull(
        "${summary.entryCount} Eintr${if (summary.entryCount == 1) "ag" else "äge"}",
        moodText, streakText
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Text(
            text = parts.joinToString(" · "),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun DailyQuoteCard() {
    val quote = remember { getDailyQuote() }
    Text(
        text = "\u201e" + quote + "\u201c",
        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun MonthHeader(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) { Icon(Icons.Default.ChevronLeft, null) }
        Text(
            "${month.month.getDisplayName(TextStyle.FULL, Locale.GERMAN)} ${month.year}",
            style = MaterialTheme.typography.titleLarge
        )
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
    pageCountPerDay: Map<String, Int>,
    moodMap: Map<String, String>,
    weatherMap: Map<String, String>,
    periodMap: Map<String, String>,
    birthdayMap: Map<String, List<String>>,
    ownBirthdayMmDd: String,
    selectedDate: LocalDate?,
    calendarIconMode: String,
    periodTrackingEnabled: Boolean,
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
                        val mmdd = dateStr.substring(5)
                        DayCell(
                            day = day,
                            pageCount = pageCountPerDay[dateStr] ?: 0,
                            mood = moodMap[dateStr],
                            weather = weatherMap[dateStr],
                            period = if (periodTrackingEnabled) periodMap[dateStr] else null,
                            isSelected = date == selectedDate,
                            isToday = date == LocalDate.now(),
                            showMood = calendarIconMode != "weather",
                            showWeather = calendarIconMode != "mood",
                            hasBirthday = birthdayMap.containsKey(mmdd),
                            isOwnBirthday = ownBirthdayMmDd.isNotEmpty() && mmdd == ownBirthdayMmDd,
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
    day: Int, pageCount: Int, mood: String?, weather: String?, period: String?,
    isSelected: Boolean, isToday: Boolean,
    showMood: Boolean, showWeather: Boolean,
    hasBirthday: Boolean, isOwnBirthday: Boolean,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val hasPeriod = !period.isNullOrEmpty()
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        hasPeriod -> Color(0xFFFFCDD2).copy(alpha = 0.6f) // Hellrot für Periode
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val dotColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val iconSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp)

    val topIcon = when {
        isOwnBirthday -> "👑"
        hasBirthday -> "🎈"
        showWeather -> weatherEmoji(weather)
        else -> null
    }
    val bottomMoodEmoji = if (showMood && !mood.isNullOrEmpty()) moodEmoji(mood) else null

    Box(
        modifier = modifier.aspectRatio(1f).clip(CircleShape).background(bgColor).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Periode-Indikator (kleiner roter Punkt oben links)
        if (hasPeriod && !isSelected) {
            Box(
                modifier = Modifier.size(6.dp).clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .align(Alignment.TopStart)
                    .padding(1.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, modifier = Modifier.padding(vertical = 2.dp)) {
            if (topIcon != null) Text(topIcon, style = MaterialTheme.typography.labelSmall.copy(fontSize = iconSize))
            else Spacer(Modifier.height(10.dp))
            Text(day.toString(), color = textColor, style = MaterialTheme.typography.bodySmall)
            if (bottomMoodEmoji != null) Text(bottomMoodEmoji, style = MaterialTheme.typography.labelSmall.copy(fontSize = iconSize))
            else if (pageCount > 0) Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(pageCount.coerceAtMost(3)) { Box(Modifier.size(3.dp).clip(CircleShape).background(dotColor)) }
            } else Spacer(Modifier.height(10.dp))
        }
    }
}
