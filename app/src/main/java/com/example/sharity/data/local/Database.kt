package com.example.sharity.data.local

import android.content.Context
import androidx.room.Room

object Database {
    fun createDatabaseConnector(applicationContext: Context): AppDatabase {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "database-1"
        ).build()

        return db
    }
}