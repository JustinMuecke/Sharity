package com.example.sharity.data.wrapper

object NfcBlocker {

    private var lastHandshakeTime = 0L
    private const val COOLDOWN_MS = 10_000L

    fun canStartHandshake(): Boolean {
        val now = System.currentTimeMillis()
        return now - lastHandshakeTime > COOLDOWN_MS
    }

    fun markHandshakeStarted() {
        lastHandshakeTime = System.currentTimeMillis()
    }
}
