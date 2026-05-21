package com.pbrockt.tagebuch.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "diary_pages",
    foreignKeys = [ForeignKey(
        entity = DiaryDay::class,
        parentColumns = ["date"],
        childColumns = ["dayDate"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("dayDate")]
)
data class DiaryPage(
    @PrimaryKey val id: String,
    val dayDate: String,
    val pageIndex: Int = 0,
    val content: String = "",
    val mediaJson: String = "[]",
    val pageColor: String? = null,  // Hex-Farbe z.B. "#FFF9C4" oder null = Standard
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
