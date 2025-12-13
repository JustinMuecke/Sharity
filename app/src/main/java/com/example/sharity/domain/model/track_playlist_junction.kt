package com.example.sharity.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["trackID", "playlistID",],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["playlistID"],
            childColumns = ["playlistID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Track::class,
            parentColumns = ["trackID"],
            childColumns = ["trackID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TrackPlaylistJunction(
    val playlistID: Int,
    val trackID: Int
)
