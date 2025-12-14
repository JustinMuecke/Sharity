package com.example.sharity.ui.feature.allsongsscreen

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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
enum class RepeatMode {
    None, All, One
}
class AllSongsViewModel(
    private val database: AppDatabase,
    val player: ExoPlayer // Still handles playback
) : ViewModel() {

    private val trackDao: TrackDao = database.trackDao()

    // -------------------------------------------------------------------------
    // 1. DATA AND FILTER STATE
    // -------------------------------------------------------------------------
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks = _tracks.asStateFlow() // Unfiltered Master List

    private val _currentListName = MutableStateFlow("All Songs")
    val currentListName = _currentListName.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = _searchQuery.asStateFlow()
    // DERIVED FLOW FOR FILTERED TRACKS (The list the UI should render)
    val filteredTracks: StateFlow<List<Track>> = _tracks.combine(_searchQuery) { trackList, query ->
        // ⬅️ NOTE: Filtering logic must use the filtered list, and you no longer need distinctBy
        if (query.isBlank()) {
            trackList
        } else {
            // Filter by title OR artist, ignoring case
            trackList.filter { track ->
                track.title.contains(query, ignoreCase = true) ||
                        (track.artist ?: "").contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    // -------------------------------------------------------------------------
    // 2. MULTI-SELECTION STATE AND LOGIC (NEW)
    // -------------------------------------------------------------------------
    private val _selectedTracks = MutableStateFlow<Set<Track>>(emptySet())
    val selectedTracks: StateFlow<Set<Track>> = _selectedTracks.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    /**
     * Toggles the selection state of a single track.
     * Also updates the selection mode flag.
     */
    fun toggleSelect(track: Track) {
        _selectedTracks.update { currentSet ->
            val newSet = if (currentSet.contains(track)) {
                currentSet - track
            } else {
                currentSet + track
            }

            // Update selection mode based on the new count
            _isSelectionMode.value = newSet.isNotEmpty()

            newSet
        }
    }

    /** Clears all selections and exits selection mode. */
    fun clearSelection() {
        _selectedTracks.value = emptySet()
        _isSelectionMode.value = false
    }

    /** Returns the currently selected tracks for use in creation/sharing. */
    fun getSelectedTracks(): List<Track> {
        return _selectedTracks.value.toList()
    }


    // -------------------------------------------------------------------------
    // 3. PLAYBACK STATE AND LOGIC (EXISTING)
    // -------------------------------------------------------------------------
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()
    private val _trackDuration = MutableStateFlow(0L)
    val trackDuration = _trackDuration.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    private val _currentTrack = MutableStateFlow<Track?>(null) // Now holds contentUri
    val currentTrack = _currentTrack.asStateFlow()
    private val _nextTracks = MutableStateFlow<List<Track>>(emptyList())
    val nextTracks: StateFlow<List<Track>> = _nextTracks.asStateFlow()
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()
    private val _repeatMode = MutableStateFlow(value = RepeatMode.None)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()



    init {
        loadTracks()
        player.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentTrackFromPlayer()
                updateNextTracksFromPlayer()
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                updateNextTracksFromPlayer()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updateNextTracksFromPlayer()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updateNextTracksFromPlayer()
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

    // 3. FUNCTION TO UPDATE SEARCH QUERY
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleRepeat() {
        // ... (existing implementation) ...
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
        // ... (existing implementation) ...
        _isShuffleEnabled.update { isShuffling ->
            val newState = !isShuffling
            player.shuffleModeEnabled = newState

            newState
        }
    }

    private fun loadTracks() {
        viewModelScope.launch(Dispatchers.IO) {
            val trackObjects = trackDao.getAllAsync()
            _tracks.value = trackObjects
        }
    }


    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _currentPosition.value = positionMs // Immediate UI update
    }


    // Add this to your AllSongsViewModel class:

    // Add a new state to track the current playback context
    private val _currentPlaybackContext = MutableStateFlow<List<Track>>(emptyList())

    // Modify selectTrack to update the context:
    fun selectTrack(selectedTrack: Track) {
        val currentList = _tracks.value
        _currentPlaybackContext.value = currentList // Store the context

        val startIndex = currentList.indexOfFirst { it.contentUri == selectedTrack.contentUri }

        if (startIndex != -1) {
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

            _currentTrack.value = selectedTrack
        }
    }

    // Modify selectTrackInContext to update the context:
    fun selectTrackInContext(selectedTrack: Track, contextList: List<Track>) {
        _currentPlaybackContext.value = contextList // Store the context

        val startIndex = contextList.indexOfFirst { it.contentUri == selectedTrack.contentUri }

        if (startIndex != -1) {
            val mediaItems = contextList.map { track ->
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

            _currentTrack.value = selectedTrack
        }
    }

    // Update updateNextTracksFromPlayer to use the context:
    private fun updateNextTracksFromPlayer() {
        val timeline = player.currentTimeline
        val currentWindowIndex = player.currentMediaItemIndex

        if (timeline.isEmpty || currentWindowIndex == C.INDEX_UNSET) {
            _nextTracks.value = emptyList()
            return
        }

        val futureTracks = mutableListOf<Track>()
        val window = Timeline.Window()

        // Use the current playback context instead of _tracks
        val contextList = _currentPlaybackContext.value

        var nextIndex = timeline.getNextWindowIndex(
            currentWindowIndex,
            player.repeatMode,
            player.shuffleModeEnabled
        )

        while (nextIndex != C.INDEX_UNSET && nextIndex != currentWindowIndex) {
            timeline.getWindow(nextIndex, window)
            val mediaItem = window.mediaItem ?: break

            val mediaUriString = mediaItem.localConfiguration?.uri.toString()

            // Find the track in the current playback context
            val track = contextList.find { it.contentUri == mediaUriString }

            if (track != null) {
                futureTracks.add(track)
            }

            nextIndex = timeline.getNextWindowIndex(
                nextIndex,
                player.repeatMode,
                player.shuffleModeEnabled
            )

            if (futureTracks.size > timeline.windowCount) break
        }

        _nextTracks.value = futureTracks
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
        player.release()
    }


    private fun updateCurrentTrackFromPlayer() {
        val index = player.currentMediaItemIndex
        val currentList = _tracks.value

        if (index in currentList.indices) {
            _currentTrack.value = currentList[index] // ⬅️ Save the unique URI
        }
    }

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



}