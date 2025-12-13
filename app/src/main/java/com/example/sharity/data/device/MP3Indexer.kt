package com.example.sharity.data.device

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.domain.model.Track
import java.io.FileNotFoundException

class MP3Indexer(
    val context: Context,
    val db: AppDatabase,
    val uri: Uri,
) {

    fun index() {
        indexFiles()
        indexDB()
    }

    /**
     * Searches the shared storage for music files and inserts them into the database index and updated them accordingly.
     */
    private fun indexFiles() {
        val query = context.contentResolver.query(
            uri,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.YEAR,
            ),
            "",
            arrayOf(),
            "",
        )

        val tracks = db.trackDao()

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(uri, id)

                val duration = cursor.getLong(durationColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val year = cursor.getInt(yearColumn)

                Log.i("INFO", "ID: $id")
                Log.i("INFO", "URI: $uri")
                Log.i("INFO", "Duration: $duration")
                Log.i("INFO", "Title: $title")
                Log.i("INFO", "Artist: $artist")
                Log.i("INFO", "Year: $year")
                Log.i("INFO", "")

                val track = Track(
                    contentUri = uri.toString(),
                    duration = duration,
                    title = title,
                    artist = artist,
                    releaseYear = year,
                )

                tracks.insertAll(track)
            }
        }
    }

    /**
     * Searches the database index and checks if the music tracks are still there or have been deleted.
     */
    private fun indexDB() {
        val trackDao = db.trackDao();
        val tracks = trackDao.getAll();

        tracks.forEach { track ->
            try {
                val stream = context.contentResolver.openInputStream(
                    Uri.parse(track.contentUri)
                )
                if (stream == null) {
                    return
                }

                stream.close()
            } catch (e: FileNotFoundException) {
                // File does not exist, remove from index
                Log.i("INFO", "Delete ${track.contentUri}")
                trackDao.delete(track)
            }
        }
    }
}
