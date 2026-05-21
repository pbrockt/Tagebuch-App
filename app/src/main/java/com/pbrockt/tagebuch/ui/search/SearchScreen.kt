package com.pbrockt.tagebuch.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pbrockt.tagebuch.data.model.DiaryPage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Extrahiert den lesbaren Text aus dem gespeicherten Inhalt (kann JSON oder Plaintext sein) */
private fun extractPlainText(content: String): String {
    if (!content.startsWith("{")) return content
    return try {
        // Einfaches Regex um das "text"-Feld aus dem FormattedContent-JSON zu lesen
        val match = Regex(""""text"\s*:\s*"((?:[^"\\]|\\.)*)"""").find(content)
        match?.groupValues?.get(1)
            ?.replace("\\n", "\n")
            ?.replace("\\\"", "\"")
            ?.replace("\\\\", "\\")
            ?: content
    } catch (e: Exception) {
        content
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToCalendar: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = viewModel::setQuery,
                        placeholder = { Text("Einträge durchsuchen...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                }
                // Kein Zurück-Pfeil — Bottom Navigation übernimmt die Navigation
            )
        }
    ) { padding ->
        if (query.length < 2) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Mindestens 2 Zeichen eingeben",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (results.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Keine Einträge gefunden",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(results) { page ->
                    SearchResultItem(
                        page = page,
                        onClick = {
                            // Datum im CalendarViewModel setzen und zu Kalender wechseln
                            viewModel.navigateToDate(page.dayDate)
                            onNavigateToCalendar()
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(page: DiaryPage, onClick: () -> Unit) {
    val plainText = remember(page.id) { extractPlainText(page.content) }
    val dateFormatted = remember(page.dayDate) {
        try {
            val date = LocalDate.parse(page.dayDate)
            date.format(DateTimeFormatter.ofPattern("EEEE, dd. MMMM yyyy", Locale.GERMAN))
                .replaceFirstChar { it.uppercase() }
        } catch (e: Exception) { page.dayDate }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = dateFormatted,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = plainText.trim().ifEmpty { "(Kein Text)" },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tippen zum Öffnen →",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
