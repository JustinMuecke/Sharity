package com.example.sharity.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.Room.databaseBuilder

object Database {
    fun createDatabaseConnector(applicationContext: Context): TrackDao {
        return databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "Sharity database"
        ).build().trackDao()
    }
}