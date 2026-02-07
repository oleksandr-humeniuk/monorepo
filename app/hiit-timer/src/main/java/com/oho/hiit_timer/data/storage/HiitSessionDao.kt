package com.oho.hiit_timer.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HiitRunSessionDao {

    @Query("SELECT * FROM hiit_run_session WHERE id = 1 LIMIT 1")
    fun observe(): Flow<HiitRunSessionEntity?>

    @Query("SELECT * FROM hiit_run_session WHERE id = 1 LIMIT 1")
    suspend fun get(): HiitRunSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HiitRunSessionEntity)

    @Query("DELETE FROM hiit_run_session WHERE id = 1")
    suspend fun clear()
}