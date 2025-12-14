package com.example.sharity.ui.feature.playlistselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // 1. Need this for coroutines
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.domain.model.Playlist
import com.example.sharity.domain.model.Track
import com.example.sharity.domain.model.TrackPlaylistJunction
import com.example.sharity.domain.usecase.PlaylistDao
import com.example.sharity.domain.usecase.TrackDao
import kotlinx.coroutines.flow.SharingStarted // 2. Need this for stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn // <-- This is the crucial one
import kotlin.collections.forEach

class PlaylistSelectionViewModel(
    db : AppDatabase
): ViewModel() {
    private val playlistDao: PlaylistDao = db.playlistDao()
    private val trackDao: TrackDao = db.trackDao()

    // 5. CORRECT WAY: Use stateIn to collect the database Flow
    val userPlaylists: StateFlow<List<Playlist>> = playlistDao.getPlaylists()
        .stateIn(
            scope = viewModelScope,
            // Start sharing the flow when there's an observer (i.e., the Composable)
            started = SharingStarted.WhileSubscribed(5000),
            // Initial value before the database has returned anything
            initialValue = emptyList<Playlist>()
        )

    // You should wrap DAO writes in coroutines
    suspend fun createPlaylistWithTracks(name: String, tracks: List<Track>): Int {

        // 1. Create the Playlist object
        val newPlaylist = Playlist(playlistName = name)

        // 2. Insert and CAPTURE the returned ID directly
        // The createPlaylist function now returns the generated Long ID
        val newPlaylistIdLong = playlistDao.createPlaylist(newPlaylist)

        // Cast to Int if your IDs are Ints (Room returns Long for generated IDs)
        val finalPlaylistId = newPlaylistIdLong.toInt()

        // 3. Insert tracks and cross-references
        tracks.forEach { track ->
            // A. Ensure the Track exists in the DB first
            trackDao.insertAllAsync(track)

            // B. Create the Junction using the ACTUAL database ID (finalPlaylistId)
            // Do NOT use newPlaylist.playlistID here, as that is likely still 0
            val junction = TrackPlaylistJunction(
                playlistID = finalPlaylistId,
                contentUri = track.contentUri
            )

            // C. Insert the link
            playlistDao.insertPlaylistTrackCrossRef(junction)
        }

        // 4. Return the ID for navigation
        return finalPlaylistId
    }


}