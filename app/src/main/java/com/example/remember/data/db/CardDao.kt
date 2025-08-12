package com.example.remember.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CardDao {
    @Transaction
    @Query("SELECT * FROM cards WHERE nextReviewDate <= :today")
    fun getDueCards(today: Date): Flow<List<CardWithContent>>

    @Transaction
    @Query("SELECT * FROM cards ORDER BY nextReviewDate ASC")
    fun getAllCards(): Flow<List<CardWithContent>>

    @Transaction
    @Query("SELECT * FROM cards WHERE id = :cardId")
    fun getCardWithContentById(cardId: Int): Flow<CardWithContent?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentItems(items: List<ContentItem>)

    @Update
    suspend fun updateCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)

    @Query("DELETE FROM content_items WHERE cardId = :cardId")
    suspend fun deleteContentItemsForCard(cardId: Int)

    @Transaction
    suspend fun updateCardAndContent(card: Card, items: List<ContentItem>) {
        deleteContentItemsForCard(card.id)
        if (items.isNotEmpty()) {
            val itemsWithCardId = items.map { it.copy(cardId = card.id) }
            insertContentItems(itemsWithCardId)
        }
        updateCard(card)
    }
}