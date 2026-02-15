package com.oho.hiit_timer.workouts.add

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.hiit_timer.data.HiitWorkoutsRepository
import com.oho.hiit_timer.data.HiitWorkoutsRepository.Companion.TEMP_WORKOUT_ID
import com.oho.hiit_timer.domain.HiitWorkout
import com.oho.hiit_timer.domain.totalDurationWithoutPrepareSec
import com.oho.utils.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CreateEditWorkoutViewModel(
    private val workoutId: String,
    private val hiitWorkoutsRepository: HiitWorkoutsRepository,
    @SuppressLint("StaticFieldLeak") private val context: Context
) : ViewModel() {

    @Immutable
    data class UiState(
        val workoutDomain: HiitWorkout? = null,
        val title: String = "",
        val blocks: List<WorkoutBlockUi> = emptyList(),
        val totalDurationSec: Int = 0,
        val blockContextMenuId: String? = null,
        val enterName: Boolean = false,
    )

    sealed interface Event {
        data object Back : Event
        data class Start(val workoutId: String) : Event
        data object OnAddBlockClicked : Event
    }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<Event>(capacity = Channel.Factory.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            hiitWorkoutsRepository.observeWorkout(HiitWorkoutsRepository.TEMP_WORKOUT_ID)
                .filterNotNull()
                .collect { hiitWorkout ->
                    suppressPersist = true
                    _state.update { s ->
                        s.copy(
                            workoutDomain = hiitWorkout,
                            title = hiitWorkout.name.takeIf { it.isNotBlank() }
                                ?: context.getString(R.string.create_workout_title),
                            totalDurationSec = hiitWorkout.totalDurationWithoutPrepareSec(),
                            blocks = hiitWorkout.exercises.map { exercise ->
                                WorkoutBlockUi(
                                    id = exercise.id,
                                    name = exercise.name,
                                    spec = WorkoutBlockSpec.fromExercise(exercise)
                                )
                            }
                        )
                    }
                    suppressPersist = false
                }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch { _events.send(Event.Back) }
    }

    fun onBlockMoreClicked(blockId: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    blockContextMenuId = blockId
                )
            }
        }
    }

    fun onAddBlockClicked() {
        viewModelScope.launch { _events.send(Event.OnAddBlockClicked) }
    }

    fun onSaveClicked() {
        _state.update {
            it.copy(enterName = true)
        }

    }

    fun onReorderBlocks(fromIndex: Int, toIndex: Int) {
        _state.update {
            val domain = it.workoutDomain
            it.copy(
                blocks = it.blocks.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                },
                workoutDomain = domain?.copy(
                    exercises = domain.exercises.toMutableList().apply {
                        add(toIndex, removeAt(fromIndex))
                    }
                )
            )
        }
        _state.value.workoutDomain?.let {
            schedulePersist(domain = it)
        }
    }

    private fun schedulePersist(domain: HiitWorkout) {
        if (suppressPersist) return

        persistJob?.cancel()
        persistJob = viewModelScope.launch {
            delay(250L) // debounce
            hiitWorkoutsRepository.upsert(
                workout = domain,
                source = HiitWorkoutsRepository.SOURCE_USER,
            )
        }
    }

    fun onDismissedContextMenu() {
        hideContextMenu()
    }

    private fun hideContextMenu() {
        _state.update {
            it.copy(blockContextMenuId = null)
        }
    }

    fun deleteExercise(exerciseId: String) {
        hideContextMenu()
        viewModelScope.launch {
            hiitWorkoutsRepository.deleteExercise(exerciseId)
        }
    }

    fun duplicateExercise(exerciseId: String) {
        hideContextMenu()
        viewModelScope.launch {
            hiitWorkoutsRepository.duplicateExercise(
                exerciseId = exerciseId,
                workoutId = TEMP_WORKOUT_ID
            )
        }
    }

    fun onDismissNameSheet() {
        _state.update {
            it.copy(enterName = false)
        }
    }

    fun onConfirmWorkoutName(name: String) {
        val currentDomain = _state.value.workoutDomain ?: return
        onDismissNameSheet()
        viewModelScope.launch {
            val id = workoutId
            if (id == HiitWorkoutsRepository.TEMP_WORKOUT_ID) {
                hiitWorkoutsRepository.upsert(
                    workout = currentDomain.copy(
                        id = UUID.randomUUID().toString(),
                        name = name
                    ),
                    source = HiitWorkoutsRepository.SOURCE_USER,
                )
            } else { //move from temp to real for edit
                hiitWorkoutsRepository.upsert(
                    workout = currentDomain.copy(
                        id = workoutId,
                        name = name
                    ),
                    source = HiitWorkoutsRepository.SOURCE_USER,
                )
            }
            _events.send(Event.Back)
        }
    }

    private var suppressPersist = false
    private var persistJob: Job? = null
}