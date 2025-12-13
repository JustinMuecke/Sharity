package com.example.sharity.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import com.example.sharity.domain.usecase.TrackDao

object Database {
    fun createDatabaseConnector(applicationContext: Context): AppDatabase {
        return databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "Sharity database"
        ).fallbackToDestructiveMigration(true).build()
    }
}