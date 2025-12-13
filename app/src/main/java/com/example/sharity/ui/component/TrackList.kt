package com.example.sharity.ui.component

import TrackCard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.feature.playlistscreen.PlaylistViewModel

@Composable
fun TrackList(viewModel: PlaylistViewModel, modifier: Modifier = Modifier){

    val trackList by viewModel.filteredTracks.collectAsState()
    val selectedTrack by viewModel.currentTrack.collectAsState()
    val currentListName by viewModel.currentListName.collectAsState()
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = currentListName,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        // 1. Add the SearchBar component above the LazyColumn
        SearchBar(viewModel = viewModel)

        LazyColumn(
            // The padding is now applied to the OutlinedTextField in SearchBar,
            // so you might want to adjust the padding here or use a Column for all of them.
            modifier = modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // The items() function now uses the filtered trackList
            items(trackList) { track ->
                TrackCard(
                    uri = track.contentUri,
                    title = track.title,
                    artist = track.artist ?: "",
                    isSelected = (track.contentUri == selectedTrack),
                    onClick = {
                        viewModel.selectTrack(track)
                    }
                )
            }
        }
    }
}
