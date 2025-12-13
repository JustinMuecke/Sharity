package com.example.sharity.ui.feature.homescreen

import android.media.browse.MediaBrowser
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.data.local.Track
import com.example.sharity.data.local.TrackDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val database: AppDatabase,
    val player: ExoPlayer
) : ViewModel() {
    val trackDao : TrackDao = database.trackDao()
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks = _tracks.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L) // Current time in ms
    val currentPosition = _currentPosition.asStateFlow()

    private val _trackDuration = MutableStateFlow(0L) // Total time in ms
    val trackDuration = _trackDuration.asStateFlow()

    init {
        // 2. Trigger the load immediately when ViewModel starts
        loadTracks()
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            // Optional: Auto-play next track when one finishes
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    skipNext()
                }
            }
        })
        viewModelScope.launch {
            while (true) {
                if (player.isPlaying) {
                    _currentPosition.value = player.currentPosition
                    _trackDuration.value = player.duration.coerceAtLeast(0L)
                }
                delay(500) // Update UI every 0.5 seconds
            }
        }
    }


    private fun loadTracks() {
        // 3. Move to IO Thread (Background)
        viewModelScope.launch(Dispatchers.IO) {

            // Now this is safe!
            val trackObjects = trackDao.getAllAsync()
            // Update State (StateFlow is thread-safe)
            _tracks.value = trackObjects
        }
    }
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<String?>(null)
    val currentTrack = _currentTrack.asStateFlow()

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _currentPosition.value = positionMs // Immediate UI update
    }
    // 2. New Action: Function to handle the click
    fun selectTrack(track: Track) {
        _currentTrack.value = track.title

        // 1. Prepare the Media Item
        val mediaItem = MediaItem.fromUri(track.contentUri)
        // 2. Tell Player to play this item
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }
    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release() // Important: Free memory when app closes
    }


    // 3. Logic: Skip Tracks (Placeholder for now)
    fun skipNext() {
        // Logic to move to next index in list
    }

    fun skipPrevious() {
        // Logic to move to previous index
    }


}