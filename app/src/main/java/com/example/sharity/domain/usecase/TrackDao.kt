package com.example.sharity.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sharity.domain.model.Track

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun InsertAll(vararg track: Track)

    @Delete
    fun delete(track: Track)

    @Update
    fun update(track: Track)

    @Query("SELECT * FROM tracks")
    suspend fun getAll() : List<Track>
    @Query("SELECT * FROM tracks")
    fun getAllAsync() : List<Track>
}