package com.oho.utils.database.entity

data class AppRow(
    val packageName: String,
    val appName: String?,
    val isExcluded: Boolean,
    val isPinned: Boolean,
    val lastPostedAt: Long,
    val lastTitle: String?,
    val lastText: String?,
    val totalCount: Long,
)

