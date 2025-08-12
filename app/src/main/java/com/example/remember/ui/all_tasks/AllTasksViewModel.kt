package com.example.remember.ui.all_tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remember.data.Repository
import com.example.remember.data.db.CardWithContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllTasksUiState(
    val cards: List<CardWithContent> = emptyList()
)

@HiltViewModel
class AllTasksViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AllTasksUiState())
    val uiState = _uiState.asStateFlow()
    
    init {
        loadAllCards()
    }

    private fun loadAllCards() {
        viewModelScope.launch {
            repository.getAllCards().collect { allCards ->
                val activeCards = allCards.filter { !it.card.isFinished }
                _uiState.update { currentState ->
                    currentState.copy(cards = activeCards)
                }
            }
        }
    }

    fun deleteCard(cardWithContent: CardWithContent) {
        viewModelScope.launch {
            repository.deleteCard(cardWithContent.card)
        }
    }
}
