package com.example.sharity.ui.feature.allsongsscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.sharity.ui.component.ReusableTrackList

@Composable
fun AllSongsView(
    viewModel: AllSongsViewModel,
    paddingValues: PaddingValues, // Add this parameter
    modifier: Modifier = Modifier,
) {
    val searchQuery by viewModel.currentQuery.collectAsState()
    val tracks by viewModel.filteredTracks.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ReusableTrackList(
            trackList = tracks,
            selectedTracks = viewModel.selectedTracks.collectAsState().value,
            onTrackClick = { track -> viewModel.selectTrack(track) },
            onTrackSelect = { track -> viewModel.toggleSelect(track) },
            isSelectionMode = viewModel.isSelectionMode.collectAsState().value,
            currentListName = viewModel.currentListName.collectAsState().value,
            searchQuery = searchQuery,
            onSearchQueryChange = { newQuery ->
                viewModel.onSearchQueryChange(newQuery)
            },
            paddingValues = paddingValues, // Pass it here
            modifier = Modifier.fillMaxSize()
        )
    }
}