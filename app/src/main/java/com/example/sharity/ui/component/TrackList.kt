package com.example.sharity.ui.component

import TrackCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sharity.domain.model.Playlist
import com.example.sharity.domain.model.Track
import com.example.sharity.indexerManager

@Composable
fun ReusableTrackList(
    trackList: List<Track>,
    selectedTracks: Set<Track>,
    onTrackClick: (Track) -> Unit,
    onTrackSelect: (Track) -> Unit,
    isSelectionMode: Boolean,
    currentListName: String,
    playlists: List<Playlist> = emptyList(),
    onAddTrackToPlaylist: ((Track, Playlist) -> Unit)? = null,
    onCreateNewPlaylist: (() -> Unit)? = null,
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    paddingValues: PaddingValues = PaddingValues(0.dp), // Add this parameter
    modifier: Modifier = Modifier
) {
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var selectedTrackForPlaylist by remember { mutableStateOf<Track?>(null) }

    PullToRefreshBox(
        isRefreshing = false,
        onRefresh = {
            indexerManager.startIndex()
        },
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues), // Apply the padding here
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Modern Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Gradient icon
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = currentListName,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${trackList.size} ${if (trackList.size == 1) "song" else "songs"}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Search Bar
                    if (onSearchQueryChange != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search songs...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // Track List
            itemsIndexed(
                items = trackList,
                key = { _, track -> track.contentUri }
            ) { index, track ->
                val clickAction = if (isSelectionMode) {
                    { onTrackSelect(track) }
                } else {
                    { onTrackClick(track) }
                }

                val isSelected = selectedTracks.contains(track)

                TrackCard(
                    uri = track.contentUri,
                    title = track.title,
                    artist = track.artist ?: "Unknown Artist",
                    isSelected = isSelected,
                    onClick = clickAction,
                )
            }
        }
    }
}