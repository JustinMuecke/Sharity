package com.example.sharity.data.device

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.sharity.data.local.HandshakeData
import com.example.sharity.data.wrapper.db
import com.example.sharity.domain.model.Connection

class WifiDirectHandshake(
    private val context: Context,
    private val myUuid: String,
    private val onConnectionEstablished: (hostAddress: String, port: Int, isGroupOwner: Boolean) -> Unit
) {
    public var deviceAddress: String = ""
    private val manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = manager.initialize(context, context.mainLooper, null)
    private var handshake: HandshakeData? = null
    private var isInitiator = false
    private var isConnected = false
    private var connectionAttempted = false

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun start(handshakeData: HandshakeData, forceInitiator: Boolean = false) {
        handshake = handshakeData

        isInitiator = when {
            forceInitiator -> true
            handshakeData.peerUuid == null -> false
            else -> myUuid < handshakeData.peerUuid
        }

        registerReceiver()
        discoverPeers()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun discoverPeers() {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WIFI", "Peer discovery started")
            }

            override fun onFailure(reason: Int) {
                Log.e("WIFI", "Discovery failed: $reason")
            }
        })
    }

    private val receiver = object : BroadcastReceiver() {
        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {

                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION ->
                    handleStateChanged(intent)

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION ->
                    handlePeersChanged()

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION ->
                    handleConnectionChanged()

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION ->
                    handleThisDeviceChanged(intent)
            }
        }
    }

    private fun handleStateChanged(intent: Intent) {
        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
        val enabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
        Log.d("WIFI", "WiFi P2P enabled: $enabled")
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun handlePeersChanged() {
        if (isConnected) {
            Log.d("WIFI", "Already connected, ignoring peers")
            return
        }

        manager.requestPeers(channel) { peers ->
            logPeers(peers.deviceList)

            if (!isInitiator) {
                Log.d("WIFI", "RESPONDER: waiting for incoming connection")
                return@requestPeers
            }

            if (connectionAttempted) {
                Log.d("WIFI", "INITIATOR: already attempted connection")
                return@requestPeers
            }

            val target = selectTargetPeer(peers.deviceList)

            if (target != null) {
                Log.d("WIFI", "INITIATOR: connecting to ${target.deviceName}")
                connectionAttempted = true
                connectToPeer(target)
            } else {
                Log.w("WIFI", "INITIATOR: no suitable peer found")
            }
        }
    }

    private fun logPeers(devices: Collection<WifiP2pDevice>) {
        Log.d("WIFI", "Peers found: ${devices.size}")
        devices.forEach {
            Log.d("WIFI", "Peer: ${it.deviceName} - ${it.deviceAddress}")

            Thread {
                context.db().connectionDao().insertAll(Connection(username = it.deviceName, connectionUuid = it.deviceAddress))
            }.start()
            deviceAddress = it.deviceAddress
        }
    }

    private fun selectTargetPeer(devices: Collection<WifiP2pDevice>): WifiP2pDevice? {
        if (devices.size == 1) return devices.first()

        val hs = handshake ?: return null
        val peerUuid = hs.peerUuid ?: return null

        return devices.firstOrNull { device ->
            device.deviceName.contains(peerUuid, ignoreCase = true) ||
                    device.deviceName.contains(hs.name, ignoreCase = true) ||
                    device.deviceAddress.contains(peerUuid, ignoreCase = true)
        }
    }

    private fun handleConnectionChanged() {
        manager.requestConnectionInfo(channel) { info ->

            if (!info.groupFormed) {
                Log.d("WIFI", "Group not formed yet")
                isConnected = false
                return@requestConnectionInfo
            }

            if (isConnected) return@requestConnectionInfo

            isConnected = true

            val host = info.groupOwnerAddress?.hostAddress
            val port = handshake?.port ?: 8888
            val isGroupOwner = info.isGroupOwner

            stopDiscovery()

            host?.let {
                onConnectionEstablished(it, port, isGroupOwner)
            } ?: Log.e("WIFI", "Host address is null")
        }
    }

    private fun stopDiscovery() {
        manager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WIFI", "Peer discovery stopped")
            }

            override fun onFailure(reason: Int) {
                Log.w("WIFI", "Failed to stop discovery: $reason")
            }
        })
    }

    private fun handleThisDeviceChanged(intent: Intent) {
        val device = intent.getParcelableExtra<WifiP2pDevice>(
            WifiP2pManager.EXTRA_WIFI_P2P_DEVICE
        )
        Log.d("WIFI", "This device changed: ${device?.deviceName}")
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun connectToPeer(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
            groupOwnerIntent = 0
        }

        Log.d("WIFI", "INITIATOR: Attempting to connect to ${device.deviceName}...")

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WIFI", "INITIATOR: Connection request sent successfully")
            }

            override fun onFailure(reason: Int) {
                val reasonText = when (reason) {
                    WifiP2pManager.ERROR -> "ERROR"
                    WifiP2pManager.P2P_UNSUPPORTED -> "P2P_UNSUPPORTED"
                    WifiP2pManager.BUSY -> "BUSY"
                    else -> "UNKNOWN ($reason)"
                }
                Log.e("WIFI", "INITIATOR: Connection failed: $reasonText")

                // If busy, retry only once after a delay
                if (reason == WifiP2pManager.BUSY && !isConnected) {
                    Log.d("WIFI", "INITIATOR: Retrying connection in 2 seconds...")
                    Handler(Looper.getMainLooper()).postDelayed(@androidx.annotation.RequiresPermission(
                        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.NEARBY_WIFI_DEVICES]
                    ) {
                        if (!isConnected && !connectionAttempted) {
                            connectionAttempted = true
                            connectToPeer(device)
                        }
                    }, 1000)
                } else {
                    connectionAttempted = false
                }
            }
        })
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        context.registerReceiver(receiver, filter)
    }

    fun disconnect() {
        try {
            manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("WIFI", "WiFi P2P group removed")
                }

                override fun onFailure(reason: Int) {
                    Log.w("WIFI", "Failed to remove group: $reason")
                }
            })
        } catch (e: Exception) {
            Log.e("WIFI", "Disconnect failed", e)
        }
    }


    fun unregister() {
        try {
            context.unregisterReceiver(receiver)
            Log.d("WIFI", "Receiver unregistered")
        } catch (e: IllegalArgumentException) {
            Log.w("WIFI", "Receiver already unregistered")
        }
    }
}