package com.example.sharity.data.device

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.util.concurrent.TimeUnit

class MP3Indexer(
    val context: Context,
) {

    fun index() {
        val location = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val query = context.contentResolver.query(
            location,
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

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(location, id)

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
            }
        }
    }
}
