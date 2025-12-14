package com.example.sharity.domain.model

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
    return "$uuid,$playerName,$font"
        .toByteArray(Charsets.UTF_8)
}
