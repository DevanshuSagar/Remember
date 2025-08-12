package com.example.remember.ui.add_edit_card

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remember.data.Repository
import com.example.remember.data.db.Card
import com.example.remember.data.db.ContentItem
import com.example.remember.data.db.ContentType
import com.example.remember.ui.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DraftContentItem(
    val type: ContentType,
    val persistentUri: String,
    val sourceUri: String
)

data class AddEditCardUiState(
    val cardId: Int = 0,
    val topic: String = "",
    val notes: String = "",
    val availableIntervalSets: List<IntervalSet> = listOf(
        IntervalSet("Standard", listOf(1, 3, 7, 14, 30, 60)),
        IntervalSet("Quick", listOf(1, 2, 4, 8)),
        IntervalSet("Academic", listOf(1, 5, 15, 30, 90))
    ),
    val selectedIntervalSet: IntervalSet = availableIntervalSets.first(),
    val draftContentItems: List<DraftContentItem> = emptyList(),
    val originalCard: Card? = null
)

data class IntervalSet(val name: String, val intervals: List<Int>)

@HiltViewModel
class AddEditCardViewModel @Inject constructor(
    private val application: Application,
    private val repository: Repository, 
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditCardUiState())
    val uiState = _uiState.asStateFlow()
    
    private val prevCardId: Int = savedStateHandle.get<Int>("cardId") ?: -1
    
    init {
        if (prevCardId != -1) {
            loadCard(prevCardId)
        }
    }

    private fun loadCard(prevCardId: Int) {
        viewModelScope.launch {
            val cardWithContent = repository.getCardById(prevCardId).first()
            if (cardWithContent != null) {
                val card = cardWithContent.card
                val draftItems = cardWithContent.contentItems.map {
                    DraftContentItem(
                        type = it.type,
                        persistentUri = it.content,
                        sourceUri = it.content
                    )
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        cardId = card.id,
                        topic = card.topic,
                        notes = card.notes,
                        selectedIntervalSet = IntervalSet("Previously selected", card.intervals),
                        draftContentItems = draftItems,
                        originalCard = card
                    )
                }
            }
        }
    }


    fun onTopicChange(newTopic: String) {
        _uiState.update {
            it.copy(topic = newTopic)
        }
    }

    fun onNotesChange(notes: String) {
        _uiState.update {
            it.copy(notes = notes)
        }
    }

    fun onIntervalSetSelected(intervalSet: IntervalSet) {
        _uiState.update {
            it.copy(selectedIntervalSet = intervalSet)
        }
    }

    fun addImages(sourceUris: List<Uri>) {
        viewModelScope.launch {
            val existingSourceUris = _uiState.value.draftContentItems
                .filter { it.type == ContentType.IMAGE }
                .map { it.sourceUri }
                .toSet()

            val newItems = mutableListOf<DraftContentItem>()
            for (uri in sourceUris) {
                if (uri.toString() !in existingSourceUris) {
                    val persistentUri = FileUtils.copyFileToInternalStorage(application, uri)
                    if (persistentUri != null) {
                        newItems.add(
                            DraftContentItem(
                                ContentType.IMAGE,
                                persistentUri.toString(),
                                uri.toString()
                            )
                        )
                    }
                }
            }
            _uiState.update { currentState ->
                currentState.copy(draftContentItems = currentState.draftContentItems + newItems)
            }
        }
    }

    fun addImage(sourceUri: Uri) {
        viewModelScope.launch {
            val persistentUri = FileUtils.copyFileToInternalStorage(application, sourceUri)
            if (persistentUri != null) {
                val newItem = DraftContentItem(
                    type = ContentType.IMAGE,
                    persistentUri = persistentUri.toString(),
                    sourceUri = sourceUri.toString()
                )
                _uiState.update { currentState ->
                    currentState.copy(draftContentItems = currentState.draftContentItems + newItem)
                }
            }
        }
    }

    fun addPdf(sourceUri: Uri) {
        val persistentUri = FileUtils.copyFileToInternalStorage(application, sourceUri)
        if (persistentUri != null) {
            val newItem = DraftContentItem(
                type = ContentType.PDF,
                persistentUri = persistentUri.toString(),
                sourceUri = sourceUri.toString()
            )
            _uiState.update { currentState ->
                currentState.copy(draftContentItems = currentState.draftContentItems + newItem)
            }
        }
    }

    fun removeDraftContentItem(itemToRemove: DraftContentItem) {
        _uiState.update { currentState ->
            currentState.copy(
                draftContentItems = currentState.draftContentItems.filter { it != itemToRemove }
            )
        }
    }

    fun saveCard() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState.topic.isBlank()) {
                return@launch
            }

            val intervals = currentState.selectedIntervalSet.intervals
            val firstInterval = intervals.firstOrNull() ?: 1

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, firstInterval)
            val reviewDateForNewCard = calendar.time

            val cardToSave = Card(
                id = currentState.cardId,
                topic = currentState.topic,
                notes = currentState.notes,
                intervals = intervals,
                isFinished = false,
                nextReviewDate = currentState.originalCard?.nextReviewDate ?: reviewDateForNewCard,
                currentInterval = currentState.originalCard?.currentInterval ?: firstInterval
            )

            val contentItemsToSave = currentState.draftContentItems.map { draftItem ->
                ContentItem(
                    cardId = 0,
                    type = draftItem.type,
                    content = draftItem.persistentUri
                )
            }

            if (cardToSave.id == 0) {
                repository.insertCardAndContent(cardToSave, contentItemsToSave)
            } else {
                repository.updateCardAndContent(cardToSave, contentItemsToSave)
            }
        }
    }
}