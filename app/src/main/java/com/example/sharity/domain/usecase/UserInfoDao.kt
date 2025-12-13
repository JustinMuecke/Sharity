package com.example.sharity.domain.usecase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sharity.domain.model.UserInfo

@Dao
interface UserInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg userInfo: UserInfo)

    @Delete
    fun delete(userInfo: UserInfo)

    @Update
    fun update(userInfo: UserInfo)

    @Query("SELECT * FROM userInfos")
    fun getAll() : List<UserInfo>

    @Query("""
            INSERT INTO userInfos(`key`, value) 
            SELECT 'uuid', :uuid
            WHERE NOT EXISTS (SELECT 1 FROM userInfos WHERE `key` = 'uuid')
         """)
    fun createUuidIfEmpty(uuid: String)

    @Query("""
            SELECT value
            FROM userInfos
            WHERE `key` = 'uuid'
            LIMIT 1
        """)
    fun getUuid() : String
}