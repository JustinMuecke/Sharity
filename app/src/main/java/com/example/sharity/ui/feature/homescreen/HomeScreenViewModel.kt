package com.example.sharity.ui.feature.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.domain.usecase.TrackDao
import com.example.sharity.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
enum class RepeatMode {
    None, All, One
}
class HomeScreenViewModel(
    private val database: AppDatabase,
    val player: ExoPlayer
) : ViewModel() {
    val trackDao : TrackDao = database.trackDao()
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks = _tracks.asStateFlow()
    private val _nextTracks = MutableStateFlow<List<Track>>(emptyList())
    val nextTracks: StateFlow<List<Track>> = _nextTracks.asStateFlow()
    private val _currentPosition = MutableStateFlow(0L) // Current time in ms
    val currentPosition = _currentPosition.asStateFlow()

    private val _trackDuration = MutableStateFlow(0L) // Total time in ms
    val trackDuration = _trackDuration.asStateFlow()
    // =========================================================================
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()



    // =========================================================================
    // NEW: Repeat State
    // =========================================================================
    private val _repeatMode = MutableStateFlow(value = RepeatMode.None)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()


    init {
        loadTracks()
        player.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                // Optional: Handle playlist end
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            // This fires when the song changes (automatically OR via skip)
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentTrackFromPlayer()
                updateNextTracksFromPlayer() // <--- NEW
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                // Fired when tracks are added/removed OR shuffle/repeat state changes
                updateNextTracksFromPlayer() // <--- NEW
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updateNextTracksFromPlayer() // <--- NEW
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updateNextTracksFromPlayer() // <--- NEW
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

    fun toggleRepeat() {
        _repeatMode.update { currentMode ->
            val newMode = when (currentMode) {
                RepeatMode.None -> RepeatMode.All
                RepeatMode.All -> RepeatMode.One
                RepeatMode.One -> RepeatMode.None
            }

            player.repeatMode = when (newMode) {
                RepeatMode.None -> Player.REPEAT_MODE_OFF
                RepeatMode.All -> Player.REPEAT_MODE_ALL
                RepeatMode.One -> Player.REPEAT_MODE_ONE
            }

            newMode
        }
    }

    fun toggleShuffle() {
        _isShuffleEnabled.update { isShuffling ->
            val newState = !isShuffling
            player.shuffleModeEnabled = newState

            newState
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
        updateNextTracksFromPlayer()
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
    fun selectTrack(selectedTrack: Track) {
        val currentList = _tracks.value

        // B. Find the index of the clicked track
        val startIndex = currentList.indexOfFirst { it.title == selectedTrack.title }

        if (startIndex != -1) {
            // C. Convert your Track objects to ExoPlayer MediaItems
            val mediaItems = currentList.map { track ->
                MediaItem.Builder()
                    .setUri(track.contentUri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artist ?: "Unknown")
                            .build()
                    )
                    .build()
            }


            player.setMediaItems(mediaItems)

            player.seekTo(startIndex, 0)
            player.prepare()
            player.play()

            // F. Update UI immediately
            _currentTrack.value = selectedTrack.title
        }
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


    private fun updateCurrentTrackFromPlayer() {
        // ExoPlayer knows the index of the current song
        val index = player.currentMediaItemIndex
        val currentList = _tracks.value

        if (index in currentList.indices) {
            // Update the text and the highlighted card
            _currentTrack.value = currentList[index].title
        }
    }

    // --- 4. NAVIGATION ---
    fun skipNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        }
    }

    fun skipPrevious() {
        // If we are more than 3 seconds in, restart the song. Otherwise go to prev.
        if (player.currentPosition > 3000) {
            player.seekTo(0)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        }
    }

    private fun updateNextTracksFromPlayer() {
        val timeline = player.currentTimeline
        val currentWindowIndex = player.currentMediaItemIndex

        if (timeline.isEmpty || currentWindowIndex == C.INDEX_UNSET) {
            _nextTracks.value = emptyList()
            return
        }

        val futureTracks = mutableListOf<Track>()
        val window = Timeline.Window()

        // Start with the index immediately following the current one
        var nextIndex = timeline.getNextWindowIndex(
            currentWindowIndex,
            player.repeatMode,
            player.shuffleModeEnabled
        )

        // Loop through the next items in the playlist order
        while (nextIndex != C.INDEX_UNSET && nextIndex != currentWindowIndex) {
            timeline.getWindow(nextIndex, window)
            val mediaItem = window.mediaItem ?: break

            // Find the full 'Track' object that corresponds to this MediaItem/Title
            val track = _tracks.value.find { it.title == mediaItem.mediaMetadata.title }

            if (track != null) {
                futureTracks.add(track)
            }

            // Get the next index in the playback sequence
            nextIndex = timeline.getNextWindowIndex(
                nextIndex,
                player.repeatMode,
                player.shuffleModeEnabled
            )

            // Safety break to prevent infinite loops in certain repeat modes if not handled by timeline
            if (futureTracks.size > timeline.windowCount) break
        }

        _nextTracks.value = futureTracks
    }
}