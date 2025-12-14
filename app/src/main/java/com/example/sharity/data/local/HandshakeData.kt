package com.example.sharity.data.local

data class HandshakeData(
    val peerUuid: String?,
    val name: String,
    val font: String,
    val port: Int,
    val token: String
)

fun parseHandshake(bytes: ByteArray): HandshakeData {
    val parts = bytes.toString(Charsets.UTF_8).split(",")

    return HandshakeData(
        peerUuid = parts[0],
        name = parts[1],
        token = parts[2],
        port = parts[3].toInt(),
        font = parts[4],
    )
}