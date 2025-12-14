import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackCard(
    uri: String,
    title: String,
    artist: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    ) {
    var showMenu by remember { mutableStateOf(false) }

    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant
    val iconColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.secondary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Album Art Placeholder
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Song Icon",
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Song Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artist.ifBlank { "Unknown Artist" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Playing Indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Playing",
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
/*
        // Context Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Add to Playlist") },
                onClick = {
                    showMenu = false
                    onAddToPlaylist()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = null
                    )
                }
            )
            // You can add more menu items here
            // DropdownMenuItem(text = { Text("Share") }, onClick = { ... })
            // DropdownMenuItem(text = { Text("Delete") }, onClick = { ... })
        }
        */

    }


}