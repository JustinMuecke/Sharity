package com.example.sharity.ui.feature.homescreen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sharity.ui.component.AudioControl
import com.example.sharity.ui.component.SearchBar
import com.example.sharity.ui.component.TrackList

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.component.navBar.NavBar
import com.example.sharity.ui.component.share.PeerMiniProfileOverlay
import com.example.sharity.ui.component.share.PeerSummary

import androidx.media3.exoplayer.ExoPlayer
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.ui.feature.allsongsscreen.AllSongsView
import com.example.sharity.ui.feature.allsongsscreen.AllSongsViewModel


/*


*/

@Composable
fun HomeScreen(modifier: Modifier,
               db : AppDatabase,
               exoPlayer : ExoPlayer,
               onProfileClick: () -> Unit,
               allSongsViewModel: AllSongsViewModel,
               onHistoryClick: () -> Unit,
               onFriendsClick: () -> Unit,
               onPlaylistsClick: () -> Unit,
               onListClick: () -> Unit,
               onOpenPeer: () -> Unit,
               onAllsongsClick: () -> Unit,
){
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 72.dp)
            ) {



        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp) // Add padding to the column content
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround // Space out the two boxes
            ) {
                // History Box
                Box(
                    modifier = Modifier
                        .weight(1f) // Give it weight so it takes up space
                        .clickable(onClick = onHistoryClick) // ⬅️ ADD CLICKABLE
                        .padding(8.dp) // Inner padding
                ) {
                    Text(
                        text = "History"
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onFriendsClick) // ⬅️ ADD CLICKABLE
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Friends <3"
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Playlists Box
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onPlaylistsClick) // ⬅️ ADD CLICKABLE
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Playlists"
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Column {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onAllsongsClick) // ⬅️ ADD CLICKABLE
                        .padding(8.dp)
                ) {
                    Text(
                        text = "All Songs"
                    )
                }
            }
        }
    }
}
}
