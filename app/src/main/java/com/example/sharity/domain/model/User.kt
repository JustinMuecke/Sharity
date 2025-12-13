package com.example.sharity.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userID: Int,
    @ColumnInfo(name = "name") val username: String,
    @ColumnInfo(name = "tracks_sent") val tracksSent: Int,
    @ColumnInfo(name = "tracks_received") val tracksReceived: Int
)