package com.example.sharity.data.device

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log

class NfcClient {

    /// Schema: Fetch Profile -> Got Chunks -> Got them all -> Complete
    suspend fun fetchProfile(tag: Tag): Result<ByteArray> =
        runCatching {
            val isoDep = IsoDep.get(tag) ?: error("Not ISO-DEP")

            isoDep.connect()

            val selectApdu = buildSelectApdu("F0010203040506")
            val selectResponse = isoDep.transceive(selectApdu)

            val sw = selectResponse.takeLast(2).toByteArray()

            if (!sw.contentEquals(byteArrayOf(0x90.toByte(), 0x00))) {
                error("AID selection failed")
            }

            var response = isoDep.transceive(byteArrayOf(0x01, 0x00))

            val profileData = mutableListOf<Byte>()

            while (response[0] == 0x02.toByte()) {
                val len = response[1].toInt()
                profileData += response.copyOfRange(2, 2 + len).toList()

                response = isoDep.transceive(byteArrayOf(0x02, 0x00))
            }

            isoDep.transceive(byteArrayOf(0x03, 0x00))
            isoDep.close()

            profileData.toByteArray()
        }

    private fun buildSelectApdu(aid: String): ByteArray {
        val aidBytes = aid.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

        return byteArrayOf(
            0x00,
            0xA4.toByte(),
            0x04,
            0x00,
            aidBytes.size.toByte()
        ) + aidBytes
    }
}
