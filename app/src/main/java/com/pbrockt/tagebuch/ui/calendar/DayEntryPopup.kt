package com.pbrockt.tagebuch.ui.calendar

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pbrockt.tagebuch.ui.editor.EntryEditorScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MOODS = listOf("great" to "😁", "good" to "😊", "okay" to "😐", "bad" to "😔", "awful" to "😢")
private val WEATHERS = listOf("sunny" to "☀️", "cloudy" to "☁️", "rainy" to "🌧️", "snowy" to "❄️", "stormy" to "⛈️")
private val PERIODS = listOf("start" to "🩸 Start", "active" to "🩸 Aktiv", "end" to "🩸 Ende")

private val PAGE_COLORS = listOf(
    null to "Standard",
    "#FFF9C4" to "Gelb",
    "#F8BBD9" to "Rosa",
    "#C8E6C9" to "Grün",
    "#BBDEFB" to "Blau",
    "#E1BEE7" to "Lila",
    "#FFE0B2" to "Orange",
    "#F5F5F5" to "Grau"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEntryPopup(date: LocalDate, viewModel: CalendarViewModel, onDismiss: () -> Unit) {
    val pages by viewModel.pagesForSelectedDay.collectAsState()
    val allDays by viewModel.allDays.collectAsState()
    val dayInfo = allDays.find { it.date == date.toString() }
    var selectedPageIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.GERMAN)).replaceFirstChar { it.uppercase() }
    val dateFormatted = date.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy", Locale.GERMAN))

    // „An diesem Tag vor X Jahren"
    var pastEntries by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    LaunchedEffect(date) {
        val past = viewModel.getPastYearEntries(date)
        pastEntries = past.map { (year, pageList) -> year to pageList.size }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(dayOfWeek, style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(dateFormatted, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row {
                    // Teilen-Button
                    if (pages.isNotEmpty()) {
                        IconButton(onClick = {
                            val currentPage = pages.getOrNull(selectedPageIndex)
                            if (currentPage != null) {
                                val text = buildString {
                                    appendLine("📔 $dayOfWeek, $dateFormatted")
                                    appendLine()
                                    appendLine(currentPage.content.ifEmpty { "(Kein Text)" })
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Eintrag teilen"))
                            }
                        }) { Icon(Icons.Default.Share, "Teilen") }
                        IconButton(onClick = {
                            pages.getOrNull(selectedPageIndex)?.let { viewModel.deletePage(it) }
                            selectedPageIndex = maxOf(0, selectedPageIndex - 1)
                        }) { Icon(Icons.Default.Delete, null) }
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
            }

            // „An diesem Tag vor X Jahren"
            if (pastEntries.isNotEmpty()) {
                val yearsAgo = LocalDate.now().year - pastEntries.first().first
                val count = pastEntries.first().second
                Surface(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary)
                        Text("Vor $yearsAgo Jahr${if (yearsAgo == 1) "" else "en"}: $count Eintrag${if (count == 1) "" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            HorizontalDivider()

            // Stimmung & Wetter
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Stimmung:", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(70.dp))
                MOODS.forEach { (key, emoji) ->
                    FilterChip(selected = dayInfo?.mood == key,
                        onClick = { viewModel.setMood(date.toString(), if (dayInfo?.mood == key) null else key) },
                        label = { Text(emoji) }, modifier = Modifier.height(32.dp))
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Wetter:", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(70.dp))
                WEATHERS.forEach { (key, emoji) ->
                    FilterChip(selected = dayInfo?.weather == key,
                        onClick = { viewModel.setWeather(date.toString(), if (dayInfo?.weather == key) null else key) },
                        label = { Text(emoji) }, modifier = Modifier.height(32.dp))
                }
            }

            // Perioden-Tracking (nur wenn aktiviert)
            if (viewModel.periodTrackingEnabled) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Periode:", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(70.dp))
                    PERIODS.forEach { (key, label) ->
                        FilterChip(selected = dayInfo?.period == key,
                            onClick = { viewModel.setPeriod(date.toString(), if (dayInfo?.period == key) null else key) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(32.dp))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Seiten-Tabs + Seitenfarbe
            if (pages.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(pages.indices.toList()) { index ->
                            FilterChip(selected = index == selectedPageIndex,
                                onClick = { selectedPageIndex = index },
                                label = { Text("Seite ${index + 1}") })
                        }
                        item {
                            InputChip(selected = false,
                                onClick = { viewModel.addPageToSelectedDay(); selectedPageIndex = pages.size },
                                label = { Text("Neu") },
                                trailingIcon = { Icon(Icons.Default.Add, null, Modifier.size(14.dp)) })
                        }
                    }
                    // Seitenfarbe
                    Box {
                        var showColorPicker by remember { mutableStateOf(false) }
                        val currentPage = pages.getOrNull(selectedPageIndex)
                        val currentColor = currentPage?.pageColor
                        IconButton(onClick = { showColorPicker = true }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Palette, "Seitenfarbe", modifier = Modifier.size(20.dp))
                        }
                        if (showColorPicker) {
                            DropdownMenu(expanded = true, onDismissRequest = { showColorPicker = false }) {
                                PAGE_COLORS.chunked(4).forEach { row ->
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        row.forEach { (hex, label) ->
                                            val displayColor = hex?.let { Color(android.graphics.Color.parseColor(it)) }
                                                ?: MaterialTheme.colorScheme.surface
                                            Box(modifier = Modifier.size(28.dp).clip(CircleShape)
                                                .background(displayColor)
                                                .border(if (currentColor == hex) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                .clickable {
                                                    currentPage?.let { viewModel.savePage(it.copy(pageColor = hex)) }
                                                    showColorPicker = false
                                                })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            if (pages.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Noch kein Eintrag", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.addPageToSelectedDay() }) {
                            Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Eintrag erstellen")
                        }
                    }
                }
            } else {
                val currentPage = pages.getOrNull(selectedPageIndex)
                val bgColor = currentPage?.pageColor?.let {
                    runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
                }
                Box(modifier = Modifier.fillMaxSize().then(
                    if (bgColor != null) Modifier.background(bgColor) else Modifier
                )) {
                    currentPage?.let { page ->
                        EntryEditorScreen(
                            page = page,
                            onPageChanged = { viewModel.savePage(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
