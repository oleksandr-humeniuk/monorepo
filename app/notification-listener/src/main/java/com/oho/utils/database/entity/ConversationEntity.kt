package com.oho.utils.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "conversations",
    primaryKeys = ["packageName", "conversationKey"],
    indices = [
        Index(value = ["packageName", "lastPostedAt"]),
        Index(value = ["packageName", "isPinned", "lastPostedAt"]),
    ],
)
data class ConversationEntity(
    val packageName: String,
    val conversationKey: String,
    val title: String? = null,
    val lastPostedAt: Long = 0L,
    val lastText: String? = null,
    val messageCount: Long = 0L,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
)