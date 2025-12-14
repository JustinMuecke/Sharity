package com.example.sharity.domain.usecase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sharity.domain.model.Connection
import com.example.sharity.domain.model.NameStatsJunction

@Dao
interface ConnectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg connection: Connection)

    @Query("""
        UPDATE connections
        SET tracks_received = :received
        WHERE connection_id = (
            SELECT MAX(connection_id)
            FROM connections
            WHERE connection_uuid = :name
        )
    """)
    fun insertTracksReceived(received: Int, name: String)

    @Query("""
        UPDATE connections
        SET tracks_received = :sent
        WHERE connection_id = (
            SELECT MAX(connection_id)
            FROM connections
            WHERE connection_uuid = :name
        )
    """)
    fun insertTracksSent(sent: Int, name: String)

    @Delete
    fun delete(connection: Connection)

    @Update
    fun update(connection: Connection)

    @Query("SELECT * FROM connections")
    fun getAll() : List<Connection>

    @Query("SELECT * FROM connections ORDER BY connection_id DESC LIMIT 30")
    fun getLatest() : List<Connection>

    @Query("""
            SELECT
                name,
                MAX(tracks_sent + tracks_received) AS stats
            FROM connections
            GROUP BY connection_uuid
            ORDER BY stats DESC
           """)
    fun getDistinctByMax() : List<NameStatsJunction>

    @Query("SELECT COUNT(*) FROM connections")
    fun countConnections(): Int

    companion object

}