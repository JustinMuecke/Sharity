package com.example.sharity.ui.feature.playlistselection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharity.domain.model.Playlist // Assuming this model exists

// Custom Composable for the New Playlist button
@Composable
fun NewPlaylistButton(onCreateNewPlaylist: () -> Unit) {
    OutlinedCard(
        onClick = onCreateNewPlaylist,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(vertical = 4.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "New Playlist",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create New Playlist",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun PlaylistSelectionScreen(
    paddingValues: PaddingValues, // <-- Accept parent Scaffold padding
    onViewPlaylist: (id: Int) -> Unit,
    onCreateNewPlaylist: () -> Unit, // <-- New handler for creation
    modifier: Modifier = Modifier,
    viewModel: PlaylistSelectionViewModel = viewModel() // Inject ViewModel here
) {
    // 1. Correctly collect the StateFlow from the ViewModel
    val userPlaylists by viewModel.userPlaylists.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues) // Apply parent padding here
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        // --- Header Section ---
        item {
            Text(
                text = "Your Playlists",
                style = MaterialTheme.typography.headlineMedium, // More prominent title
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // --- Action Button ---
        item {
            NewPlaylistButton(onCreateNewPlaylist = onCreateNewPlaylist)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Playlists List ---
        if (userPlaylists.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You don't have any playlists yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Using modern Material3 ListItem for list presentation
            items(userPlaylists) { playlist ->
                PlaylistItem(
                    playlist = playlist,
                    onClick = { onViewPlaylist(playlist.playlistID) }
                )
            }
        }
    }
}

// A custom composable to replace the generic PlaylistCard for modern styling
@Composable
fun PlaylistItem(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = playlist.playlistName, // Assuming 'name' field exists
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                Text(
                  //  text = "${playlist.songCount} songs", // Assuming a songCount field exists
                    text = "Some Songs for sure :)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }
}