package com.pbrockt.tagebuch.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pbrockt.tagebuch.ui.editor.EntryEditorScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MOODS = listOf(
    "great" to "😁", "good" to "😊", "okay" to "😐", "bad" to "😔", "awful" to "😢"
)
private val WEATHERS = listOf(
    "sunny" to "☀️", "cloudy" to "☁️", "rainy" to "🌧️", "snowy" to "❄️", "stormy" to "⛈️"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEntryPopup(
    date: LocalDate,
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit
) {
    val pages by viewModel.pagesForSelectedDay.collectAsState()
    val allDays by viewModel.allDays.collectAsState()
    val dayInfo = allDays.find { it.date == date.toString() }
    var selectedPageIndex by remember { mutableIntStateOf(0) }

    val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.GERMAN))
        .replaceFirstChar { it.uppercase() }
    val dateFormatted = date.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy", Locale.GERMAN))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- Verbesserter Datum-Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = dayOfWeek,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    if (pages.isNotEmpty()) {
                        IconButton(onClick = {
                            pages.getOrNull(selectedPageIndex)?.let { viewModel.deletePage(it) }
                            selectedPageIndex = maxOf(0, selectedPageIndex - 1)
                        }) { Icon(Icons.Default.Delete, null) }
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
            }

            HorizontalDivider()

            // --- Stimmung & Wetter (Labels gleiche Breite) ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Stimmung:", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(70.dp))
                MOODS.forEach { (key, emoji) ->
                    FilterChip(
                        selected = dayInfo?.mood == key,
                        onClick = { viewModel.setMood(date.toString(), if (dayInfo?.mood == key) null else key) },
                        label = { Text(emoji) },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Wetter:", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(70.dp))
                WEATHERS.forEach { (key, emoji) ->
                    FilterChip(
                        selected = dayInfo?.weather == key,
                        onClick = { viewModel.setWeather(date.toString(), if (dayInfo?.weather == key) null else key) },
                        label = { Text(emoji) },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // --- Seiten-Tabs ---
            if (pages.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pages.indices.toList()) { index ->
                        FilterChip(
                            selected = index == selectedPageIndex,
                            onClick = { selectedPageIndex = index },
                            label = { Text("Seite ${index + 1}") }
                        )
                    }
                    item {
                        InputChip(
                            selected = false,
                            onClick = { viewModel.addPageToSelectedDay(); selectedPageIndex = pages.size },
                            label = { Text("Neue Seite") },
                            trailingIcon = { Icon(Icons.Default.Add, null, Modifier.size(16.dp)) }
                        )
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
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Eintrag erstellen")
                        }
                    }
                }
            } else {
                pages.getOrNull(selectedPageIndex)?.let { page ->
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
