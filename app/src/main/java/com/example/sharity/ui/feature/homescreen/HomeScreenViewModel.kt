package com.example.sharity.ui.feature.homescreen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeScreenViewModel : ViewModel() {

    private val _names = MutableStateFlow(
        listOf("Alice", "Bob", "Charlie", "David", "Eve", "Justin", "Peter", "Samuel", "Alex", "Fabi")
    )
    val tracks = _names.asStateFlow()
    private val _currentTrack = MutableStateFlow<String?>(null)
    val currentTrack = _currentTrack.asStateFlow()

    // 2. New Action: Function to handle the click
    fun selectTrack(trackName: String) {
        _currentTrack.value = trackName
    }
}