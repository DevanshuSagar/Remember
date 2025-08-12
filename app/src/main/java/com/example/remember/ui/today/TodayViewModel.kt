package com.example.remember.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remember.data.Repository
import com.example.remember.data.db.CardWithContent
import com.example.remember.data.db.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

data class TodayUiState(
    val dueCards: List<CardWithContent> = emptyList(),
    val isLoading: Boolean = true,
    val settings: Settings? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private  val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDueCards()
        loadSettings()
    }

    private fun loadDueCards() {
        viewModelScope.launch {
            repository.getDueCards().collect { cards ->
                _uiState.update { it.copy(dueCards = cards, isLoading = false) }
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.getSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun onCardForgotten(cardWithContent: CardWithContent) {
        viewModelScope.launch {
            val card = cardWithContent.card
            val intervals = card.intervals

            if (intervals.isEmpty()) return@launch

            val currentIndex = intervals.indexOf(card.currentInterval)

            if (currentIndex == intervals.lastIndex) {
                val finishedCard = card.copy(isFinished = true)
                repository.updateCard(finishedCard)
            } else {
                val nextInterval = when {
                    currentIndex != -1 -> intervals[currentIndex]
                    else -> intervals.first()
                }
                val updatedCard = card.copy(
                    currentInterval = nextInterval,
                    nextReviewDate = getNextReviewDate(nextInterval)
                )
                repository.updateCard(updatedCard)
            }
        }
    }

    fun onCardRemembered(cardWithContent: CardWithContent) {
        viewModelScope.launch {
            val card = cardWithContent.card
            val intervals = card.intervals

            if (intervals.isEmpty()) return@launch

            val currentIndex = intervals.indexOf(card.currentInterval)

            if (currentIndex == intervals.lastIndex) {
                val finishedCard = card.copy(isFinished = true)
                repository.updateCard(finishedCard)
            } else {
                val nextInterval = when {
                    currentIndex != -1 -> intervals[currentIndex + 1]
                    else -> intervals.first()
                }
                val updatedCard = card.copy(
                    currentInterval = nextInterval,
                    nextReviewDate = getNextReviewDate(nextInterval)
                )
                repository.updateCard(updatedCard)
            }
        }
    }

    private fun getNextReviewDate(daysToAdd: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return calendar.time
    }
}