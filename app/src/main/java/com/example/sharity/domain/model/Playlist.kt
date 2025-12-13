package com.example.sharity.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(true) val playlistID: Int = 0,
    @ColumnInfo(name = "playlist_name") val playlistName: String,
)
