package com.example.sharity.ui.feature.homescreen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeScreenViewModel : ViewModel() {

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