package com.example.sharity.ui.feature.playlistscreen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PlaylistView(playlistId : Int){
    Text(
        text= "Displaying Playlist $playlistId"
    )
}