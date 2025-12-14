package com.example.sharity

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
import com.example.sharity.data.wrapper.NfcBlocker
import com.example.sharity.domain.model.Connection
import com.example.sharity.domain.model.toNfcPayload
import com.example.sharity.ui.feature.peersongs.PeerSongsScreen
import com.example.sharity.ui.feature.peersongs.PeerSongsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
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

        val exoPlayer = ExoPlayer.Builder(applicationContext).build()

        initPermissions()
        initAppData()

        nfcController = NfcController(this) { tag ->
            logNfcMessages(tag)
        }

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

    private fun onTransferFinished() {
        Log.d("TRANSFER", "Cleaning up after transfer")

        if (::wifiHandshake.isInitialized) {
            wifiHandshake.disconnect()
        }
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
                val db = db()
                val repo = userRepo()

                myUuid = db.userInfoDao().getValue("uuid")
                    ?: Uuid.random().toHexString().also { newUuid ->
                        db.userInfoDao().createUuidIfEmpty(newUuid)
                    }


                val indexer = MP3Indexer(applicationContext, db(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                indexer.index()

                NfcPayloadCache.update(
                    repo.getProfile().toNfcPayload()
                )

                val payload = repo.getProfile().toNfcPayload()

                require(payload.isNotEmpty()) {
                    "NFC payload is empty!"
                }

                NfcPayloadCache.update(payload)

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
        lifecycleScope.launch(Dispatchers.IO) {
            if (!NfcBlocker.canStartHandshake()) {
                Log.d("NFC", "Handshake blocked (cooldown)")
                return@launch
            }


            nfcClient.fetchProfile(tag)
                .onSuccess { bytes ->
                    NfcBlocker.markHandshakeStarted()
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
    private fun handleFileTransfer(
        hostAddress: String,
        port: Int,
        isGroupOwner: Boolean
    ) {
        lifecycleScope.launch(Dispatchers.IO) {

            handleFileTransferOnce(
                hostAddress,
                8888,
                isGroupOwner,
                sendFiles = !isGroupOwner
            )

            delay(500)

            handleFileTransferOnce(
                hostAddress,
                8889,
                isGroupOwner,
                sendFiles = isGroupOwner
            )

            val indexer = MP3Indexer(applicationContext, db(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            indexer.index()
            Log.d("TRANSFER", "TRANSFER DONE")
            onTransferFinished()
        }
    }


    private suspend fun handleFileTransferOnce(
        hostAddress: String,
        port: Int,
        isGroupOwner: Boolean,
        sendFiles: Boolean
    ) {
        val fileTransfer = FileTransferService()

        if (sendFiles) {
            val filesToSend = db().trackDao().getAll()?.map { track ->
                applicationContext.uriToTempFile(Uri.parse(track.contentUri))
            }

            if (filesToSend == null || filesToSend.isEmpty()) {
                return
            }

            fileTransfer.sendFiles(hostAddress, port, filesToSend)
            filesToSend.forEach { it.delete() }
        } else {
            val result = withTimeoutOrNull(10_000) {
                fileTransfer.receiveFiles(port, filesDir)
                    .onSuccess { receivedFiles ->
                        receivedFiles?.forEach { receivedFile ->
                            Log.d("TRANSFER", "File received: ${receivedFile.absolutePath}")

                            val mediaUri = applicationContext
                                .saveAudioToMediaStore(receivedFile)

                            Log.d("MEDIA", "Saved to MediaStore: $mediaUri")

                            receivedFile.delete()
                        }
                    }

            }

            if (result == null) {
                Log.d("TRANSFER", "Receive timed out after 10s")
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

    fun Context.uriToTempFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open URI: $uri")

        val tempFile = File.createTempFile(
            "share_",
            ".mp3",
            cacheDir
        )

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }


    fun Context.saveAudioToMediaStore(
        sourceFile: File,
        displayName: String = sourceFile.name
    ): Uri? {
        val resolver = contentResolver

        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            put(
                MediaStore.Audio.Media.RELATIVE_PATH,
                Environment.DIRECTORY_MUSIC
            )
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val uri = resolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: return null

        resolver.openOutputStream(uri)?.use { out ->
            sourceFile.inputStream().use { input ->
                input.copyTo(out)
            }
        }

        values.clear()
        values.put(MediaStore.Audio.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        return uri
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