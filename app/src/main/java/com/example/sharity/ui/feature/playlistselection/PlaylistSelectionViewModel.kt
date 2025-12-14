package com.example.sharity.ui.feature.playlistselection

import androidx.lifecycle.ViewModel
import com.example.sharity.domain.model.Playlist

class PlaylistSelectionViewModel : androidx.lifecycle.ViewModel() {
    val userPlaylists = listOf(
        Playlist(1, "Road Trip Jams"),
        Playlist(2, "Workout Mix"),
        Playlist(3, "Chillin' Vibes")
    )
}