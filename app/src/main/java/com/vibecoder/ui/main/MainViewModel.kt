package com.vibecoder.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibecoder.data.github.GitHubRepository
import com.vibecoder.data.storage.Database
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val gitHubRepository: GitHubRepository,
    private val database: Database
) : ViewModel() {

    fun initialize() {
        viewModelScope.launch {
            // Initialize app state
        }
    }

    fun signOut() {
        viewModelScope.launch {
            gitHubRepository.disconnect()
        }
    }
}