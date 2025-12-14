package com.example.sharity.domain.usecase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.domain.model.Playlist
import com.example.sharity.domain.model.PlaylistWithTracks
import com.example.sharity.domain.model.Track
import com.example.sharity.domain.model.TrackPlaylistJunction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlist: Playlist)

    @Query("SELECT playlist_id FROM playlists WHERE playlist_name = :name")
    suspend fun getID(name: String) : Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrackCrossRef(crossRef: TrackPlaylistJunction)

    @Transaction
    suspend fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        createPlaylist(playlist)
        insertPlaylistTrackCrossRef(
            TrackPlaylistJunction(playlist.playlistID, track.contentUri)
        )
    }

    @Delete
    suspend fun delete(playlist: Playlist)

    @Update
    suspend fun update(playlist: Playlist)

    @Transaction
    @Query("SELECT * FROM playlists WHERE playlist_id = :id")
    suspend fun getPlaylist(id: Int) : PlaylistWithTracks

    @Transaction
    @Query(value="SELECT * FROM playlists")
    fun getPlaylists() : Flow<List<Playlist>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE playlist_id = :id")
    fun getPlaylistWithTracks(id: Int): Flow<PlaylistWithTracks?> // <-- New: Reactive playlist with tracks

    @Transaction
    @Query("""
    SELECT T.* FROM tracks AS T
    INNER JOIN TrackPlaylistJunction AS J 
    ON T.content_uri = J.content_uri  -- <--- FIX 1: Changed J.trackContentUri to J.content_uri
    WHERE J.playlist_id = :id         -- <--- FIX 2: Changed J.playlistId to J.playlist_id
""")
    fun getSongsByPlaylistId(id: Int): Flow<List<Track>>


}