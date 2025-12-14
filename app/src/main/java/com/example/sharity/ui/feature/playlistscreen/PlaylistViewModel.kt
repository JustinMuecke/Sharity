package com.example.sharity.ui.feature.playlistscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.domain.model.Playlist
import com.example.sharity.domain.model.Track
import com.example.sharity.domain.usecase.PlaylistDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlaylistDetails(
    val playlist: Playlist? = null,
    val songs: List<Track> = emptyList(),
    val isLoading: Boolean = true
)

class PlaylistViewModel(
    db: AppDatabase,
    private val playlistId: Int
) : ViewModel() {

    // IMPORTANT: Initialize playlistDao FIRST, before using it in init block
    private val playlistDao: PlaylistDao = db.playlistDao()

    val currentPlaylistTracks: List<Track>
        get() = uiState.value.songs

    init {
        viewModelScope.launch {
            playlistDao.getPlaylistWithTracks(playlistId).collect { result ->
                Log.e("PlaylistViewModel", "Playlist ID: $playlistId")
                Log.e("PlaylistViewModel", "Result: $result")
                Log.e("PlaylistViewModel", "Playlist: ${result?.playlist}")
                Log.e("PlaylistViewModel", "Tracks count: ${result?.tracks?.size}")
            }
        }
    }

    val uiState: StateFlow<PlaylistDetails> = playlistDao.getPlaylistWithTracks(playlistId)
        .map { playlistWithTracks ->
            PlaylistDetails(
                playlist = playlistWithTracks?.playlist,
                songs = playlistWithTracks?.tracks ?: emptyList(),
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlaylistDetails(isLoading = true)
        )
}