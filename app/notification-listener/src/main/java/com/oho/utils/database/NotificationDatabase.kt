package com.oho.utils.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.oho.utils.database.entity.AppEntity
import com.oho.utils.database.entity.ConversationEntity
import com.oho.utils.database.entity.NotificationEntity

const val CONVERSATION_ALL = "__all__"

@Database(
    entities = [
        AppEntity::class,
        ConversationEntity::class,
        NotificationEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class NotificationDatabase : RoomDatabase() {

    abstract fun appsDao(): AppsDao
    abstract fun conversationsDao(): ConversationsDao
    abstract fun notificationsDao(): NotificationsDao

    /**
     * V1 write path for NotificationListener:
     * - insert notification
     * - update app summary
     * - update conversation summary
     *
     * Dedupe strategy in V1:
     * - simplest is "insert every post" (works; bigger DB)
     * - if you want "1 entry + counter", add a separate dedupe upsert later.
     */
    @Transaction
    open suspend fun insertNotificationAndUpdateSummaries(
        appName: String?,
        pkg: String,
        conversationKey: String,
        conversationTitle: String?,
        postedAt: Long,
        title: String?,
        text: String?,
        bigText: String?,
        channelId: String?,
        category: String?,
        groupKey: String?,
        dedupeKey: String,
        sbnKey: String?,
        contentIntentBlob: ByteArray?,
    ): Long {
        val apps = appsDao()
        val convs = conversationsDao()
        val notifs = notificationsDao()

        val ck = conversationKey.ifBlank { CONVERSATION_ALL }
        val bestText = (text ?: bigText ?: title)?.takeIf { it.isNotBlank() }

        // 1) Insert notification
        val notifId = notifs.insert(
            NotificationEntity(
                packageName = pkg,
                conversationKey = ck,
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
        )

        // 2) App summary upsert
        val prevApp = apps.get(pkg)
        apps.upsert(
            AppEntity(
                packageName = pkg,
                appName = appName ?: prevApp?.appName,
                isExcluded = prevApp?.isExcluded ?: false,
                isPinned = prevApp?.isPinned ?: false,
                lastPostedAt = maxOf(prevApp?.lastPostedAt ?: 0L, postedAt),
                lastTitle = title ?: prevApp?.lastTitle,
                lastText = bestText ?: prevApp?.lastText,
                totalCount = (prevApp?.totalCount ?: 0L) + 1L,
            )
        )

        // 3) Conversation summary upsert
        val prevConv = convs.get(pkg, ck)
        convs.upsert(
            ConversationEntity(
                packageName = pkg,
                conversationKey = ck,
                title = conversationTitle ?: prevConv?.title,
                lastPostedAt = maxOf(prevConv?.lastPostedAt ?: 0L, postedAt),
                lastText = bestText ?: prevConv?.lastText,
                messageCount = (prevConv?.messageCount ?: 0L) + 1L,
                isPinned = prevConv?.isPinned ?: false,
                isMuted = prevConv?.isMuted ?: false,
            )
        )

        return notifId
    }
}