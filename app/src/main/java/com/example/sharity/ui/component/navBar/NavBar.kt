package com.example.sharity.ui.component.navBar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.component.share.PeerMiniProfileOverlay
import com.example.sharity.ui.component.share.PeerSummary

@Composable
fun NavBar(
    showBack: Boolean,
    onBackClick: () -> Unit,
    onNfcClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenPeer: () -> Unit,
) {
    val showPeerOverlay = remember { mutableStateOf(false) }

    val peer = remember {
        PeerSummary(
            displayName = "Nearby User",
            songs = 128,
            sent = 12,
            received = 9
        )
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showBack) {
                    BackButton(onClick = onBackClick)
                }
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                NfcIsland(onClick = onNfcClick)
            }

            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                ProfileButton(onClick = onProfileClick)
            }
        }
        PeerMiniProfileOverlay(
            visible = showPeerOverlay.value,
            peer = peer,
            onDismiss = { showPeerOverlay.value = false },
            onOpenPeer = {
                showPeerOverlay.value = false
                onOpenPeer()
            }
        )
    }
}
