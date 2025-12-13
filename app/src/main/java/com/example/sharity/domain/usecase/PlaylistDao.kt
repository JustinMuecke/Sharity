package com.example.sharity.domain.usecase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.sharity.domain.model.Playlist
import com.example.sharity.domain.model.PlaylistWithTracks
import com.example.sharity.domain.model.Track
import com.example.sharity.domain.model.TrackPlaylistJunction

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createPlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlaylistTrackCrossRef(crossRef: TrackPlaylistJunction)

    @Transaction
    open fun addTrackToPlaylist(playlist: Playlist, track: Track)
    {
        insertPlaylistTrackCrossRef(
            TrackPlaylistJunction(
                playlist.playlistID,
                track.trackID
            )
        )
    }

    @Delete
    fun delete(playlist: Playlist)

    @Update
    fun update(playlist: Playlist)

    @Transaction
    @Query("SELECT * FROM playlists WHERE playlistID = :id")
    fun getPlaylist(id: Int) : PlaylistWithTracks
}