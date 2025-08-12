package com.example.remember.data.di

import android.content.Context
import androidx.room.Room
import com.example.remember.data.db.AppDatabase
import com.example.remember.data.db.CardDao
import com.example.remember.data.db.SettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "remember_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCardDao(appDatabase: AppDatabase): CardDao {
        return appDatabase.cardDao()
    }

    @Provides
    fun provideSettingsDao(appDatabase: AppDatabase): SettingsDao {
        return appDatabase.settingsDao()
    }
}