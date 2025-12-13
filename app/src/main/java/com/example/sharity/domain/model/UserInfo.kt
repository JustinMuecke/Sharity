package com.example.sharity.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "userInfos")
data class UserInfo(
    @PrimaryKey(true)
    @ColumnInfo(name = "stat_id")
    val statID: Int = 0,

    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "value") val value: String
)
