package com.example.sharity.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.Room.databaseBuilder

object Database {
    fun createDatabaseConnector(applicationContext: Context): TrackDao {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "database-1"
        ).build()

        return db.trackDao();
    }
}