package com.applicationrush.elise2027.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.applicationrush.elise2027.data.model.CandidateUiState
import com.applicationrush.elise2027.data.repository.VoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VoteViewModel(app: Application) : AndroidViewModel(app) {

    val repository = VoteRepository(app)

    val onboardingDone: StateFlow<Boolean> = repository.onboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val votedCandidateId: StateFlow<String?> = repository.votedCandidateId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Initialised immédiatement avec les candidats hardcodés (0 votes) — jamais vide
    private val _candidates = MutableStateFlow<List<CandidateUiState>>(repository.baseUiList())
    val candidates: StateFlow<List<CandidateUiState>> = _candidates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isMockMode = MutableStateFlow(false)
    val isMockMode: StateFlow<Boolean> = _isMockMode.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (_isMockMode.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching { repository.fetchVotes() }
                .onSuccess { _candidates.value = it }
                .onFailure {
                    // Réseau indisponible : on garde les candidats affichés,
                    // on met juste à jour le vote connu depuis le DataStore
                    _candidates.update { list ->
                        list.map { c -> c.copy(isVotedFor = c.info.id == votedCandidateId.value) }
                    }
                    _error.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun toggleMockMode() {
        _isMockMode.update { !it }
        if (_isMockMode.value) {
            _candidates.value = repository.mockUiList(votedCandidateId.value)
        } else {
            refresh()
        }
    }

    fun vote(candidateId: String) {
        if (_isMockMode.value) {
            val currentVoted = _candidates.value.firstOrNull { it.isVotedFor }?.info?.id
            val newVoted = if (currentVoted == candidateId) null else candidateId
            _candidates.update { list -> list.map { it.copy(isVotedFor = it.info.id == newVoted) } }
            return
        }
        viewModelScope.launch {
            runCatching { repository.vote(candidateId) }
                .onSuccess { refresh() }
                .onFailure { _error.value = it.message }
        }
    }

    fun markOnboardingDone() {
        viewModelScope.launch { repository.markOnboardingDone() }
    }

    fun clearError() { _error.value = null }
}
