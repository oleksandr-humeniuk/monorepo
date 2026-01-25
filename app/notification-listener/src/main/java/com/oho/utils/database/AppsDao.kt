package com.oho.utils.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oho.utils.database.entity.AppEntity
import com.oho.utils.database.entity.AppRow
import kotlinx.coroutines.flow.Flow

@Dao
interface AppsDao {

    @Query(
        """
        SELECT packageName, appName, isExcluded, isPinned, lastPostedAt, lastTitle, lastText, totalCount
        FROM apps
        WHERE isExcluded = 0
        ORDER BY isPinned DESC, lastPostedAt DESC
        """
    )
    fun observeApps(): Flow<List<AppRow>>

    @Query("SELECT * FROM apps WHERE packageName = :pkg LIMIT 1")
    suspend fun get(pkg: String): AppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppEntity)

    @Query("UPDATE apps SET isExcluded = :excluded WHERE packageName = :pkg")
    suspend fun setExcluded(pkg: String, excluded: Boolean)

    @Query("UPDATE apps SET isPinned = :pinned WHERE packageName = :pkg")
    suspend fun setPinned(pkg: String, pinned: Boolean)
}