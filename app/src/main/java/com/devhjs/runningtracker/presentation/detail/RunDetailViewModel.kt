package com.devhjs.runningtracker.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.runningtracker.domain.affiliate.CoupangRecommender
import com.devhjs.runningtracker.domain.repository.MainRepository
import com.devhjs.runningtracker.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val mainRepository: MainRepository
) : ViewModel() {

    private val runId: Int = checkNotNull(savedStateHandle[Screen.RunDetailScreen.ARG_RUN_ID])

    private val _state = MutableStateFlow(RunDetailState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<RunDetailEvent>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            // 목록과 달리 여기서는 기록 한 건과 그 경로만 읽는다.
            val run = mainRepository.getRunById(runId)
            val pathPoints = if (run != null) mainRepository.getRunPath(runId) else emptyList()
            _state.update {
                it.copy(
                    run = run,
                    pathPoints = pathPoints,
                    isLoaded = true,
                    coupangRecommendation = run?.let { r ->
                        CoupangRecommender.forRun(r.distanceInMeters.toFloat())
                    }
                )
            }
        }
    }

    fun onAction(action: RunDetailAction) {
        when (action) {
            RunDetailAction.OnBackClick -> {
                viewModelScope.launch { _event.emit(RunDetailEvent.NavigateUp) }
            }
            RunDetailAction.OnDeleteClick -> {
                _state.update { it.copy(showDeleteDialog = true) }
            }
            RunDetailAction.OnDeleteDismiss -> {
                _state.update { it.copy(showDeleteDialog = false) }
            }
            RunDetailAction.OnDeleteConfirm -> {
                val run = _state.value.run ?: return
                _state.update { it.copy(showDeleteDialog = false) }
                viewModelScope.launch {
                    mainRepository.deleteRun(run)
                    _event.emit(RunDetailEvent.NavigateUp)
                }
            }
            is RunDetailAction.OnCoupangClick -> {
                viewModelScope.launch { _event.emit(RunDetailEvent.OpenUrl(action.url)) }
            }
        }
    }
}
