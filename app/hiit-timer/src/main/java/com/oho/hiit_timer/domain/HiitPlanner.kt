package com.oho.hiit_timer.domain

/**
 * Flatten workout into a linear list of segments:
 * Prepare, (Work, Rest)*, Done
 *
 * Rules:
 * - Prepare only once before the entire workout.
 * - For each exercise:
 *   - After each WORK set except the last one -> always use restSec (if > 0)
 *   - After the LAST work set -> apply restAfterLastWork policy
 */
object HiitPlanner {

    fun plan(workout: HiitWorkout): List<HiitSegment> {
        val out = ArrayList<HiitSegment>(64)

        if (workout.prepareSec > 0) {
            out += HiitSegment.Prepare(workout.prepareSec)
        }

        workout.exercises.forEach { ex ->
            val setsTotal = ex.sets.coerceAtLeast(0)
            for (setIdx in 1..setsTotal) {
                out += HiitSegment.Work(
                    exerciseName = ex.name,
                    setIndex = setIdx,
                    setsTotal = setsTotal,
                    durationSec = ex.workSec.coerceAtLeast(0),
                )

                val isLastSet = setIdx == setsTotal
                val restDur = if (!isLastSet) {
                    ex.restSec
                } else {
                    when (val p = ex.restAfterLastWork) {
                        is RestAfterLastWorkPolicy.SameAsRegular -> ex.restSec
                        is RestAfterLastWorkPolicy.None -> 0
                        is RestAfterLastWorkPolicy.Custom -> p.seconds
                    }
                }

                if (restDur > 0) {
                    out += HiitSegment.Rest(
                        exerciseName = ex.name,
                        setIndex = setIdx,
                        setsTotal = setsTotal,
                        durationSec = restDur,
                    )
                }
            }
        }

        out += HiitSegment.Done()
        return out
    }

    fun totalDurationSec(segments: List<HiitSegment>): Int =
        segments.sumOf { it.durationSec.coerceAtLeast(0) }
}
