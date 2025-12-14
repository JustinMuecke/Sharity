package com.example.sharity.ui.component.navBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NfcIsland(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Share",
) {
    // Solid, vibrant color to draw attention for NFC sharing
    val backgroundColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = modifier
            .height(32.dp)
            .widthIn(min = 100.dp, max = 160.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 0.dp,      // Connected to top
                    topEnd = 0.dp,        // Connected to top
                    bottomStart = 16.dp,  // Sleeker rounded bottom
                    bottomEnd = 16.dp     // Sleeker rounded bottom
                )
            )
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Nfc,
                contentDescription = "Share via NFC",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}