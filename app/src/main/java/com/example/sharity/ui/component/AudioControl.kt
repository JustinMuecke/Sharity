package com.example.sharity.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioControl(viewModel : HomeScreenViewModel){
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
// 1. Collect progress data
    val currentPos by viewModel.currentPosition.collectAsState()
    val duration by viewModel.trackDuration.collectAsState()

    // 2. Local state to handle dragging smoothly
    var isDragging by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0f) }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh, // Slightly darker background
        tonalElevation = 8.dp, // Adds a slight shadow/lift
        modifier = Modifier.fillMaxWidth()
    ) {
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
                    // 1. CUSTOM THUMB (The little circle)
                    thumb = {
                        // Creates a 12dp white circle with a shadow
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 4.dp, // Adds a subtle drop shadow
                            modifier = Modifier.size(12.dp) // Much smaller than default (20dp)
                        ) {
                            // Empty body
                        }
                    },
                    // 2. CUSTOM TRACK (The line)
                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            modifier = Modifier.height(4.dp), // Force the container height
                            thumbTrackGapSize = 0.dp, // Remove gap between thumb and line
                            drawStopIndicator = null, // Remove dots at start/end
                            colors = SliderDefaults.colors(
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    },
                    modifier = Modifier.height(20.dp) // Reduce overall touch area height slightly
                )

                // Time Labels (Made smaller and closer to slider)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp) // Align with the track start/end
                        .offset(y = (-6).dp), // Pull text up closer to the slider
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(if (isDragging) sliderValue.toLong() else currentPos),
                        style = MaterialTheme.typography.labelSmall, // Smaller font
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

            // 2. The Buttons Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp) // Space between buttons
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
        }
    }
}
fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}