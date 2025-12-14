package com.example.sharity.data.device

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.example.sharity.data.wrapper.userRepo
import com.example.sharity.domain.model.toNfcPayload
import kotlinx.coroutines.runBlocking

class NfcProfileService : HostApduService() {
    private lateinit var profile: ByteArray
    private var offset = 0

    override fun processCommandApdu(
        commandApdu: ByteArray,
        extras: Bundle?
    ): ByteArray {

        /*
            0x01 -> Request Profile
            0x02 -> Request next chunk of profile
            0x03 -> Transaction complete
            0x06 -> Error
         */

        if (isSelectApdu(commandApdu)) {
            offset = 0
            return byteArrayOf(0x90.toByte(), 0x00)
        }

        if (commandApdu.isEmpty()) {
            return statusError()
        }

        return when (commandApdu[0]) {
            0x01.toByte() -> {
                Log.d("NfcProfileServer", "REQUEST PROFILE")
                profile = runBlocking {
                    userRepo().getProfile().toNfcPayload()
                }
                offset = 0
                nextChunk()
            }

            0x02.toByte() -> {
                Log.d("NfcProfileServer", "REQUEST NEXT CHUNK")
                nextChunk()
            }

            0x03.toByte() -> {
                Log.e("NfcProfileServer", "PROFILE COMPLETE")
                byteArrayOf(0x03, 0x00)
            }

            else -> {
                Log.e("NfcProfileServer", "UNKNOWN APDU")
                statusError()
            }
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

    private fun statusError() = byteArrayOf(0x6F.toByte(), 0x00)

    private fun isSelectApdu(apdu: ByteArray): Boolean {
        return apdu.size >= 4 &&
                apdu[1] == 0xA4.toByte()
    }

}