package com.oho.utils.service

import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.oho.utils.database.NotificationDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

//TODO: Add lru cache to handle pending intents for happy path
class NotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db by inject<NotificationDatabase>()

    override fun onNotificationPosted(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?
    ) {

        if (sbn == null) return

        if ((sbn.notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) return

        val n = sbn.notification
        val e = n.extras

        val pkg = sbn.packageName
        val postedAt = sbn.postTime

        val title = e.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = e.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = e.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        val channelId = if (Build.VERSION.SDK_INT >= 26) n.channelId else null
        val category = n.category
        val groupKey = sbn.groupKey

        val conversationTitle = e.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
        val conversationKey = computeConversationKey(sbn, e)

        val dedupeKey = computeDedupeKey(
            pkg = pkg,
            conversationKey = conversationKey,
            channelId = channelId,
            title = title,
            text = text,
            bigText = bigText,
        )

        val contentIntentBlob = runCatching { n.contentIntent?.toBlob() }.getOrNull()
        val sbnKey = sbn.key

        val appName = runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(pkg, 0)
            ).toString()
        }.getOrNull()

        scope.launch {
            db.insertNotificationAndUpdateSummaries(
                appName = appName,
                pkg = pkg,
                conversationKey = conversationKey,
                conversationTitle = conversationTitle,
                postedAt = postedAt,
                title = title,
                text = text,
                bigText = bigText,
                channelId = channelId,
                category = category,
                groupKey = groupKey,
                dedupeKey = dedupeKey,
                sbnKey = sbnKey,
                contentIntentBlob = contentIntentBlob,
            )
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?,
        reason: Int
    ) {
        if (sbn == null) return

        val sbnKey = sbn.key
        val removedAt = System.currentTimeMillis()
        val cause = mapRemoveCauseToInt(reason)

        scope.launch {
            db.notificationsDao().markRemovedBySbnKey(
                sbnKey = sbnKey,
                removedAt = removedAt,
                cause = cause,
            )
        }
    }
}
