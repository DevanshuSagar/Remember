package com.example.remember.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Theme {
    LIGHT, DARK, SYSTEM
}

@Entity(tableName = "settings")
data class Settings(
    val notificationTimeMinutes: Int = 1260, // 9:00 PM
//    val globalIntervals: String = "1,3,7,21",
    val theme: Theme = Theme.SYSTEM,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
