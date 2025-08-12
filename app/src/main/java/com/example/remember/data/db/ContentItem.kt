package com.example.remember.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class ContentType {
    TEXT, IMAGE, PDF
}

@Entity(
    tableName = "content_items",
    foreignKeys = [
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContentItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cardId: Int,
    val type: ContentType,
    val content: String
)