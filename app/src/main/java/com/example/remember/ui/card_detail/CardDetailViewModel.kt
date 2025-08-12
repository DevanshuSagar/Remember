package com.example.remember.ui.card_detail

import androidx.lifecycle.SavedStateHandle
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

data class CardDetailsUiState(
    val cardWithContent: CardWithContent? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val repository: Repository
): ViewModel() {
    private val _uiState = MutableStateFlow(CardDetailsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val cardId: Int? = savedStateHandle["cardId"]
        if (cardId != null) {
            loadCard(cardId)
        }
    }

    private fun loadCard(cardId: Int) {
        viewModelScope.launch {
            repository.getCardById(cardId).collect { card ->
                _uiState.update { it.copy(cardWithContent = card, isLoading = false) }
            }
        }
    }

}