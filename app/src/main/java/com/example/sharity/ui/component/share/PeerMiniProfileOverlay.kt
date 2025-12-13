package com.example.sharity.ui.component.share

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.sharity.ui.theme.DarkBlackberry
import com.example.sharity.ui.theme.GrapeGlimmer

data class PeerSummary(
    val displayName: String,
    val songs: Int,
    val sent: Int,
    val received: Int
)

@Composable
fun PeerMiniProfileOverlay(
    peer: PeerSummary,
    onDismiss: () -> Unit,
    onOpenPeer: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // dim background behind the card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color.Transparent)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPeer() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBlackberry),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = peer.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatPill(label = "Songs", value = peer.songs.toString())
                        StatPill(label = "Sent", value = peer.sent.toString())
                        StatPill(label = "Received", value = peer.received.toString())
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tap to view songs",
                        style = MaterialTheme.typography.labelMedium,
                        color = GrapeGlimmer
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String
) {
    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Black
        )
    }
}
