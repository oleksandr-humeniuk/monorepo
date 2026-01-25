package com.oho.utils.service

import android.app.Notification
import android.app.PendingIntent
import android.os.Bundle
import android.os.Parcel
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.oho.utils.database.CONVERSATION_ALL

fun PendingIntent.toBlob(): ByteArray {
    val p = Parcel.obtain()
    return try {
        writeToParcel(p, 0)
        p.marshall()
    } finally {
        p.recycle()
    }
}


/**
 * Remove cause mapping for analytics/UI.
 * 0 Unknown/System, 1 Click, 2 Swipe, 3 App, 4 Timeout
 */
fun mapRemoveCauseToInt(reason: Int): Int = when (reason) {
    NotificationListenerService.REASON_CLICK -> 1
    NotificationListenerService.REASON_CANCEL -> 2
    NotificationListenerService.REASON_APP_CANCEL,
    NotificationListenerService.REASON_APP_CANCEL_ALL -> 3

    NotificationListenerService.REASON_TIMEOUT -> 4
    else -> 0
}

fun computeConversationKey(sbn: StatusBarNotification, extras: Bundle): String {
    val shortcutId = runCatching { sbn.notification.shortcutId }.getOrNull()
    if (!shortcutId.isNullOrBlank()) return "sc:${shortcutId.normalizeKey()}"

    // 2) conversation title
    val ct = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
    if (!ct.isNullOrBlank()) return "ct:${ct.normalizeKey()}"

    // 3) groupKey (meh, but stable-ish)
    val gk = sbn.groupKey
    if (!gk.isNullOrBlank()) return "gk:${gk.normalizeKey()}"

    return CONVERSATION_ALL
}

fun computeDedupeKey(
    pkg: String,
    conversationKey: String,
    channelId: String?,
    title: String?,
    text: String?,
    bigText: String?,
): String {
    val t = (title ?: "").normalizeKey().take(80)
    val b = (text ?: bigText ?: "").normalizeKey().take(160)
    val ch = channelId?.normalizeKey().orEmpty()

    return buildString(256) {
        append(pkg)
        append('|')
        append(conversationKey)
        append('|')
        append(ch)
        append('|')
        append(t)
        append('|')
        append(b)
    }
}

private fun String.normalizeKey(): String =
    trim()
        .lowercase()
        .replace(Regex("\\s+"), " ")
        .replace("\u200B", "")