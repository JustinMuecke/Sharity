package com.example.sharity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import com.example.sharity.data.device.MP3Indexer
import com.example.sharity.data.device.NfcClient
import com.example.sharity.data.wrapper.db
import com.example.sharity.data.wrapper.userRepo
import com.example.sharity.data.wrapper.NfcController
import com.example.sharity.ui.feature.ProfileScreen
import com.example.sharity.ui.feature.homescreen.HomeScreen
import com.example.sharity.ui.feature.homescreen.HomeScreenViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.example.sharity.ui.theme.SharityTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.sharity.data.device.NfcProfileService
import com.example.sharity.data.device.WifiDirectHandshake
import com.example.sharity.data.local.FileTransferService
import com.example.sharity.data.local.HandshakeData
import com.example.sharity.data.device.NfcPayloadCache
import com.example.sharity.data.local.parseHandshake
import com.example.sharity.domain.model.toNfcPayload
import com.example.sharity.ui.feature.peersongs.PeerSongsScreen
import com.example.sharity.ui.feature.peersongs.PeerSongsViewModel
import kotlinx.coroutines.Dispatchers
import java.io.File


enum class RootScreen { HOME, PROFILE, PEER_SONGS }

// FIXME: Move to viewmodel, to prevent Memory leak
private lateinit var nfcController: NfcController
private lateinit var wifiHandshake: WifiDirectHandshake
private val nfcClient = NfcClient()
private var myUuid: String = ""
class MainActivity : ComponentActivity() {

