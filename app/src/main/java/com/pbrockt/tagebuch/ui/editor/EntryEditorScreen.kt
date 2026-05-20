package com.pbrockt.tagebuch.ui.editor

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pbrockt.tagebuch.data.model.DiaryPage
import com.pbrockt.tagebuch.data.model.MediaType
import com.pbrockt.tagebuch.data.model.PageMedia
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.math.roundToInt

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun EntryEditorScreen(
    page: DiaryPage,
    onPageChanged: (DiaryPage) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var text by remember(page.id) { mutableStateOf(page.content) }
    var mediaItems by remember(page.id) {
        mutableStateOf(runCatching { json.decodeFromString<List<PageMedia>>(page.mediaJson) }.getOrElse { emptyList() })
    }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var canvasExpanded by remember { mutableStateOf(true) }
    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }

    fun saveMedia(items: List<PageMedia>) {
        mediaItems = items
        onPageChanged(page.copy(mediaJson = json.encodeToString(items), updatedAt = System.currentTimeMillis()))
    }

    fun resetPositions() {
        val cols = 3
        val spacing = 120f
        saveMedia(mediaItems.mapIndexed { i, item ->
            item.copy(
                positionX = (i % cols) * spacing + 16f,
                positionY = (i / cols) * spacing + 16f,
                scale = 1f
            )
        })
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            saveMedia(mediaItems + PageMedia(
                id = UUID.randomUUID().toString(),
                type = MediaType.IMAGE,
                uri = it.toString(),
                positionX = 20f, positionY = 20f,
                width = 160f, height = 160f
            ))
            canvasExpanded = true
        }
    }

    Column(modifier = modifier) {
        // --- Toolbar ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                Icon(Icons.Default.EmojiEmotions, "Emoji")
            }
            IconButton(onClick = { imageLauncher.launch("image/*") }) {
                Icon(Icons.Default.Image, "Bild")
            }
        }

        HorizontalDivider()

        // --- Canvas-Bereich ---
        if (mediaItems.isNotEmpty()) {
            // Canvas-Header mit Steuerung
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${mediaItems.size} Element${if (mediaItems.size == 1) "" else "e"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Reset-Button: alle Elemente zurück in den sichtbaren Bereich
                    TextButton(
                        onClick = { resetPositions() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Zurücksetzen", style = MaterialTheme.typography.labelSmall)
                    }
                    // Alles löschen
                    TextButton(
                        onClick = { saveMedia(emptyList()) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Alles löschen", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                    // Auf-/Zuklappen
                    IconButton(
                        onClick = { canvasExpanded = !canvasExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (canvasExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (canvasExpanded) "Canvas einklappen" else "Canvas ausklappen",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Canvas selbst (nur wenn aufgeklappt)
            if (canvasExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(0.dp))
                        .onSizeChanged {
                            canvasWidth = it.width.toFloat()
                            canvasHeight = it.height.toFloat()
                        }
                ) {
                    mediaItems.forEach { item ->
                        DraggableMediaItem(
                            item = item,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            onUpdate = { updated ->
                                saveMedia(mediaItems.map { if (it.id == updated.id) updated else it })
                            },
                            onDelete = {
                                saveMedia(mediaItems.filter { it.id != item.id })
                            }
                        )
                    }
                }
            }

            HorizontalDivider()
        }

        // --- Texteditor ---
        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                onPageChanged(page.copy(content = newText, updatedAt = System.currentTimeMillis()))
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            placeholder = { Text("Schreib deinen Eintrag...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        // --- Emoji-Picker ---
        if (showEmojiPicker) {
            EmojiPickerRow { emoji ->
                saveMedia(mediaItems + PageMedia(
                    id = UUID.randomUUID().toString(),
                    type = MediaType.EMOJI,
                    uri = emoji,
                    positionX = (20..100).random().toFloat(),
                    positionY = (20..80).random().toFloat(),
                    scale = 1.2f
                ))
                canvasExpanded = true
                showEmojiPicker = false
            }
        }
    }
}

@Composable
private fun DraggableMediaItem(
    item: PageMedia,
    canvasWidth: Float,
    canvasHeight: Float,
    onUpdate: (PageMedia) -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember(item.id) { mutableFloatStateOf(item.positionX) }
    var offsetY by remember(item.id) { mutableFloatStateOf(item.positionY) }
    var scale by remember(item.id) { mutableFloatStateOf(item.scale) }
    var selected by remember { mutableStateOf(false) }

    // Mindestgröße des Elements um es noch greifen zu können
    val minVisible = 40f

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .then(
                if (selected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                else Modifier
            )
            .pointerInput(item.id, canvasWidth, canvasHeight) {
                detectTransformGestures { _, pan, zoom, _ ->
                    selected = true
                    val newScale = (scale * zoom).coerceIn(0.3f, 5f)
                    val itemSize = minVisible * newScale

                    // Positions clampen — Element bleibt immer im sichtbaren Canvas-Bereich
                    val maxX = if (canvasWidth > 0) (canvasWidth - itemSize).coerceAtLeast(0f) else 600f
                    val maxY = if (canvasHeight > 0) (canvasHeight - itemSize).coerceAtLeast(0f) else 400f

                    offsetX = (offsetX + pan.x).coerceIn(0f, maxX)
                    offsetY = (offsetY + pan.y).coerceIn(0f, maxY)
                    scale = newScale
                    onUpdate(item.copy(positionX = offsetX, positionY = offsetY, scale = scale))
                }
            }
    ) {
        when (item.type) {
            MediaType.EMOJI -> Text(
                text = item.uri,
                fontSize = (36 * scale).sp,
                modifier = Modifier.padding(4.dp)
            )
            MediaType.IMAGE -> AsyncImage(
                model = Uri.parse(item.uri),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(
                    width = (item.width * scale).dp,
                    height = (item.height * scale).dp
                )
            )
        }
        // Löschen-Button wenn ausgewählt
        if (selected) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopEnd)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun EmojiPickerRow(onEmojiSelected: (String) -> Unit) {
    val emojis = listOf(
        "😊","😢","😍","😎","🥳","😴","🤔","😤",
        "❤️","⭐","🌟","🔥","🎉","🌈","☀️","🌙",
        "🐶","🌺","🍕","🎵","✈️","🏠","📚","💪"
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            emojis.forEach { emoji ->
                TextButton(onClick = { onEmojiSelected(emoji) }) {
                    Text(emoji, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}
