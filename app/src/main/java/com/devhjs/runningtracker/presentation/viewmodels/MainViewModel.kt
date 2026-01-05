package com.devhjs.runningtracker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.runningtracker.domain.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {
    
    fun insertRun(run: com.devhjs.runningtracker.domain.model.Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}
