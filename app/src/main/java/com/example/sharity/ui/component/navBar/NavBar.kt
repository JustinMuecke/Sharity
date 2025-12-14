package com.example.sharity.ui.component.navBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.component.share.PeerMiniProfileOverlay
import com.example.sharity.ui.component.share.PeerSummary


// todo: startDestination = RootDestinations.LANDING
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
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Back Button (with animation)
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                this@Row.AnimatedVisibility(
                    visible = showBack,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    ModernBackButton(onClick = onBackClick)
                }
            }

            // Center: NFC Island
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                NfcIsland(onClick = onNfcClick)
            }

            // Right: Profile Button
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                ModernProfileButton(onClick = onProfileClick)
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

@Composable
private fun ModernBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ModernProfileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            modifier = Modifier.size(24.dp)
        )
    }
}