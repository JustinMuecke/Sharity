package com.example.sharity.ui.feature.peersongs

import androidx.lifecycle.ViewModel
import androidx.room.util.copy
import com.example.sharity.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow



data class Track(
    val contentUri: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val releaseYear: Int?
)

class PeerSongsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PeerSongsUiState())
    val uiState: StateFlow<PeerSongsUiState> = _uiState.asStateFlow()

    init {
        loadPlaceholderData()
    }

    fun loadPlaceholderData() {
        _uiState.value = _uiState.value.copy(
            peerName = "Nearby User",
            tracks = placeholderTracks(),
            selectedTrackUris = emptySet()
        )
    }

    fun toggleSelect(track: Track) {
        val current = _uiState.value
        val uri = track.contentUri

        val nextSelected = if (current.selectedTrackUris.contains(uri)) {
            current.selectedTrackUris - uri
        } else {
            current.selectedTrackUris + uri
        }

        _uiState.value = current.copy(selectedTrackUris = nextSelected)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedTrackUris = emptySet())
    }

    fun getSelectedTracks(): List<Track> {
        val current = _uiState.value
        val selected = current.selectedTrackUris
        return current.tracks.filter { selected.contains(it.contentUri) }
    }

    //TODO
    private fun placeholderTracks(): List<Track> {
        return listOf(
            Track(
                contentUri = "peer://track/1",
                title = "Midnight Echoes",
                artist = "Demo Artist",
                album = null,
                duration = 180_000L,      // 3 minutes
                releaseYear = 2023
            ),
            Track(
                contentUri = "peer://track/2",
                title = "Lavender Lights",
                artist = "Demo Artist",
                album = null,
                duration = 210_000L,
                releaseYear = 2022
            ),
            Track(
                contentUri = "peer://track/3",
                title = "Neon Strings",
                artist = "Demo Artist",
                album = null,
                duration = 195_000L,
                releaseYear = 2021
            ),
            Track(
                contentUri = "peer://track/4",
                title = "Bassline Boulevard",
                artist = "Demo Artist",
                album = null,
                duration = 225_000L,
                releaseYear = 2020
            )
        )
    }
}

annotation class PeerSongsUiState
