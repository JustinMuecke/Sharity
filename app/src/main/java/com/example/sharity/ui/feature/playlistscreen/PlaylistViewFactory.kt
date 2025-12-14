// ui.feature.playlistscreen/PlaylistViewModelFactory.kt (FIX THIS FILE)

package com.example.sharity.ui.feature.playlistscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sharity.data.local.AppDatabase
// The import below should point to the correct ViewModel, which is PlaylistViewModel in this package

class PlaylistViewModelFactory(
    private val db: AppDatabase,
    private val playlistId: Int // <-- This is correct for the PlaylistView screen
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 1. Check if the class is the one *for the details screen*
        if (modelClass.isAssignableFrom(PlaylistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // 2. PASS ALL REQUIRED ARGUMENTS: db AND playlistId!
            return PlaylistViewModel(db, playlistId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}