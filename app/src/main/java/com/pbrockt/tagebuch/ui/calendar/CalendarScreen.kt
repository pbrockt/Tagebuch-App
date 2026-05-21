package com.pbrockt.tagebuch.ui.calendar

import android.content.Intent
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    "great" to Color(0xFF4CAF50),
    "good" to Color(0xFF8BC34A),
    "okay" to Color(0xFFFFC107),
    "bad" to Color(0xFFFF9800),
    "awful" to Color(0xFFF44336)
)

private fun moodEmoji(mood: String?) = when (mood) {
    "great" -> "😁"; "good" -> "😊"; "okay" -> "😐"
    "bad" -> "😔"; "awful" -> "😢"; else -> null
}

private fun weatherEmoji(weather: String?) = when (weather) {
    "sunny" -> "☀"; "cloudy" -> "☁"; "rainy" -> "🌧"
    "snowy" -> "❄"; "stormy" -> "⛈"; else -> null
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
    val context = LocalContext.current

    // Eigenen Geburtstag aus Prefs (MM-dd Format)
    val ownBirthday = remember {
        val raw = context.getSharedPreferences("tagebuch_own_bday", android.content.Context.MODE_PRIVATE)
            .getString("bday", "") ?: ""
        raw  // Wir lesen es über SecurePrefs, aber für eine einfache Lösung direkt
    }

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
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "PDF teilen"))
            viewModel.clearExportedFile()
        }
    }

    val monthEntryCount = datesWithEntries.count { it.startsWith(currentMonth.toString()) }
    val primaryColor = MaterialTheme.colorScheme.primary

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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surface
                        ),
                        startY = 0f,
                        endY = 600f
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                MonthHeader(
                    month = currentMonth,
                    entryCount = monthEntryCount,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth
                )
                Spacer(Modifier.height(8.dp))
                WeekdayHeader()
                Spacer(Modifier.height(4.dp))

                // Slide-Animation beim Monatswechsel
                AnimatedContent(
                    targetState = currentMonth,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "MonthSlide"
                ) { month ->
                    CalendarGrid(
                        month = month,
                        datesWithEntries = datesWithEntries,
                        pageCountPerDay = pageCountPerDay,
                        moodMap = allDays.associate { it.date to (it.mood ?: "") },
                        weatherMap = allDays.associate { it.date to (it.weather ?: "") },
                        birthdayMap = birthdayMap,
                        ownBirthdayMmDd = viewModel.getOwnBirthdayMmDd(),
                        selectedDate = selectedDate,
                        calendarIconMode = calendarIconMode,
                        onDayClick = viewModel::selectDate
                    )
                }
            }

            Text(
                "Version 0.2",
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
        }
    }

    if (selectedDate != null) {
        DayEntryPopup(date = selectedDate!!, viewModel = viewModel, onDismiss = viewModel::closePopup)
    }
}

@Composable
private fun MonthHeader(month: YearMonth, entryCount: Int, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) { Icon(Icons.Default.ChevronLeft, null) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${month.month.getDisplayName(TextStyle.FULL, Locale.GERMAN)} ${month.year}",
                style = MaterialTheme.typography.titleLarge
            )
            if (entryCount > 0) {
                Text(
                    "$entryCount Eintr${if (entryCount == 1) "ag" else "äge"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, null) }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(Modifier.fillMaxWidth()) {
        listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So").forEach { day ->
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
    birthdayMap: Map<String, List<String>>,
    ownBirthdayMmDd: String,
    selectedDate: LocalDate?,
    calendarIconMode: String,
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
                        val mmdd = dateStr.substring(5)  // "MM-dd"
                        DayCell(
                            day = day,
                            pageCount = pageCountPerDay[dateStr] ?: 0,
                            mood = moodMap[dateStr],
                            weather = weatherMap[dateStr],
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
    day: Int,
    pageCount: Int,
    mood: String?,
    weather: String?,
    isSelected: Boolean,
    isToday: Boolean,
    showMood: Boolean,
    showWeather: Boolean,
    hasBirthday: Boolean,
    isOwnBirthday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val dotColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val iconSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp)

    // Oberes Icon: Geburtstag hat Vorrang, dann Wetter
    val topIcon = when {
        isOwnBirthday -> "👑"
        hasBirthday -> "🎈"
        showWeather -> weatherEmoji(weather)
        else -> null
    }

    // Unteres Icon: Stimmung
    val bottomMoodEmoji = if (showMood && !mood.isNullOrEmpty()) moodEmoji(mood) else null

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            // Oberes Icon
            if (topIcon != null) {
                Text(topIcon, style = MaterialTheme.typography.labelSmall.copy(fontSize = iconSize))
            } else {
                Spacer(Modifier.height(10.dp))
            }

            // Tageszahl
            Text(day.toString(), color = textColor, style = MaterialTheme.typography.bodySmall)

            // Unteres Icon oder Dots
            if (bottomMoodEmoji != null) {
                Text(bottomMoodEmoji, style = MaterialTheme.typography.labelSmall.copy(fontSize = iconSize))
            } else if (pageCount > 0) {
                // 1–3 Dots je nach Seitenanzahl
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(pageCount.coerceAtMost(3)) {
                        Box(Modifier.size(3.dp).clip(CircleShape).background(dotColor))
                    }
                }
            } else {
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}
