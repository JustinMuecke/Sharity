package com.example.sharity.ui.feature.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.lifecycle.viewModelScope
import com.example.sharity.data.local.Database
import com.example.sharity.data.local.TrackDao
import com.example.sharity.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeScreenViewModel(private val trackDao: TrackDao) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())

    init {
        // 2. Trigger the load immediately when ViewModel starts
        loadTracks()
    }

    private fun loadTracks() {
        // 3. Move to IO Thread (Background)
        viewModelScope.launch(Dispatchers.IO) {

            // Now this is safe!
            val trackObjects = trackDao.getAll()
            // Update State (StateFlow is thread-safe)
            _tracks.value = trackObjects
        }
    }

    private val _names = MutableStateFlow(
        listOf("Alice", "Bob", "Charlie", "David", "Eve", "Justin", "Peter", "Samuel", "Alex", "Fabi")
    )
    val tracks = _names.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<String?>(null)
    val currentTrack = _currentTrack.asStateFlow()

    // 2. New Action: Function to handle the click
    fun selectTrack(trackName: String) {
        _currentTrack.value = trackName
    }


    // 2. Logic: Toggle Play/Pause
    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    // 3. Logic: Skip Tracks (Placeholder for now)
    fun skipNext() {
        // Logic to move to next index in list
    }

    fun skipPrevious() {
        // Logic to move to previous index
    }
}