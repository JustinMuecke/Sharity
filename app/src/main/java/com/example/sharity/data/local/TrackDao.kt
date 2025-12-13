package com.example.sharity.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun InsertAll(vararg track: Track)

    @Delete
    fun delete(track: Track)

    @Update
    fun update(track: Track)

    @Query("SELECT * FROM track")
    fun getAll() : List<Track>
}