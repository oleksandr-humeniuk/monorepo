package com.oho.utils.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimeLabel(
    lastPostedAt: Long,
    now: Long = System.currentTimeMillis(),
): String? {
    val diffMs = now - lastPostedAt
    if (diffMs < 0) return null // clock skew safety

    val minutes = diffMs / 60_000
    val hours = diffMs / 3_600_000
    val days = diffMs / 86_400_000

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> formatDate(lastPostedAt)  // e.g. Jan 24
    }
}

private fun formatDate(time: Long): String {
    val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
    return formatter.format(Date(time))
}