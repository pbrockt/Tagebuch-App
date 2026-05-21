@file:OptIn(ExperimentalFoundationApi::class)

package com.pbrockt.tagebuch.ui.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.pbrockt.tagebuch.data.model.DiaryPage
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class FormattedContent(
    val text: String = "",
    val spans: List<FormatSpan> = emptyList()
)

@Serializable
data class FormatSpan(
    val start: Int,
    val end: Int,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val colorHex: String? = null  // 6-stellig RGB ohne #, z.B. "E53935"
)

private fun loadContent(raw: String): FormattedContent =
    if (raw.startsWith("{"))
        runCatching { json.decodeFromString<FormattedContent>(raw) }.getOrElse { FormattedContent(raw) }
    else FormattedContent(raw)

private fun FormattedContent.toAnnotatedString(): AnnotatedString {
    val builder = AnnotatedString.Builder(text)
    for (span in spans) {
        if (span.start >= text.length || span.start >= span.end) continue
        val end = span.end.coerceAtMost(text.length)
        val color = span.colorHex?.let { hex ->
            Color(("FF$hex").toLong(16) or 0xFF000000L)
        } ?: Color.Unspecified
        builder.addStyle(
            SpanStyle(
                fontWeight = if (span.bold) FontWeight.Bold else null,
                fontStyle = if (span.italic) FontStyle.Italic else null,
                color = color
            ),
            span.start, end
        )
    }
    return builder.toAnnotatedString()
}

// Farb-Optionen: null = Standardfarbe (Theme)
private val TEXT_COLORS = listOf<String?>(null, "E53935", "1E88E5", "43A047", "FB8C00", "8E24AA")

@Composable
fun EntryEditorScreen(
    page: DiaryPage,
    onPageChanged: (DiaryPage) -> Unit,
    readOnly: Boolean = false,  // true = Nur-Lesen-Modus für alte gesperrte Einträge
    modifier: Modifier = Modifier
) {
    var content by remember(page.id) { mutableStateOf(loadContent(page.content)) }
    var tfv by remember(page.id) {
        mutableStateOf(TextFieldValue(annotatedString = content.toAnnotatedString()))
    }
    val hasSelection = tfv.selection.length > 0

    fun persist(newContent: FormattedContent, sel: TextRange) {
        content = newContent
        tfv = TextFieldValue(annotatedString = newContent.toAnnotatedString(), selection = sel)
        onPageChanged(page.copy(
            content = json.encodeToString(newContent),
            updatedAt = System.currentTimeMillis()
        ))
    }

    fun applySpan(bold: Boolean? = null, italic: Boolean? = null, colorHex: String? = "KEEP") {
        val sel = tfv.selection
        if (sel.collapsed) return
        val s = sel.min; val e = sel.max
        val kept = content.spans.filter { it.end <= s || it.start >= e }
        val base = content.spans.firstOrNull { it.start < e && it.end > s }
        val newSpan = FormatSpan(
            start = s, end = e,
            bold = bold ?: (base?.bold ?: false),
            italic = italic ?: (base?.italic ?: false),
            colorHex = if (colorHex == "KEEP") base?.colorHex else colorHex
        )
        persist(content.copy(spans = kept + newSpan), sel)
    }

    fun clearFormat() {
        val sel = tfv.selection
        if (sel.collapsed) return
        persist(content.copy(
            spans = content.spans.filter { it.end <= sel.min || it.start >= sel.max }
        ), sel)
    }

    Column(modifier = modifier) {

        // --- Formatierungs-Toolbar ---
        // Toolbar nur im Edit-Modus anzeigen
        if (!readOnly) Surface(tonalElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Fett
                IconButton(onClick = {
                    val cur = content.spans.any { it.start <= tfv.selection.min && it.end >= tfv.selection.max && it.bold }
                    applySpan(bold = !cur)
                }, enabled = hasSelection) {
                    Icon(Icons.Default.FormatBold, "Fett",
                        tint = if (hasSelection) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
                // Kursiv
                IconButton(onClick = {
                    val cur = content.spans.any { it.start <= tfv.selection.min && it.end >= tfv.selection.max && it.italic }
                    applySpan(italic = !cur)
                }, enabled = hasSelection) {
                    Icon(Icons.Default.FormatItalic, "Kursiv",
                        tint = if (hasSelection) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }

                VerticalDivider(modifier = Modifier.height(28.dp).padding(horizontal = 2.dp))

                // Farb-Kreise
                TEXT_COLORS.forEach { hex ->
                    val circleColor = hex?.let { Color(("FF$it").toLong(16) or 0xFF000000L) }
                        ?: MaterialTheme.colorScheme.onSurface
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(circleColor, CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                            .clickable(
                                enabled = hasSelection,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { applySpan(colorHex = hex) }
                    )
                }

                VerticalDivider(modifier = Modifier.height(28.dp).padding(horizontal = 2.dp))

                // Formatierung löschen
                IconButton(onClick = { clearFormat() }, enabled = hasSelection) {
                    Icon(Icons.Default.FormatClear, "Formatierung entfernen",
                        tint = if (hasSelection) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }
        }

        if (!readOnly) HorizontalDivider()

        // Hinweis im Nur-Lesen-Modus
        if (readOnly) {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Nur Lesen — Schloss antippen zum Bearbeiten",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider()
        }

        // --- Texteditor mit AnnotatedString ---
        BasicTextField(
            value = tfv,
            onValueChange = { newTfv ->
                if (readOnly) return@BasicTextField  // Im Nur-Lesen-Modus keine Änderungen erlauben
                val newText = newTfv.text
                val adjustedSpans = content.spans.mapNotNull { span ->
                    val newEnd = span.end.coerceAtMost(newText.length)
                    val newStart = span.start.coerceAtMost(newEnd)
                    if (newStart >= newEnd || newStart >= newText.length) null
                    else span.copy(start = newStart, end = newEnd)
                }
                val newContent = FormattedContent(newText, adjustedSpans)
                content = newContent
                tfv = TextFieldValue(
                    annotatedString = newContent.toAnnotatedString(),
                    selection = newTfv.selection
                )
                onPageChanged(page.copy(
                    content = json.encodeToString(newContent),
                    updatedAt = System.currentTimeMillis()
                ))
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (content.text.isEmpty()) {
                        Text(
                            "Schreib deinen Eintrag...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
