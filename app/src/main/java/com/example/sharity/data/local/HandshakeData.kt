package com.example.sharity.data.local

data class HandshakeData(
    val peerUuid: String?,
    val name: String,
    val font: String,
    val port: Int,
    val token: String
)

fun parseHandshake(bytes: ByteArray): HandshakeData {
    val raw = bytes.toString(Charsets.UTF_8).trim()
    val parts = raw.split(",")

    require(parts.size == 5) {
        "Invalid handshake payload: '$raw'"
    }

    return HandshakeData(
        peerUuid = parts[0].ifBlank { null },
        name = parts[1],
        token = parts[2],
        port = parts[3].toInt(),
        font = parts[4],
    )
}
