package com.example.sharity.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.feature.homescreen.HomeScreenViewModel

@Composable
fun TrackList(viewModel : HomeScreenViewModel, modifier : Modifier){
    val trackList by viewModel.tracks.collectAsState()
    val selectedTrack by viewModel.currentTrack.collectAsState()
    LazyColumn (
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        items(trackList) { trackName ->
            TrackCard(
                title = trackName.title,
                album = trackName.album?: "",
                artist = trackName.artist?: "",
                isSelected = (trackName.title == selectedTrack),
                onClick = { viewModel.selectTrack(trackName.title) }
            )
        }
    }
}