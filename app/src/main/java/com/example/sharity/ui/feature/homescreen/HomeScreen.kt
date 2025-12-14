package com.example.sharity.ui.feature.homescreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.domain.model.Connection
import com.example.sharity.ui.component.SearchBar
import com.example.sharity.ui.component.homescreen.LastConnectedPeersSection
import com.example.sharity.ui.feature.allsongsscreen.AllSongsViewModel

// A reusable, modern card component for the dashboard items (kept from previous answer)
@Composable
fun HomeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isWide: Boolean = true // Flag to control card size
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .height(if (isWide) 100.dp else 120.dp)
            .padding(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}


@Composable
fun HomeScreen(
    // 1. **Crucial Change:** Accept PaddingValues from the parent Scaffold
    paddingValues: PaddingValues,
    modifier: Modifier,
    db: AppDatabase,
    exoPlayer: ExoPlayer,
    onProfileClick: () -> Unit,
    allSongsViewModel: AllSongsViewModel,
    onHistoryClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onPlaylistsClick: () -> Unit,
    onListClick: () -> Unit,
    onOpenPeer: () -> Unit,
    onAllsongsClick: () -> Unit,
) {
    // 2. Apply the parent's padding to the main container
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues) // <-- Apply the PaddingValues here
            .padding(horizontal = 8.dp), // Additional horizontal padding for the cards
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        // --- Custom Top Section: Search and Profile Greeting ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp) // Adjust to balance with card padding
            ) {
                // Profile Greeting
                Text(
                    text = "Hello, User!",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .clickable(onClick = onProfileClick)
                        .padding(top = 16.dp, bottom = 8.dp)
                )
            }
        }

        // --- Content Cards: Dashboard Items ---

        item {
            // First Row: History and Friends (50/50 split)
            Row(modifier = Modifier.fillMaxWidth()) {
                HomeCard(
                    title = "History",
                    icon = Icons.Filled.History,
                    onClick = onHistoryClick,
                    modifier = Modifier.weight(1f),
                    isWide = false
                )
                HomeCard(
                    title = "Friends <3",
                    icon = Icons.Filled.People,
                    onClick = onFriendsClick,
                    modifier = Modifier.weight(1f),
                    isWide = false
                )
            }
        }

        item {
            // Playlists (Full Width)
            HomeCard(
                title = "Playlists",
                icon = Icons.Filled.QueueMusic,
                onClick = onPlaylistsClick,
                modifier = Modifier.fillMaxWidth(),
                isWide = true
            )
        }

        item {
            // All Songs (Full Width)
            HomeCard(
                title = "All Songs",
                icon = Icons.Filled.LibraryMusic,
                onClick = onAllsongsClick,
                modifier = Modifier.fillMaxWidth(),
                isWide = true
            )
        }

        item {
            val sampleLastPeers = listOf(
                Connection(3, "Chris's Laptop", "Offline", 200, 180),
                Connection(4, "Dana's Watch", "Current Track", 20, tracksReceived = 0),
                Connection(5, "Eve's Phone", "Offline", 15, tracksReceived = 2000)
            )
            LastConnectedPeersSection(
                lastPeers = sampleLastPeers,
                onPeerClick = { peer -> onOpenPeer() /* or a specific peer-profile-nav */ }
            )
        }

        // Add more content items here if needed...
    }
}