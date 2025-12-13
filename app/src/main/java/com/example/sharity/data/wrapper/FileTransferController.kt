package com.example.sharity.data.wrapper

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.TimeUnit
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDiscoveryConfig
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.util.Log
import java.io.DataInputStream
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketAddress
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

private val SERVICE_TYPE = "_sharity._tcp"
private val TAG = "FileTransferController"
private val PORT = 42069

class FileTransferController {

    /*
    TCP Message:
    +----------------+
    | 4 Bytes Length |
    +----------------+
    |    Payload     |
    |                |
    |                |
     */

    private val activity: Activity
    private val channel: WifiP2pManager.Channel
    private val manager: WifiP2pManager

    private val intentFilter: IntentFilter

    private val broadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    Log.d(TAG, "P2P connection changed")

                    val networkInfo: NetworkInfo? = intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_NETWORK_INFO,
                        NetworkInfo::class.java,
                    )
                    if (networkInfo == null) {
                        return
                    }

                    if (networkInfo.isConnected != true) {
                        return
                    }

                    manager.requestConnectionInfo(
                        channel,
                        object : WifiP2pManager.ConnectionInfoListener {
                            override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
                                Log.d(TAG, "Address: $info")

                                // TODO: DO NOT DO THIS!
                                if (android.os.Build.MODEL == "Pixel 4a") {
                                    return
                                }
                                /*

                                Thread.sleep(5_000)

                                Socket().use { socket ->
                                    socket.connect(InetSocketAddress(
                                        info?.groupOwnerAddress,
                                        PORT,
                                    ))

                                    Log.d(TAG, "Device connected via TCP!")
                                }
                                */
                            }
                        })
                }
            }
        }
    }

    constructor(activity: Activity) {
        this.activity = activity
        this.manager = activity.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        this.channel = manager.initialize(
            activity.applicationContext,
            activity.mainLooper,
            null,
        )

        this.intentFilter = IntentFilter()
        this.intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        this.intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        this.intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        this.intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    fun exchange(from: String, to: String, data: ByteArray): ByteArray {
        val fromHash = hashUserId(from)
        val fromTo = hashUserId(to)

        return if (fromHash > fromTo) this.openService(from, to)
        else if (fromHash < fromTo) this.connectToService(from, to)
        else throw Error("Same hash!")
    }

    private fun openService(from: String, to: String): ByteArray {
        val record: Map<String, String> = mapOf(
            "username" to "testing-123"
        )

        val name = this.getRecordName(from)
        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(
            name,
            SERVICE_TYPE,
            record,
        )

        manager.addLocalService(channel, serviceInfo, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Registered service \"$name\"")
            }

            override fun onFailure(arg0: Int) {
                // TODO: Handle this properly
                Log.d(TAG, "Failed to register service")
            }
        })

        manager.startListening(channel, null)

        ServerSocket().use { server ->
            server.bind(
                InetSocketAddress(
                    // TODO: Only bind on IP for wifi direct IP!
                    Inet4Address.getByName("0.0.0.0"),
                    PORT,
                )
            )
            Log.d(TAG, "TCP server listening on port $PORT")

            server.accept().use { socket ->
                Log.d(TAG, "Device connected to TCP server!")
            }
        }

        return ByteArray(0)
    }

    private fun connectToService(from: String, to: String): ByteArray {
        var address: String? = null
        var records: Map<String, String>? = null
        val lookingForDomain = "${getRecordName(to)}.${SERVICE_TYPE}.local."

        val serviceListener =
            WifiP2pManager.DnsSdServiceResponseListener { instanceName, domain, device ->
                Log.d(TAG, "Found service \"$domain\" from device ${device.deviceAddress}")
            }

        val txtListener = WifiP2pManager.DnsSdTxtRecordListener { domain, record, device ->
            Log.d(
                TAG,
                "Found TXT records for service \"$domain\" from device ${device.deviceAddress}"
            )

            if (domain == lookingForDomain) {
                address = device.deviceAddress
                records = record
            }
        }

        manager.setDnsSdResponseListeners(channel, serviceListener, txtListener)
        manager.startListening(channel, null)

        val timeoutTime = Instant.now().plus(30, ChronoUnit.SECONDS)
        while (true) {
            val serviceRequest = WifiP2pDnsSdServiceRequest.newInstance()
            manager.addServiceRequest(
                channel,
                serviceRequest,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.i("", "Added service request")

                        manager.discoverServices(
                            channel,
                            object : WifiP2pManager.ActionListener {
                                override fun onSuccess() {
                                    Log.i("", "Discovered services")
                                }

                                override fun onFailure(code: Int) {
                                    Log.i("", "Wi-Fi Direct isn't supported on this device.")
                                }
                            }
                        )
                    }

                    override fun onFailure(code: Int) {
                        Log.i("", "Failed to add service request")
                    }
                }
            )

            if (Instant.now().isAfter(timeoutTime)) {
                throw Error("Timeout for wifi direction connection reached")
            }

            if (address != null && records != null) {
                break
            }

            Thread.sleep(1_000)
        }

        val config = WifiP2pConfig()
        config.deviceAddress = address
        config.wps.setup = WpsInfo.PBC

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                Log.d(TAG, "Devices connected!")
            }

            override fun onFailure(reason: Int) {

            }
        })

        return ByteArray(0)
    }

    private fun hashUserId(id: String): Int {
        var total = 0
        for (char in id.chars()) {
            total += char
        }

        return total
    }

    private fun getRecordName(hash: String): String {
        return "_$hash"
    }

    fun onPause() {
        activity.unregisterReceiver(broadcastReceiver)
    }

    fun onResume() {
        activity.registerReceiver(broadcastReceiver, intentFilter)
    }
}
