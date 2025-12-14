package com.example.sharity.data.wrapper

import android.app.Application
import android.content.Context
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.domain.usecase.UserRepo

class DbAccess : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var userRepo: UserRepo
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)

        userRepo = UserRepo(
            database.userInfoDao(),
            database.trackDao()
        )
    }
}
fun Context.db(): AppDatabase =
    (applicationContext as DbAccess).database

fun Context.userRepo(): UserRepo =
    (applicationContext as DbAccess).userRepo