package com.example.sharity.ui.feature.playlistselection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.component.playlist.PlaylistCard

@Composable
fun PlaylistSelectionScreen(
    onViewPlaylist: (id: Int) -> Unit, // Handler to navigate to a specific playlist
    modifier: Modifier = Modifier
) {
    // 1. Placeholder ViewModel for user playlists
    val viewModel: PlaylistSelectionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val userPlaylists = viewModel.userPlaylists // In a real app, this would be collectedAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // --- 1. Top Button: All Songs Playlist ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable {
                    // When clicked, navigate to the special 'All Songs' playlist.
                    // We'll use ID 0L as a convention for the 'All Songs' list.
                    onViewPlaylist(0)
                },
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // You can add an Icon here for 'All Songs'
                Text(
                    text = "All Songs",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                // Optionally show total track count here
                // Text(text = "999 Tracks", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. Title for User Playlists ---
        Text(
            text = "Your Playlists",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- 3. LazyColumn for Custom Playlists ---
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
             items(userPlaylists) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    onClick = { onViewPlaylist(playlist.playlistID )}
                )
            }
        }
    }
}

