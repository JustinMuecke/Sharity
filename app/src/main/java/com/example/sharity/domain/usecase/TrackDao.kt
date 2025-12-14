package com.example.sharity.domain.usecase

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
    fun insertAll(vararg track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAsync(vararg track: Track)

    @Delete
    fun delete(track: Track)

    @Update
    fun update(track: Track)


    @Query("SELECT * FROM tracks")
    fun getAll() : List<Track>?
    @Query("SELECT * FROM tracks")
    suspend fun getAllAsync() : List<Track>

    @Query("SELECT COUNT(*) FROM tracks")
    fun countTracks() : Int
}