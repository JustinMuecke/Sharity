package com.example.sharity.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sharity.domain.model.Playlist
import com.example.sharity.domain.model.Track
import com.example.sharity.domain.model.TrackPlaylistJunction
import com.example.sharity.domain.model.User
import com.example.sharity.domain.usecase.PlaylistDao
import com.example.sharity.domain.usecase.TrackDao
import com.example.sharity.domain.usecase.UserDao

@Database(entities = [Track::class, Playlist::class, TrackPlaylistJunction::class ,User::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun userDao(): UserDao
}

