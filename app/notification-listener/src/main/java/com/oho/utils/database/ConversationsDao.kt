package com.oho.utils.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oho.utils.database.entity.ConversationEntity
import com.oho.utils.database.entity.ConversationRow
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationsDao {

    @Query(
        """
        SELECT packageName, conversationKey, title, lastPostedAt, lastText, messageCount, isPinned, isMuted
        FROM conversations
        WHERE packageName = :pkg
          AND (:minPostedAt IS NULL OR lastPostedAt >= :minPostedAt)
        ORDER BY isPinned DESC, lastPostedAt DESC
        """
    )
    fun observeConversations(pkg: String, minPostedAt: Long?): Flow<List<ConversationRow>>

    @Query(
        """
        SELECT COUNT(*)
        FROM conversations
        WHERE packageName = :pkg AND conversationKey != :allKey
        """
    )
    suspend fun countRealConversations(pkg: String, allKey: String = CONVERSATION_ALL): Long

    @Query(
        """
        SELECT * FROM conversations
        WHERE packageName = :pkg AND conversationKey = :ck
        LIMIT 1
        """
    )
    suspend fun get(pkg: String, ck: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ConversationEntity)

    @Query(
        """
        UPDATE conversations
        SET isPinned = :pinned
        WHERE packageName = :pkg AND conversationKey = :ck
        """
    )
    suspend fun setPinned(pkg: String, ck: String, pinned: Boolean)

    @Query(
        """
        UPDATE conversations
        SET isMuted = :muted
        WHERE packageName = :pkg AND conversationKey = :ck
        """
    )
    suspend fun setMuted(pkg: String, ck: String, muted: Boolean)
}