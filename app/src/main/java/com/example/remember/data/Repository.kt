package com.example.remember.data

import com.example.remember.data.db.Card
import com.example.remember.data.db.CardDao
import com.example.remember.data.db.CardWithContent
import com.example.remember.data.db.ContentItem
import com.example.remember.data.db.Settings
import com.example.remember.data.db.SettingsDao
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val cardDao: CardDao,
    private val settingsDao: SettingsDao
) {

    // cards
    fun getDueCards(): Flow<List<CardWithContent>> {
        return cardDao.getDueCards(Date())
    }

    fun getAllCards(): Flow<List<CardWithContent>> {
        return cardDao.getAllCards()
    }

    fun getCardById(cardId: Int): Flow<CardWithContent?> {
        return cardDao.getCardWithContentById(cardId)
    }

    suspend fun insertCardAndContent(card: Card, items: List<ContentItem>) {
        val cardId = cardDao.insertCard(card)
        val itemsWithCardId = items.map { it.copy(cardId = cardId.toInt()) }
        cardDao.insertContentItems(itemsWithCardId)
    }

    suspend fun updateCard(card: Card) {
        cardDao.updateCard(card)
    }

    suspend fun deleteCard(card: Card) {
        cardDao.deleteCard(card)
    }

    suspend fun updateCardAndContent(card: Card, items: List<ContentItem>) {
        cardDao.updateCardAndContent(card, items)
    }

    // settings
    fun getSettings(): Flow<Settings?> {
        return settingsDao.getSettings()
    }

    suspend fun saveSettings(settings: Settings) {
        settingsDao.saveSettings(settings)
    }
}