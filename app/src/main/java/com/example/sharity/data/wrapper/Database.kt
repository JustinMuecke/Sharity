package com.example.sharity.data.wrapper

import android.content.Context
import androidx.room.Room
import com.example.sharity.data.local.AppDatabase

object Database {
    fun createDatabaseConnector(applicationContext: Context): AppDatabase {
        return Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "sharity-database"
        ).fallbackToDestructiveMigration(true).build()
    }
}