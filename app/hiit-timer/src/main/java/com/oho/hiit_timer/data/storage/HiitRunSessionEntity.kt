package com.oho.hiit_timer.data.storage

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Single-row table (id=1) that stores the currently running workout session snapshot.
 *
 * Persisted only on:
 * - start
 * - pause/resume
 * - manual next/prev
 * - auto segment transition
 * - stop/finish
 *
 * NOT persisted on each tick.
 */
@Entity(tableName = "hiit_run_session")
data class HiitRunSessionEntity(
    @PrimaryKey val id: Long = ACTIVE_ID,

    val workoutId: String,

    /** Current segment index inside planned segments list. */
    val segmentIndex: Int,

    /** Wall clock time when current segment started (epoch millis). */
    val segmentStartedAtEpochMs: Long,

    /** Pause state */
    val isPaused: Boolean,
    val pausedAtEpochMs: Long?,      // set only when paused
    val accumulatedPausedMs: Long,   // total paused duration for current segment

    /** Run lifecycle */
    val isFinished: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
) {
    companion object {
        const val ACTIVE_ID: Long = 1L
    }
}