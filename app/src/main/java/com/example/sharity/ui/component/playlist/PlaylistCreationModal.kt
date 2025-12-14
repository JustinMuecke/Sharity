package com.example.sharity.ui.component.playlist


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharity.domain.model.Track // Assuming you have a Song model
import com.example.sharity.ui.feature.allsongsscreen.AllSongsViewModel // Assuming this ViewModel exists

// Helper Composable to show a selectable song item
@Composable
fun SelectableSongItem(
    song: Track,
    isSelected: Boolean,
    onToggleSelection: (Track) -> Unit
) {
    // You can use a ListItem inside a Card for better visual feedback
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelection(song) }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        ListItem(
            headlineContent = { Text(song.title, style = MaterialTheme.typography.titleMedium) }, // Assuming 'title' field
            supportingContent = { Text(song.artist?: "", style = MaterialTheme.typography.bodySmall) }, // Assuming 'artist' field
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.LibraryMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            trailingContent = {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        )
    }
}


@Composable
fun SongSelectorModalContent(
    allSongsViewModel: AllSongsViewModel,
    onClose: () -> Unit,
    onPlaylistCreated: (name : String, tracks: List<Track>) -> Unit // Action with the final list of selected songs
) {
    // Collect the list of ALL songs
    val allSongs by allSongsViewModel.tracks.collectAsState(initial = emptyList()) // Assuming 'allSongs' StateFlow

    // State to hold the currently selected songs
    val selectedSongs = remember { mutableStateListOf<Track>() }
    var playlistName by rememberSaveable { mutableStateOf("") } // Use rememberSaveable for modal
    // Toggle function
    val onToggleSelection: (Track) -> Unit = { song ->
        if (selectedSongs.contains(song)) {
            selectedSongs.remove(song)
        } else {
            selectedSongs.add(song)
        }
    }

    // Modal UI
    // Modal UI
    Column(modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 600.dp)) {

        // --- Modal Header ---
        ModalHeader(
            selectedCount = selectedSongs.size,
            playlistName = playlistName, // Pass name state to the header
            onClose = onClose,
            // Pass the name and tracks to the handler
            onCreate = { onPlaylistCreated(playlistName, selectedSongs) }
        )

        Divider()

        // --- Name Input Field (NEW) ---
        OutlinedTextField(
            value = playlistName,
            onValueChange = { playlistName = it },
            label = { Text("Playlist Name") },
            placeholder = { Text("e.g., Chill Beats") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Divider() // Divider after the input field

        // --- Song List ---
        LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)) {
            items(allSongs) { song ->
                SelectableSongItem(
                    song = song,
                    isSelected = selectedSongs.contains(song),
                    onToggleSelection = onToggleSelection
                )
            }
        }
    }
}

// Simple Header with actions
@Composable
fun ModalHeader(selectedCount: Int, playlistName: String, onClose: () -> Unit, onCreate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Close")
        }
        Text(
            text = "Selected: $selectedCount",
            style = MaterialTheme.typography.titleLarge
        )
        Button(
            // Button enabled ONLY if tracks are selected AND a name is entered
            onClick = onCreate,
            enabled = selectedCount > 0 && playlistName.isNotBlank()
        ) {
            Text("Create")
        }
    }
}