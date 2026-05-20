package com.pbrockt.tagebuch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_days")
data class DiaryDay(
    @PrimaryKey val date: String,
    val mood: String? = null,     // "great","good","okay","bad","awful"
    val weather: String? = null,  // "sunny","cloudy","rainy","snowy","stormy"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
