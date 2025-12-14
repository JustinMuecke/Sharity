package com.example.sharity.data.device

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.sharity.data.device.NfcPayloadCache

class NfcProfileService : HostApduService() {
    private lateinit var profile: ByteArray
    private var offset = 0
    private var transactionComplete = false
    companion object {
        const val ACTION_NFC_TRANSACTION_COMPLETE = "com.example.sharity.NFC_COMPLETE"
    }

    override fun processCommandApdu(
        commandApdu: ByteArray,
        extras: Bundle?
    ): ByteArray {

        if (isSelectApdu(commandApdu)) {
            offset = 0
            transactionComplete = false
            return byteArrayOf(0x90.toByte(), 0x00)
        }

        if (commandApdu.isEmpty()) {
            return statusError()
        }

        return when (commandApdu[0]) {
            0x01.toByte() -> {
                Log.d("NfcProfileServer", "REQUEST PROFILE")
                profile = NfcPayloadCache.get()
                offset = 0
                nextChunk()
            }

            0x02.toByte() -> {
                Log.d("NfcProfileServer", "REQUEST NEXT CHUNK")
                nextChunk()
            }

            0x03.toByte() -> {
                Log.d("NfcProfileServer", "PROFILE COMPLETE - Transaction finished!")

                if (!transactionComplete) {
                    transactionComplete = true
                    notifyTransactionComplete()
                }
                byteArrayOf(0x03, 0x00)
            }

            else -> {
                Log.e("NfcProfileServer", "UNKNOWN APDU: ${commandApdu[0]}")
                statusError()
            }
        }
    }

    private fun nextChunk(): ByteArray {
        if (offset >= profile.size) {
            Log.d("NfcProfileServer", "All chunks sent, sending completion signal")
            return byteArrayOf(0x03, 0x00)
        }

        val chunkSize = minOf(20, profile.size - offset)
        val chunk = profile.copyOfRange(offset, offset + chunkSize)
        offset += chunkSize

        Log.d("NfcProfileServer", "Sending chunk: $offset/${profile.size} bytes")
        return byteArrayOf(0x02, chunkSize.toByte()) + chunk
    }

    private fun notifyTransactionComplete() {
        Log.d("NfcProfileServer", "Broadcasting NFC transaction complete")
        val intent = Intent(ACTION_NFC_TRANSACTION_COMPLETE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDeactivated(reason: Int) {
        val reasonText = when (reason) {
            DEACTIVATION_LINK_LOSS -> "LINK_LOSS"
            DEACTIVATION_DESELECTED -> "DESELECTED"
            else -> "UNKNOWN($reason)"
        }
        Log.d("NfcProfileServer", "Deactivated: $reasonText")
        offset = 0
        transactionComplete = false
    }

    private fun statusError() = byteArrayOf(0x6F.toByte(), 0x00)

    private fun isSelectApdu(apdu: ByteArray): Boolean {
        return apdu.size >= 5 &&
                apdu[0] == 0x00.toByte() &&
                apdu[1] == 0xA4.toByte() &&
                apdu[2] == 0x04.toByte() &&
                apdu[3] == 0x00.toByte()
    }
}