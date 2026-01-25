package com.oho.utils.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["packageName", "postedAt"]),
        Index(value = ["packageName", "conversationKey", "postedAt"]),
        Index(value = ["packageName", "dedupeKey"]),
        Index(value = ["sbnKey"]),
        Index(value = ["postedAt"]),
    ],
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val packageName: String,
    val conversationKey: String,

    val postedAt: Long,

    val title: String? = null,
    val text: String? = null,
    val bigText: String? = null,

    val channelId: String? = null,
    val category: String? = null,
    val groupKey: String? = null,

    val dedupeKey: String,
    val dedupeCount: Int = 1,
    val lastSeenAt: Long = postedAt,

    val removedAt: Long? = null,
    val removeCause: Int? = null,

    val sbnKey: String? = null,
    val contentIntentBlob: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotificationEntity

        if (id != other.id) return false
        if (postedAt != other.postedAt) return false
        if (dedupeCount != other.dedupeCount) return false
        if (lastSeenAt != other.lastSeenAt) return false
        if (removedAt != other.removedAt) return false
        if (removeCause != other.removeCause) return false
        if (packageName != other.packageName) return false
        if (conversationKey != other.conversationKey) return false
        if (title != other.title) return false
        if (text != other.text) return false
        if (bigText != other.bigText) return false
        if (channelId != other.channelId) return false
        if (category != other.category) return false
        if (groupKey != other.groupKey) return false
        if (dedupeKey != other.dedupeKey) return false
        if (sbnKey != other.sbnKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + postedAt.hashCode()
        result = 31 * result + dedupeCount
        result = 31 * result + lastSeenAt.hashCode()
        result = 31 * result + (removedAt?.hashCode() ?: 0)
        result = 31 * result + (removeCause ?: 0)
        result = 31 * result + packageName.hashCode()
        result = 31 * result + conversationKey.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + (bigText?.hashCode() ?: 0)
        result = 31 * result + (channelId?.hashCode() ?: 0)
        result = 31 * result + (category?.hashCode() ?: 0)
        result = 31 * result + (groupKey?.hashCode() ?: 0)
        result = 31 * result + dedupeKey.hashCode()
        result = 31 * result + (sbnKey?.hashCode() ?: 0)
        return result
    }
}