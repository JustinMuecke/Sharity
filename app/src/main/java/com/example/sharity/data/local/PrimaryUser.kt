package com.example.sharity.data.local

import android.content.ContentResolver
import android.provider.Settings
import android.util.Log
import kotlin.random.Random

// TODO: Just for mocking till the real functionality comes in place
object PrimaryUser {
    lateinit var handshakeId: String
    lateinit var username: String
    lateinit var font: String

    fun init(contentResolver: ContentResolver, username: String, font: String) : PrimaryUser {
        // TODO: Replace androidID with unique identifier, from device to connect later
        val handshakeChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        handshakeId = (1..16)
            .map { Random.nextInt(0, handshakeChars.size).let { handshakeChars[it] } }
            .joinToString("")

        PrimaryUser.username = username
        PrimaryUser.font = font
        return this
    }

    fun getConcatenatedData() : ByteArray {
        return handshakeId.toByteArray(Charsets.UTF_8)
    }
}