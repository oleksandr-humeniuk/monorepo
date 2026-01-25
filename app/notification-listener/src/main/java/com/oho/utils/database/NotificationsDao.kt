package com.oho.utils.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.oho.utils.database.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationsDao {

    @Query(
        """
        SELECT *
        FROM notifications
        WHERE packageName = :pkg
          AND (:minPostedAt IS NULL OR postedAt >= :minPostedAt)
        ORDER BY postedAt DESC
        LIMIT :limit
        """
    )
    fun observeAppNotifications(
        pkg: String,
        minPostedAt: Long?,
        limit: Int,
    ): Flow<List<NotificationEntity>>

    @Query(
        """
        SELECT *
        FROM notifications
        WHERE packageName = :pkg
          AND conversationKey = :ck
          AND (:minPostedAt IS NULL OR postedAt >= :minPostedAt)
        ORDER BY postedAt DESC
        LIMIT :limit
        """
    )
    fun observeConversationNotifications(
        pkg: String,
        ck: String,
        minPostedAt: Long?,
        limit: Int,
    ): Flow<List<NotificationEntity>>

    @Query(
        """
        SELECT *
        FROM notifications
        WHERE packageName = :pkg
          AND (:minPostedAt IS NULL OR postedAt >= :minPostedAt)
          AND (
            (title IS NOT NULL AND title LIKE '%' || :q || '%') OR
            (text IS NOT NULL AND text LIKE '%' || :q || '%') OR
            (bigText IS NOT NULL AND bigText LIKE '%' || :q || '%')
          )
        ORDER BY postedAt DESC
        LIMIT :limit
        """
    )
    suspend fun searchInApp(
        pkg: String,
        q: String,
        minPostedAt: Long?,
        limit: Int = 200,
    ): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE packageName = :pkg")
    suspend fun countAllInApp(pkg: String): Long

    @Query(
        """
        SELECT COUNT(*)
        FROM notifications
        WHERE packageName = :pkg
          AND (:minPostedAt IS NULL OR postedAt >= :minPostedAt)
        """
    )
    suspend fun countVisibleInApp(pkg: String, minPostedAt: Long?): Long

    @Query("SELECT COUNT(*) FROM notifications WHERE packageName = :pkg AND conversationKey = :ck")
    suspend fun countAllInConversation(pkg: String, ck: String): Long

    @Query(
        """
        SELECT COUNT(*)
        FROM notifications
        WHERE packageName = :pkg AND conversationKey = :ck
          AND (:minPostedAt IS NULL OR postedAt >= :minPostedAt)
        """
    )
    suspend fun countVisibleInConversation(pkg: String, ck: String, minPostedAt: Long?): Long

    @Insert
    suspend fun insert(entity: NotificationEntity): Long

    @Query(
        """
        UPDATE notifications
        SET removedAt = :removedAt, removeCause = :cause
        WHERE sbnKey = :sbnKey AND removedAt IS NULL
        """
    )
    suspend fun markRemovedBySbnKey(sbnKey: String, removedAt: Long, cause: Int?)
}