package com.pbrockt.tagebuch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_days")
data class DiaryDay(
    @PrimaryKey val date: String, // ISO-8601: "2024-01-15"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
