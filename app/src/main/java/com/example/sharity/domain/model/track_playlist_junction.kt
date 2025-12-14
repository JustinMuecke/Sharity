package com.example.sharity.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["content_uri", "playlist_id"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["playlist_id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Track::class,
            parentColumns = ["content_uri"],
            childColumns = ["content_uri"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TrackPlaylistJunction(
    @ColumnInfo(name = "playlist_id")
    val playlistID: Int,
    @ColumnInfo(name = "content_uri")
    val contentUri: String
)
