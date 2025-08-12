package com.example.remember.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class CardWithContent(
    @Embedded val card: Card,
    @Relation(
        parentColumn = "id",
        entityColumn = "cardId"
    )
    val contentItems: List<ContentItem>
)