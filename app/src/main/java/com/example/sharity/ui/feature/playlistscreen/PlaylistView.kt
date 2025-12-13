package com.example.sharity.ui.feature.playlistscreen


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sharity.ui.component.AudioControl
import com.example.sharity.ui.component.TrackList
import com.example.sharity.ui.component.navBar.NavBar

@Composable
fun PlaylistView(
    viewModel: PlaylistViewModel,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TrackList(
                viewModel = viewModel,
                modifier = Modifier.weight(2f)
            )
        }

    }



}

