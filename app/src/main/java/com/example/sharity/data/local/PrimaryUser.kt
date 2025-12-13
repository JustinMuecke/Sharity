package com.example.sharity.data.local

import android.content.ContentResolver
import android.provider.Settings

// TODO: Just for mocking till the real functionality comes in place
object PrimaryUser {
    lateinit var androidID: String
    lateinit var username: String
    lateinit var font: String

    fun init(db: AppDatabase, username: String, font: String) : PrimaryUser {
        androidID = db.userInfoDao().getUuid()
        PrimaryUser.username = username
        PrimaryUser.font = font
        return this
    }

    fun getConcatenatedData() : ByteArray {
        return "$androidID,$username,$font".toByteArray(Charsets.UTF_8)
    }
}