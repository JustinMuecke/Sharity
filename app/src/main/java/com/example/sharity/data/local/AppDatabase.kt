package com.example.sharity.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sharity.domain.model.Playlist
import com.example.sharity.domain.model.Track
import com.example.sharity.domain.model.TrackPlaylistJunction
import com.example.sharity.domain.model.Connection
import com.example.sharity.domain.model.UserInfo
import com.example.sharity.domain.usecase.PlaylistDao
import com.example.sharity.domain.usecase.TrackDao
import com.example.sharity.domain.usecase.ConnectionDao
import com.example.sharity.domain.usecase.UserInfoDao

@Database(
    entities = [
        Track::class,
        Playlist::class,
        TrackPlaylistJunction::class,
        Connection::class,
        UserInfo::class,
        ],
    version = 11)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun connectionDao(): ConnectionDao
    abstract fun userInfoDao(): UserInfoDao
}

