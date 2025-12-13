package com.example.sharity.data.local

import android.content.Context
import androidx.room.Room

object Database {
    fun createDatabaseConnector(applicationContext: Context): AppDatabase {
        return Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "sharity-database"
        ).fallbackToDestructiveMigration(true).build()
    }
}