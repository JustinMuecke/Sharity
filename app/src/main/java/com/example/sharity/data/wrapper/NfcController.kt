package com.example.sharity.data.wrapper

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build


class NfcController(
    private val activity: Activity,
    private val onTagDiscovered: (Tag) -> Unit
) {

    private val nfcAdapter: NfcAdapter? =
        NfcAdapter.getDefaultAdapter(activity)

    fun onResume() {
        nfcAdapter ?: return

        val intent = Intent(activity, activity.javaClass)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent = PendingIntent.getActivity(
            activity,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )

        nfcAdapter.enableForegroundDispatch(
            activity,
            pendingIntent,
            null,
            null
        )
    }

    fun onPause() {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    fun onNewIntent(intent: Intent) {
        val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }

        tag?.let(onTagDiscovered)
    }
}

