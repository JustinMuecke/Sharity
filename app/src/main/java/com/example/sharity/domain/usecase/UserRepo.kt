package com.example.sharity.domain.usecase

import com.example.sharity.domain.model.UserInfo
import com.example.sharity.domain.model.UserProfile

class UserRepo(
    private val userInfoDao: UserInfoDao,
    private val trackDao: TrackDao
) {
    fun getProfile() : UserProfile {
        val infos = userInfoDao.getAll().toMap()

        return UserProfile (
            uuid = infos["uuid"]!!,
            playerName = infos["player_name"] ?: "",
            description = infos["description"] ?: "",
            font = infos["font"] ?: "default",
            sent = infos["sent"]?.toInt() ?: 0,
            received = infos["received"]?.toInt() ?: 0,
            tracksTotal = trackDao.countTracks()
        )
    }

    fun onNfcStatsReceived(sent: Int, received: Int) {
        userInfoDao.upsert("sent", sent.toString())
        userInfoDao.upsert("received", received.toString())
    }

    fun List<UserInfo>.toMap(): Map<String, String> =
        associate { it.key to it.value }
}