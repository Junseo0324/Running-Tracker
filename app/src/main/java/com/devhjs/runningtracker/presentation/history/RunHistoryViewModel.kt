package com.devhjs.runningtracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.runningtracker.domain.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunHistoryViewModel @Inject constructor(
    // init 블록에서 데이터를 가져온 후에 쓰지 않기 때문에 파라미터로 사용
    mainRepository: MainRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RunHistoryState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<RunHistoryEvent>()
    val event = _event.asSharedFlow()

    init {
        mainRepository.getAllRunsSortedByDate().onEach { runs ->
            _state.update { it.copy(runs = runs) }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: RunHistoryAction) {
        when (action) {
            RunHistoryAction.OnBackClick -> {
                viewModelScope.launch {
                    _event.emit(RunHistoryEvent.NavigateUp)
                }
            }
        }
    }
}
