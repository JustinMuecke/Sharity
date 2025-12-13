package com.example.sharity.data.local

import android.content.ContentResolver
import android.provider.Settings
import android.util.Log

// TODO: Just for mocking till the real functionality comes in place
object PrimaryUser {
    lateinit var androidID: String
    lateinit var username: String
    lateinit var font: String

    fun init(contentResolver: ContentResolver, username: String, font: String) : PrimaryUser {
        // TODO: Replace androidID with unique identifier, from device to connect later
        // TODO: sign with uuid

        androidID = Settings.Secure.ANDROID_ID.toString()
        PrimaryUser.username = username
        PrimaryUser.font = font
        return this
    }

    fun getConcatenatedData() : ByteArray {
        return "$androidID$username$font".toByteArray(Charsets.UTF_8)
    }
}