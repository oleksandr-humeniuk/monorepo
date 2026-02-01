package com.oho.hiit_timer.data.storage

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hiit_workouts",
    indices = [
        Index("source"),
        Index("createdAt"),
        Index("updatedAt"),
    ]
)
data class WorkoutEntity(
    @PrimaryKey val id: String, // "quick_start" або UUID/ULID
    val name: String,
    val prepareSec: Int,

    /**
     * 0 = SYSTEM (quick/presets shipped)
     * 1 = USER (created by user)
     * 2 = PRESET (downloaded/remote in future)
     */
    val source: Int,

    val createdAt: Long,
    val updatedAt: Long,

    // optional flags for UX
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false, // soft delete if you want
)
