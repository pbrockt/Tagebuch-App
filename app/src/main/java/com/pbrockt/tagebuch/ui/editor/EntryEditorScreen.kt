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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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

    fun saveMedia(items: List<PageMedia>) {
        mediaItems = items
        onPageChanged(page.copy(mediaJson = json.encodeToString(items), updatedAt = System.currentTimeMillis()))
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val item = PageMedia(
                id = UUID.randomUUID().toString(),
                type = MediaType.IMAGE,
                uri = it.toString(),
                positionX = 60f, positionY = 60f,
                width = 180f, height = 180f
            )
            saveMedia(mediaItems + item)
        }
    }

    Column(modifier = modifier) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                Icon(Icons.Default.EmojiEmotions, "Emoji")
            }
            IconButton(onClick = { imageLauncher.launch("image/*") }) {
                Icon(Icons.Default.Image, "Bild")
            }
        }

        HorizontalDivider()

        // Canvas for free-placement media items
        if (mediaItems.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                mediaItems.forEach { item ->
                    DraggableMediaItem(
                        item = item,
                        onUpdate = { updated ->
                            saveMedia(mediaItems.map { if (it.id == updated.id) updated else it })
                        },
                        onDelete = {
                            saveMedia(mediaItems.filter { it.id != item.id })
                        }
                    )
                }
                Text(
                    "← Ziehen zum Verschieben · Pinch zum Skalieren",
                    modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            HorizontalDivider()
        }

        // Text editor
        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                onPageChanged(page.copy(content = newText, updatedAt = System.currentTimeMillis()))
            },
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
            placeholder = { Text("Schreib deinen Eintrag...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        // Emoji picker
        if (showEmojiPicker) {
            EmojiPickerRow { emoji ->
                val item = PageMedia(
                    id = UUID.randomUUID().toString(),
                    type = MediaType.EMOJI,
                    uri = emoji,
                    positionX = (20..120).random().toFloat(),
                    positionY = (20..120).random().toFloat(),
                    scale = 1.2f
                )
                saveMedia(mediaItems + item)
                showEmojiPicker = false
            }
        }
    }
}

@Composable
private fun DraggableMediaItem(
    item: PageMedia,
    onUpdate: (PageMedia) -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember(item.id) { mutableFloatStateOf(item.positionX) }
    var offsetY by remember(item.id) { mutableFloatStateOf(item.positionY) }
    var scale by remember(item.id) { mutableFloatStateOf(item.scale) }
    var selected by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .then(if (selected) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)) else Modifier)
            .pointerInput(item.id) {
                detectTransformGestures { _, pan, zoom, _ ->
                    selected = true
                    offsetX += pan.x
                    offsetY += pan.y
                    scale = (scale * zoom).coerceIn(0.3f, 5f)
                    onUpdate(item.copy(positionX = offsetX, positionY = offsetY, scale = scale))
                }
            }
    ) {
        when (item.type) {
            MediaType.EMOJI -> Text(
                text = item.uri,
                fontSize = (40 * scale).sp,
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
        // Delete button when selected
        if (selected) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(20.dp)
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
        "😊", "😢", "😍", "😎", "🥳", "😴", "🤔", "😤",
        "❤️", "⭐", "🌟", "🔥", "🎉", "🌈", "☀️", "🌙",
        "🐶", "🌺", "🍕", "🎵", "✈️", "🏠", "📚", "💪"
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp),
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
