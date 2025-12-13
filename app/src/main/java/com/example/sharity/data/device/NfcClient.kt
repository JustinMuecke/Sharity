package com.example.sharity.data.device

import android.nfc.Tag
import android.nfc.tech.IsoDep

class NfcClient {

    /// Schema: Fetch Profile -> Got Chunks -> Got them all -> Complete
    suspend fun fetchProfile(tag: Tag): Result<ByteArray> =
        runCatching {
            val isoDep = IsoDep.get(tag)
                ?: error("Not ISO-DEP")

            isoDep.connect()

            val request = byteArrayOf(0x01, 0x00) // -> Request Data, no Data
            var response = isoDep.transceive(request)

            val profileData = mutableListOf<Byte>()

            while (response[0] == 0x02.toByte()) { // -> Give me the next chunk in case profile is bigger than expected
                val len = response[1].toInt()
                profileData += response.copyOfRange(2, 2 + len).toList()

                response = isoDep.transceive(byteArrayOf(0x02, 0x00))
            }

            isoDep.transceive(byteArrayOf(0x03, 0x00)) // -> End of Profile, no Data
            isoDep.close()

            profileData.toByteArray()
        }

    companion object
}
