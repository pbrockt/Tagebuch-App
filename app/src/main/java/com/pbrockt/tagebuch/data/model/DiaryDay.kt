package com.pbrockt.tagebuch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "diary_days")
data class DiaryDay(
    @PrimaryKey val date: String,
    val mood: String? = null,
    val weather: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
