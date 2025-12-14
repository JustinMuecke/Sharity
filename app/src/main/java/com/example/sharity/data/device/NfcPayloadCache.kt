package com.example.sharity.data.device

object NfcPayloadCache {

    @Volatile
    private var payload: ByteArray = ByteArray(0)

    fun update(bytes: ByteArray) {
        payload = bytes
    }

    fun get(): ByteArray = payload
}