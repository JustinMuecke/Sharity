package com.example.sharity.domain.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlaylistWithTracks(
    @Embedded val playlist: Playlist,

    @Relation(
        parentColumn = "playlistID",
        entityColumn = "content_uri",
        associateBy = Junction(TrackPlaylistJunction::class)
    )

    val tracks: List<Track>
)