package com.vibecoder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.viewModelScope
import com.vibecoder.storage.prefs.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _isSetupComplete = MutableStateFlow(false)
    val isSetupComplete = _isSetupComplete.asStateFlow()

    private val _currentProjectId = MutableStateFlow<String?>(null)
    val currentProjectId = _currentProjectId.asStateFlow()

    init {
        loadSetupState()
    }

    private fun loadSetupState() {
        viewModelScope.launch {
            val complete = preferencesRepository.isSetupComplete()
            _isSetupComplete.value = complete
        }
    }

    fun markSetupComplete() {
        viewModelScope.launch {
            preferencesRepository.setSetupComplete(true)
            _isSetupComplete.value = true
        }
    }

    fun setCurrentProject(projectId: String?) {
        _currentProjectId.value = projectId
    }
}