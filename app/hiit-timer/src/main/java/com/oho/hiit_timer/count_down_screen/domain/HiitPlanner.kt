package com.oho.hiit_timer.count_down_screen.domain

/**
 * Flatten workout into a linear list of segments: Prepare, (Work, Rest)*, Done
 * Rules:
 * - Prepare only once before entire workout.
 * - Rest after last set can be:
 *   - absent (hasRestAfterLastWork = false OR lastRestSec == 0)
 *   - default restSec
 *   - custom lastRestSec
 */
object HiitPlanner {
    fun plan(workout: HiitWorkout): List<HiitSegment> {
        val out = ArrayList<HiitSegment>(64)

        if (workout.prepareSec > 0) {
            out += HiitSegment.Prepare(workout.prepareSec)
        }

        workout.exercises.forEachIndexed { exIndex, ex ->
            val setsTotal = ex.sets
            for (setIdx in 1..setsTotal) {
                out += HiitSegment.Work(
                    exerciseName = ex.name,
                    setIndex = setIdx,
                    setsTotal = setsTotal,
                    durationSec = ex.workSec,
                )

                val isLastSet = setIdx == setsTotal
                val isLastExercise = exIndex == workout.exercises.lastIndex

                val shouldHaveRest = ex.hasRestAfterLastWork && !(isLastExercise && isLastSet && (ex.lastRestSec == 0))
                if (!shouldHaveRest) continue

                val restDur = if (isLastSet) (ex.lastRestSec ?: ex.restSec) else ex.restSec
                if (restDur <= 0) continue

                val nextExerciseName = when {
                    isLastSet && !isLastExercise -> workout.exercises[exIndex + 1].name
                    else -> ex.name
                }
                val nextIsWork = true

                out += HiitSegment.Rest(
                    nextExerciseName = nextExerciseName,
                    nextIsWork = nextIsWork,
                    setIndex = setIdx,
                    setsTotal = setsTotal,
                    durationSec = restDur,
                )
            }
        }

        out += HiitSegment.Done()
        return out
    }

    fun totalDurationSec(segments: List<HiitSegment>): Int =
        segments.sumOf { it.durationSec }
}