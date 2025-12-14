package com.example.sharity.ui.feature.allsongsscreen

import ReusableTrackList
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun AllSongsView(
    viewModel: AllSongsViewModel,
    modifier: Modifier = Modifier,

) {
    val searchQuery by viewModel.currentQuery.collectAsState()
    val tracks by viewModel.filteredTracks.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Pass the new hook down to the TrackList
            ReusableTrackList(
                trackList = tracks,
                selectedTracks = viewModel.selectedTracks.collectAsState().value, // Assumed state
                onTrackClick = { track -> viewModel.selectTrack(track) }, // Playback
                onTrackSelect = { track -> viewModel.toggleSelect(track) }, // Selection
                isSelectionMode = viewModel.isSelectionMode.collectAsState().value, // Selection Mode
                currentListName = viewModel.currentListName.collectAsState().value, // "All Songs"
                modifier = Modifier.fillMaxSize(),
                searchQuery = searchQuery,
                onSearchQueryChange = { newQuery ->
                    viewModel.onSearchQueryChange(newQuery)
                }
            )
        }
    }
}

