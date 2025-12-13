package com.example.sharity.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["content_uri", "playlistID",],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["playlistID"],
            childColumns = ["playlistID"],
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
    val playlistID: Int,
    val content_uri: String
)