    private val nfcTransactionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == NfcProfileService.ACTION_NFC_TRANSACTION_COMPLETE) {
                onNfcServerTransactionComplete()
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            nfcTransactionReceiver,
            IntentFilter(NfcProfileService.ACTION_NFC_TRANSACTION_COMPLETE)
        )

        nfcController = NfcController(this) { tag ->
            logNfcMessages(tag)
        }

        val exoPlayer = ExoPlayer.Builder(applicationContext).build()

        initAppData()


        setContent {
            SharityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    var currentScreen by remember { mutableStateOf(RootScreen.HOME) }

                    val homeViewModel = viewModel<HomeScreenViewModel>(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return HomeScreenViewModel(db(), exoPlayer) as T
                            }
                        }
                    )

                    when (currentScreen) {

                        RootScreen.HOME -> {
                            HomeScreen(
                                viewModel = homeViewModel,
                                modifier = Modifier.padding(innerPadding),
                                onProfileClick = { currentScreen = RootScreen.PROFILE },
                                onOpenPeer = { currentScreen = RootScreen.PEER_SONGS }
                            )
                        }

                        RootScreen.PROFILE -> {
                            ProfileScreen(
                                modifier = Modifier.padding(innerPadding),
                                onBackClick = { currentScreen = RootScreen.HOME },
                                onOpenPeer = { currentScreen = RootScreen.PEER_SONGS }
                            )
                        }

                        RootScreen.PEER_SONGS -> {
                            val peerSongsViewModel = viewModel<PeerSongsViewModel>()
                            val state = peerSongsViewModel.uiState.collectAsState().value

                            PeerSongsScreen(
                                peerName = state.peerName,
                                tracks = state.tracks,
                                selectedTrackUris = state.selectedTrackUris,
                                onToggleSelect = { track -> peerSongsViewModel.toggleSelect(track) },
                                onCancel = {
                                    peerSongsViewModel.clearSelection()
                                    currentScreen = RootScreen.HOME
                                },
                                onFinished = {
                                    // TODO: next step = navigate to Trade/Confirm screen
                                    // For now: keep it simple and go back home
                                    val selected = peerSongsViewModel.getSelectedTracks()
                                    // later: pass selected to next screen
                                    peerSongsViewModel.clearSelection()
                                    currentScreen = RootScreen.HOME
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcController.onResume()
    }

    override fun onPause() {
        super.onPause()
        nfcController.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nfcController.onNewIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(nfcTransactionReceiver)
        if (::wifiHandshake.isInitialized) {
            wifiHandshake.unregister()
            wifiHandshake.disconnect()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun initAppData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = applicationContext.db()
                val repo = applicationContext.userRepo()

                myUuid = db.userInfoDao()
                    .getValue("uuid")
                    ?: Uuid.random().toHexString().also {
                        db.userInfoDao().createValueIfEmpty(it)
                    }

                MP3Indexer(
                    applicationContext,
                    db,
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                ).index()

                NfcPayloadCache.update(
                    repo.getProfile().toNfcPayload()
                )

                Log.d("INIT", "My UUID = $myUuid")

            } catch (e: Exception) {
                Log.e("INIT", "Initialization failed", e)
            }
        }
    }


    private fun onNfcServerTransactionComplete() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("NFC", "=== NFC SERVER: Waiting for connection ===")

                val dummyHandshake = HandshakeData(
                    peerUuid = "",
                    name = "",
                    token = "",
                    port = 8888,
                    font = ""
                )

                wifiHandshake = WifiDirectHandshake(this@MainActivity, myUuid) {
                    hostAddress, port, isGroupOwner ->
                    Log.d("WIFI", "Connection callback (NFC Server - was RESPONDER)")
                    handleFileTransfer(hostAddress, port, isGroupOwner)
                }

                logUuidState("NFC SERVER (RESPONDER)", myUuid, dummyHandshake.peerUuid)

                wifiHandshake.start(dummyHandshake, forceInitiator = false)

            } catch (e: Exception) {
                Log.e("WIFI", "Failed to start WiFi Direct from NFC server", e)
            }
        }
    }

    private fun logNfcMessages(tag: Tag) {
        lifecycleScope.launch {
            nfcClient.fetchProfile(tag)
                .onSuccess { bytes ->
                    val handshake = parseHandshake(bytes)
                    logUuidState("NFC CLIENT (RESPONDER)", myUuid, handshake.peerUuid)


                    wifiHandshake = WifiDirectHandshake(this@MainActivity, myUuid) {
                        hostAddress, port, isGroupOwner ->
                        Log.d("WIFI", "Connection callback (NFC Client - was INITIATOR)")
                        handleFileTransfer(hostAddress, port, isGroupOwner)
                    }
                    wifiHandshake.start(handshake, forceInitiator = true)
                }
                .onFailure { exception ->
                    Log.e("NFC", "Profile exchange failed", exception)
                }
        }
    }
    private fun handleFileTransfer(hostAddress: String, port: Int, isGroupOwner: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            val fileTransfer = FileTransferService()

            if (isGroupOwner) {
                fileTransfer.receiveFile(port, filesDir
                )
                    .onSuccess { receivedFile ->
                        Log.d("TRANSFER", "File received: ${receivedFile.absolutePath}")
                    }
                    .onFailure { exception ->
                        Log.e("TRANSFER", "Failed to receive file", exception)
                    }
            } else {
                val fileToSend = File(filesDir, "example.mp3")
                if (!fileToSend.exists()) {
                    fileToSend.writeText("Test file content - UUID: $myUuid")
                }

                kotlinx.coroutines.delay(1000)

                fileTransfer.sendFile(hostAddress, port, fileToSend)
                    .onSuccess { Log.d("TRANSFER", "File sent successfully!") }
                    .onFailure { Log.e("TRANSFER", "Failed to send file", it) }
            }
        }
    }

    private fun initPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.NEARBY_WIFI_DEVICES),
                1001
            )
        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }
}

private fun logUuidState(
    context: String,
    myUuid: String,
    peerUuid: String?
) {
    Log.d("UUID_TEST", "===== UUID CHECK ($context) =====")
    Log.d("UUID_TEST", "My UUID   : $myUuid")
    Log.d("UUID_TEST", "Peer UUID : ${peerUuid ?: "NULL"}")
    Log.d(
        "UUID_TEST",
        "Same UUID?: ${peerUuid != null && myUuid == peerUuid}"
    )
    Log.d("UUID_TEST", "================================")
}