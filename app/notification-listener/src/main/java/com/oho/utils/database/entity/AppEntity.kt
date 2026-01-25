package com.oho.utils.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "apps",
    indices = [
        Index(value = ["isPinned", "lastPostedAt"]),
        Index(value = ["lastPostedAt"]),
    ],
)
data class AppEntity(
    @PrimaryKey val packageName: String,
    val appName: String? = null,
    val isExcluded: Boolean = false,
    val isPinned: Boolean = false,
    val lastPostedAt: Long = 0L,
    val lastTitle: String? = null,
    val lastText: String? = null,
    val totalCount: Long = 0L,
)