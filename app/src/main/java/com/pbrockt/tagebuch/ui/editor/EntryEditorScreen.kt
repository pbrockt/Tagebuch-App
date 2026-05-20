package com.pbrockt.tagebuch.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pbrockt.tagebuch.data.model.DiaryPage

@Composable
fun EntryEditorScreen(
    page: DiaryPage,
    onPageChanged: (DiaryPage) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(page.id) { mutableStateOf(page.content) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                Icon(Icons.Default.EmojiEmotions, contentDescription = "Emoji")
            }
            IconButton(onClick = { /* Bildauswahl — TODO: ActivityResultLauncher */ }) {
                Icon(Icons.Default.Image, contentDescription = "Bild einfügen")
            }
        }

        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        // Text Editor
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
                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        if (showEmojiPicker) {
            EmojiPickerRow(
                onEmojiSelected = { emoji ->
                    text += emoji
                    onPageChanged(page.copy(content = text, updatedAt = System.currentTimeMillis()))
                    showEmojiPicker = false
                }
            )
        }
    }
}

@Composable
private fun EmojiPickerRow(onEmojiSelected: (String) -> Unit) {
    val emojis = listOf("😊", "😢", "😍", "😎", "🥳", "😴", "🤔", "😤", "❤️", "⭐", "🌟", "🔥", "🎉", "🌈", "☀️", "🌙")
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .horizontalScroll(rememberScrollState()),
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

@Composable
private fun Modifier.horizontalScroll(state: androidx.compose.foundation.ScrollState) =
    this.then(androidx.compose.foundation.horizontalScroll(state))
