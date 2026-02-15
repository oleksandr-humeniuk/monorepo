package com.oho.hiit_timer.workouts.add

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.hiit_timer.data.HiitWorkoutsRepository
import com.oho.hiit_timer.domain.HiitWorkout
import com.oho.hiit_timer.domain.totalDurationWithoutPrepareSec
import com.oho.utils.R
import kotlinx.coroutines.channels.Channel
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
        val title: String,
        val blocks: List<WorkoutBlockUi> = emptyList(),
        val totalDurationSec: Int = 0,
    )

    sealed interface Event {
        data object Back : Event
        data class Start(val workoutId: String) : Event
        data class OpenBlockMenu(val blockId: String) : Event
        data object OnAddBlockClicked : Event
    }

    private val _state = MutableStateFlow(
        UiState(
            title = ""
        )
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<Event>(capacity = Channel.Factory.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            hiitWorkoutsRepository.observeWorkout(workoutId)
                .filterNotNull()
                .collect { hiitWorkout ->
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
                }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch { _events.send(Event.Back) }
    }

    fun onBlockMoreClicked(blockId: String) {
        viewModelScope.launch { _events.send(Event.OpenBlockMenu(blockId)) }
    }

    fun onAddBlockClicked() {
        viewModelScope.launch { _events.send(Event.OnAddBlockClicked) }
    }

    fun onSaveClicked() {
        val currentDomain = _state.value.workoutDomain ?: return
        viewModelScope.launch {
            val id = workoutId
            if (id == HiitWorkoutsRepository.TEMP_WORKOUT_ID) {
                hiitWorkoutsRepository.upsert(
                    workout = currentDomain.copy(
                        id = UUID.randomUUID().toString(),
                        name = "Current"
                    ),
                    source = HiitWorkoutsRepository.SOURCE_USER,
                )
            } else {
                //just store
            }
            _events.send(Event.Back)
        }
    }

    fun onReorderBlocks(fromIndex: Int, toIndex: Int) {
        _state.update {
            it.copy(
                blocks = it.blocks.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                }
            )
        }
    }

}