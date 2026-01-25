package com.oho.utils.database.entity

data class ConversationRow(
    val packageName: String,
    val conversationKey: String,
    val title: String?,
    val lastPostedAt: Long,
    val lastText: String?,
    val messageCount: Long,
    val isPinned: Boolean,
    val isMuted: Boolean,
)