package com.pbrockt.tagebuch.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pbrockt.tagebuch.ui.theme.FloralBackground
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val datesWithEntries by viewModel.datesWithEntries.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tagebuch") },
                actions = {
                    IconButton(onClick = { viewModel.syncNow() }) {
                        Icon(Icons.Default.Sync, contentDescription = "Synchronisieren")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Einstellungen")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            FloralBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                MonthHeader(
                    month = currentMonth,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth
                )
                Spacer(Modifier.height(8.dp))
                WeekdayHeader()
                Spacer(Modifier.height(4.dp))
                CalendarGrid(
                    month = currentMonth,
                    datesWithEntries = datesWithEntries,
                    selectedDate = selectedDate,
                    onDayClick = viewModel::selectDate
                )
            }
        }
    }

    if (selectedDate != null) {
        DayEntryPopup(
            date = selectedDate!!,
            viewModel = viewModel,
            onDismiss = viewModel::closePopup
        )
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) { Icon(Icons.Default.ChevronLeft, null) }
        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.GERMAN)} ${month.year}",
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, null) }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("Mo","Di","Mi","Do","Fr","Sa","So").forEach { day ->
            Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    datesWithEntries: Set<String>,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val startOffset = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val daysInMonth = month.lengthOfMonth()
    val rows = (startOffset + daysInMonth + 6) / 7

    Column {
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val day = row * 7 + col - startOffset + 1
                    if (day < 1 || day > daysInMonth) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = month.atDay(day)
                        DayCell(
                            day = day,
                            hasEntry = datesWithEntries.contains(date.toString()),
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
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.bodyMedium
            )
            if (hasEntry) {
                Box(
                    modifier = Modifier.size(4.dp).clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
