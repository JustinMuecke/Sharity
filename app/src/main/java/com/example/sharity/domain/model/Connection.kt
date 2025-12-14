package com.example.sharity.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connections")
data class Connection(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "connection_id")
    val connectionID: Int = 0,

    @ColumnInfo(name = "name") val username: String,
    @ColumnInfo(name = "connection_uuid") val connectionUuid: String?,
    @ColumnInfo(name = "tracks_sent") val tracksSent: Int = 0,
    @ColumnInfo(name = "tracks_received") val tracksReceived: Int = 0
)