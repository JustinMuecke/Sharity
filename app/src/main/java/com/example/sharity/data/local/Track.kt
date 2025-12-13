package com.example.sharity.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Track(
    @PrimaryKey @ColumnInfo(name = "content_uri") val contentUri: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "artist") val artist: String?,
    @ColumnInfo(name = "release_year") val releaseYear: Int?,
    @ColumnInfo(name = "duration") val duration: Long?
)
