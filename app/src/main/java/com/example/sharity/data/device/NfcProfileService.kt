package com.example.sharity.data.device

import android.nfc.cardemulation.HostApduService
import android.os.Bundle

class NfcProfileService : HostApduService() {
    //TODO: Replace MY_PROFILE_DATA with the MAC address, name and stats to send
    private val profile = "MY_PROFILE_DATA".toByteArray()
    private var offset = 0

    override fun processCommandApdu(
        commandApdu: ByteArray,
        extras: Bundle?
    ): ByteArray {

        return when (commandApdu[0]) {
            /*
                0x01 -> Request Profile
                0x02 -> Request next chunk of profile
                0x03 -> Transaction complete
                0x06 -> Error
             */
            0x01.toByte() -> {
                offset = 0
                nextChunk()
            }

            0x02.toByte() -> {
                nextChunk()
            }

            0x03.toByte() -> {
                byteArrayOf(0x03, 0x00)
            }

            else -> statusError()
        }
    }

    private fun nextChunk(): ByteArray {
        if (offset >= profile.size) {
            return byteArrayOf(0x03, 0x00)
        }

        val chunkSize = minOf(20, profile.size - offset)
        val chunk = profile.copyOfRange(offset, offset + chunkSize)
        offset += chunkSize

        return byteArrayOf(0x02, chunkSize.toByte()) + chunk
    }

    override fun onDeactivated(reason: Int) {
        offset = 0
    }

    private fun statusError() =
        byteArrayOf(0x6F.toByte(), 0x00)
}