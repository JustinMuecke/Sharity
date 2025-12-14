package com.example.sharity.domain.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlaylistWithTracks(
    @Embedded val playlist: Playlist,

    @Relation(
        parentColumn = "playlist_id",
        entityColumn = "content_uri",
        associateBy = Junction(
            value = TrackPlaylistJunction::class,
            parentColumn = "playlist_id",  // Column in junction that references Playlist
            entityColumn = "content_uri"    // Column in junction that references Track
        )
    )
    val tracks: List<Track>
)