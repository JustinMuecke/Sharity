package com.example.sharity.ui.component

import android.media.session.MediaSession
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.feature.homescreen.HomeScreenViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.ui.graphics.Color
import com.example.sharity.ui.feature.homescreen.RepeatMode



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioControl(viewModel : HomeScreenViewModel){
    val isPlaying by viewModel.isPlaying.collectAsState()
    val nextTracks by viewModel.nextTracks.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val trackList by viewModel.tracks.collectAsState()
    val currentPos by viewModel.currentPosition.collectAsState()
    val duration by viewModel.trackDuration.collectAsState()
    var showQueue by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState() // Assumed to be an enum (None, All, One)
    // =========================================================================

    // 2. Local state to handle dragging smoothly
    var isDragging by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Center everything
    ) {
        // 1. Track Title
        Text(
            text = currentTrack ?: "Choose a track",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = if (isDragging) sliderValue else currentPos.toFloat(),
                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                onValueChange = { newValue ->
                    isDragging = true
                    sliderValue = newValue
                },
                onValueChangeFinished = {
                    viewModel.seekTo(sliderValue.toLong())
                    isDragging = false
                },
                // ... Slider content remains the same ...
                thumb = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(12.dp)
                    ) { }
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(4.dp),
                        thumbTrackGapSize = 0.dp,
                        drawStopIndicator = null,
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                },
                modifier = Modifier.height(20.dp)
            )

            // Time Labels (Made smaller and closer to slider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .offset(y = (-6).dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(if (isDragging) sliderValue.toLong() else currentPos),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 2. The Buttons Row (Now using SpaceBetween to spread controls)
        Row(
            modifier = Modifier.fillMaxWidth(), // <--- Fill width
            verticalAlignment = Alignment.CenterVertically,        ) {

            // =========================================================================
            // NEW: Shuffle and Repeat buttons on the left
            // =========================================================================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f), // Takes up 1/3 of the main Row's width
            ) {
                // 1. Shuffle Button
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)                     )
                }

                // 2. Repeat Button
                IconButton(onClick = { viewModel.toggleRepeat() }) {
                    val icon = when (repeatMode) {
                        RepeatMode.One -> Icons.Default.RepeatOne
                        RepeatMode.All -> Icons.Default.Repeat
                        RepeatMode.None -> Icons.Default.Repeat
                    }
                    val tintColor = if (repeatMode != RepeatMode.None)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

                    Icon(
                        imageVector = icon,
                        contentDescription = "Repeat",
                        tint = tintColor,
                        modifier = Modifier.size(20.dp)                     )
                }
            }
            Spacer(modifier = Modifier.weight(.5f))
            // 2. The Buttons Row
            Row(
                modifier = Modifier.weight(2f), // Takes up 1/3 of the main Row's width
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center // Center the content within its 1/3 space
            ) {
                // Previous Button
                IconButton(onClick = { viewModel.skipPrevious() }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play/Pause Button (Highlighted)
                FilledIconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(56.dp) // Make it bigger
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Next Button
                IconButton(onClick = { viewModel.skipNext() }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(.5f))
            Row(
                modifier = Modifier.weight(1f), // Takes up 1/3 of the main Row's width
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End // Anchors content to the right
            ) {
                // Queue Button
                IconButton(onClick = { showQueue = true }) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Queue",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        }
    }
    if (showQueue) {
        ModalBottomSheet(
            onDismissRequest = { showQueue = false },
            sheetState = sheetState
        ) {
            // Content of the Sheet
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Playing Next",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Reuse your existing list logic, or make a simpler one
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp) // padding for bottom bar
                ) {
                    items(nextTracks, key = { it.contentUri }) { track -> // Use a unique key like path or ID if available
                        val trackTitle = track.title
                        QueueItem(
                            title = trackTitle,
                            // isPlaying should be FALSE for all items in 'nextTracks'
                            // The current track is by definition NOT in the queue list.
                            isPlaying = false,
                            onClick = {
                                viewModel.selectTrack(track)
                                showQueue = false
                            }
                        )
                    }
                }
            }
        }
    }
}
fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun QueueItem(title: String, isPlaying: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Playing Indicator or Track Number
        if (isPlaying) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Playing",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            // Just a dot or number for non-playing
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Transparent)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
            color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}