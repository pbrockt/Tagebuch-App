package com.pbrockt.tagebuch.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PageMedia(
    val id: String,
    val type: MediaType,
    val uri: String,          // lokaler URI oder Emoji-String
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val width: Float = 200f,
    val height: Float = 200f
)

@Serializable
enum class MediaType { IMAGE, EMOJI }
