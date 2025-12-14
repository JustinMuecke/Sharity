package com.example.sharity.domain.model

import java.util.UUID

data class UserProfile(
    val uuid: String,
    val playerName: String,
    val description: String,
    val font: String,
    val sent : Int,
    val received: Int,
    val tracksTotal: Int,
    //val badges: List<Badge> [WIP]
)
fun UserProfile.toNfcPayload(): ByteArray {
    val port = "8888"
    val sessionToken = UUID.randomUUID().toString()
    return "$uuid,$playerName,$sessionToken,$port,$font"
        .toByteArray(Charsets.UTF_8)
}
