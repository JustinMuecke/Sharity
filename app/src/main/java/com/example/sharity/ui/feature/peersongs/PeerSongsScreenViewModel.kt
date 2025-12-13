package com.example.sharity.ui.feature.peersongs

import androidx.lifecycle.ViewModel
import com.example.sharity.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PeerSongsUiState(
    val peerName: String = "Nearby User",
    val tracks: List<Track> = emptyList(),
    val selectedTrackUris: Set<String> = emptySet()
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

    private fun placeholderTracks(): List<Track> {
        return listOf(
            Track(
                contentUri = "peer://track/1",
                title = "Midnight Echoes",
                artist = "Demo Artist",
                duration = 180_000L,
                releaseYear = 2023
            ),
            Track(
                contentUri = "peer://track/2",
                title = "Lavender Lights",
                artist = "Demo Artist",
                duration = 210_000L,
                releaseYear = 2022
            ),
            Track(
                contentUri = "peer://track/3",
                title = "Neon Strings",
                artist = "Demo Artist",
                duration = 195_000L,
                releaseYear = 2021
            ),
            Track(
                contentUri = "peer://track/4",
                title = "Bassline Boulevard",
                artist = "Demo Artist",
                duration = 225_000L,
                releaseYear = 2020
            )
        )
    }
}
