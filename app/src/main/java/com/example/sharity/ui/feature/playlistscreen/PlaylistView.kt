package com.example.sharity.ui.feature.playlistscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharity.domain.model.Track

@OptIn(ExperimentalMaterial3Api::class)
// ui.feature.playlistscreen/PlaylistView.kt (The FINAL clean version)

@Composable
fun PlaylistView(
    playlistId: Int,
    onSongClick: (Track) -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // The root element uses a Box to manage the full screen space
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues) // <-- CRUCIAL: Applies the top/bottom offsets
    ) {

        // The scrollable content (LazyColumn)
        if (uiState.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else if (uiState.playlist == null) {
            Text("Playlist not found.", Modifier.align(Alignment.Center))
        } else {
            // LazyColumn holds the title and the list of songs
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // contentPadding is ONLY for internal spacing, not for the top/bottom bar offsets.
                // Since paddingValues is applied to the Box, the content starts correctly.
                contentPadding = PaddingValues(bottom = 16.dp) // Add padding for scrolling to the bottom
            ) {
                // --- 1. Title/Details Header ---
                item {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = uiState.playlist!!.playlistName,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.songs.size} songs",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider(Modifier.padding(vertical = 16.dp))
                    }
                }

                // --- 2. Song List ---
                items(uiState.songs) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song) },
                    )
                    Divider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                }
            }
        }
    }
}
// Placeholder for SongListItem to prevent compilation issues
@Composable
fun SongListItem(song: Track, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        headlineContent = { Text(song.title) },
        supportingContent = { Text(song.artist?: "") },
        modifier = modifier.clickable(onClick = onClick)
    )
}