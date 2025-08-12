package com.example.remember.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cards")

data class Card (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val topic: String,
    val notes: String,
    val intervals: List<Int>,
    val isFinished: Boolean = false,
    val nextReviewDate: Date,
    val currentInterval: Int = 1
)