package com.example.sharity.ui.feature.homescreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sharity.ui.component.AudioControl
import com.example.sharity.ui.component.HomeTopBar
import com.example.sharity.ui.component.SearchBar
import com.example.sharity.ui.component.TagSelection
import com.example.sharity.ui.component.TrackList

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.component.navBar.NavBar
import com.example.sharity.ui.component.share.PeerMiniProfileOverlay
import com.example.sharity.ui.component.share.PeerSummary

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit,
    onOpenPeer: () -> Unit
) {
    val showPeerOverlay = remember { mutableStateOf(false) }

    // demo peer; later from NFC/Bluetooth discovery
    val peer = remember {
        PeerSummary(
            displayName = "Nearby User",
            songs = 128,
            sent = 12,
            received = 9
        )
    }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavBar(
            showBack = false,
            onBackClick = {},
            onNfcClick = { showPeerOverlay.value = true },
            onProfileClick = onProfileClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp)
        ) {


            TagSelection()
            SearchBar(viewModel)

            TrackList(
                viewModel = viewModel,
                modifier = Modifier.weight(1f)
            )
            AudioControl(viewModel)
        }

        if (showPeerOverlay.value) {
            PeerMiniProfileOverlay(
                peer = peer,
                onDismiss = { showPeerOverlay.value = false },
                onOpenPeer = {
                    showPeerOverlay.value = false
                    onOpenPeer()
                }
            )
        }
        HomeTopBar(onProfileClick = onProfileClick)
        TrackList(
            viewModel = viewModel,
            modifier = Modifier.weight(1f)
        )
        AudioControl(viewModel)


    }


}

//fun Modifier.Companion.align(topCenter: Alignment) {}

//fun Modifier.Companion.weight(f: Float): Modifier {
//    return TODO("Provide the return value")
//}



