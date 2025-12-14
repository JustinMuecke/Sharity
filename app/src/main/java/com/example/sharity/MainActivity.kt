package com.example.sharity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink // Needed for NFC Deep Link fix
import com.example.sharity.data.device.MP3Indexer
import com.example.sharity.data.device.NfcClient
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.data.wrapper.db
import com.example.sharity.data.wrapper.userRepo
import com.example.sharity.data.wrapper.NfcController
import com.example.sharity.domain.model.Connection
import com.example.sharity.ui.component.AudioControl
import com.example.sharity.ui.component.navBar.NavBar
import com.example.sharity.ui.component.playlist.SongSelectorModalContent // Assuming this is correct
import com.example.sharity.ui.component.share.PeerMiniProfileOverlay
import com.example.sharity.ui.feature.ProfileScreen
import com.example.sharity.ui.feature.allsongsscreen.AllSongsView
import com.example.sharity.ui.feature.allsongsscreen.AllSongsViewModel
import com.example.sharity.ui.feature.friends.FriendsScreen
import com.example.sharity.ui.feature.friends.FriendsViewModel
import com.example.sharity.ui.feature.history.HistoryScreen
import com.example.sharity.ui.feature.history.HistoryViewModel
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
import com.example.sharity.ui.feature.peersongs.PeerSongsScreen
import com.example.sharity.ui.feature.peersongs.PeerSongsViewModel
import com.example.sharity.ui.feature.playlistscreen.PlaylistView
import com.example.sharity.ui.feature.playlistscreen.PlaylistViewModelFactory
import com.example.sharity.ui.feature.playlistselection.PlaylistSelectionScreen
import com.example.sharity.ui.feature.playlistselection.PlaylistSelectionViewModel
import com.example.sharity.ui.feature.playlistscreen.PlaylistViewModel
import com.example.sharity.ui.theme.SharityTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object RootDestinations {
    // ... (All your destination objects remain the same)
    const val HOME = "home"
    const val PROFILE = "profile"
    const val NFC = "nfc" // Destination for NFC handling
    const val HISTORY = "history"
    const val FRIENDS = "friends"
    const val PLAYLISTS = "playlists" // Playlist Selection Screen
    const val PLAYLIST_VIEW = "playlist_view/{playlistId}" // Individual Playlist View
    const val PEER = "peer"
    const val ALL_SONGS = "all_songs"
}

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

    @OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
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
            val navController = rememberNavController()

            // --- MODAL STATE SETUP ---
            val showCreatePlaylistModal = rememberSaveable { mutableStateOf(false) }
            val scope = rememberCoroutineScope() // Should be here in setContent
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            // -------------------------

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val showBackButton = navController.previousBackStackEntry != null
            val currentRoute = navBackStackEntry?.destination?.route

            // --- VIEW MODEL SETUP ---
            val allSongsViewModel = viewModel<AllSongsViewModel>(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AllSongsViewModel(db, exoPlayer) as T
                    }
                }
            )
            val playlistSelectionViewModel = viewModel<PlaylistSelectionViewModel>(
                factory = object : ViewModelProvider.Factory {
                    override fun <T: ViewModel> create(modelClass: Class<T>):T {
                        @Suppress("UNCHECKED_CAST") // Added cast suppression for safety
                        return PlaylistSelectionViewModel(db) as T
                    }
                }
            )


            // ------------------------

            SharityTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        val screenTitle = when (currentRoute) { // Use currentRoute here
                            RootDestinations.HOME -> "My Music Library"
                            // Add more cases for other routes
                            else -> "Sharity"
                        }
                        NavBar(
                            showBack = showBackButton,
                            onBackClick = { navController.navigateUp() },
                            onNfcClick = { navController.navigate(RootDestinations.NFC) },
                            onProfileClick = { navController.navigate(RootDestinations.PROFILE) },
                            onOpenPeer = { /* maybe unused now */ }
                        )
                    },
                    bottomBar = {
                        if (allSongsViewModel.currentTrack.collectAsState().value != null) {
                            AudioControl(allSongsViewModel)
                        }
                        // Add your persistent navigation bar here if it's not in topBar
                        // NavBar is in topBar, so this is likely just the AudioControl
                    }
                ) { innerPadding -> // innerPadding handles topBar and bottomBar

                    // 1. NAV HOST (The main screen content)
                    NavHost(
                        navController = navController,
                        startDestination = RootDestinations.HOME,
                        modifier = Modifier.fillMaxSize() // Fill the space provided by Scaffold
                    ) {

                        // HOME
                        composable(RootDestinations.HOME) {
                            HomeScreen(
                                // Pass the required padding to the content's LazyColumn/Container
                                paddingValues = innerPadding,
                                db = db,
                                exoPlayer = exoPlayer,
                                allSongsViewModel = allSongsViewModel,
                                onProfileClick = { navController.navigate(RootDestinations.PROFILE) },
                                onHistoryClick = { navController.navigate(RootDestinations.HISTORY) },
                                onFriendsClick = { navController.navigate(RootDestinations.FRIENDS) },
                                onPlaylistsClick = { navController.navigate(RootDestinations.PLAYLISTS) },
                                onListClick = { /* no-op */ },
                                onOpenPeer = {navController.navigate(RootDestinations.PEER)},
                                onAllsongsClick = { navController.navigate(RootDestinations.ALL_SONGS)},
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable(RootDestinations.ALL_SONGS) {
                            AllSongsView(
                                viewModel = allSongsViewModel,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        composable(RootDestinations.PROFILE) {
                            // Create your profile screen here
                            ProfileScreen(
                                paddingValues = innerPadding,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // PLAYLIST SELECTION
                        composable(RootDestinations.PLAYLISTS) {
                            PlaylistSelectionScreen(
                                onViewPlaylist = { playlistId ->
                                    navController.navigate("playlist_view/$playlistId")
                                },
                                // Pass the actual ViewModel instance
                                viewModel = playlistSelectionViewModel,
                                // Pass the handler to open the modal
                                onCreateNewPlaylist = { showCreatePlaylistModal.value = true },
                                // Pass innerPadding, but PlaylistSelectionScreen should handle it.
                                // If your screen uses a LazyColumn, you need to be careful with padding.
                                paddingValues = innerPadding,
                                modifier = Modifier.fillMaxSize()
                            )
                        }


                        // PEER/NFC SCREEN (Added Deep Link fix here)
                        composable(
                            route = RootDestinations.NFC,
                            deepLinks = listOf(
                                navDeepLink { uriPattern = "android-app://androidx.navigation/nfc" }
                            )
                        ) {
                            val peerSongsViewModel = viewModel<PeerSongsViewModel>()
                            val state = peerSongsViewModel.uiState.collectAsState().value
                            /*
                            PeerSongsScreen(
                                peerName = state.peerName,
                                tracks = state.tracks,
                                selectedTrackUris = state.selectedTrackUris,
                                onToggleSelect = { track -> peerSongsViewModel.toggleSelect(track) },
                                onCancel = {
                                    peerSongsViewModel.clearSelection()
                                    navController.navigateUp()
                                },
                                onFinished = {
                                    val selected = peerSongsViewModel.getSelectedTracks()
                                    peerSongsViewModel.clearSelection()
                                    navController.navigateUp()
                                },
                                */
                            PeerMiniProfileOverlay(
                                false,
                                null,
                                onDismiss = {},
                                onOpenPeer = {},
                                modifier = Modifier.fillMaxSize()
                            )

                        }
                        composable(RootDestinations.FRIENDS) {
                            val friendsViewModel = viewModel<FriendsViewModel>(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        @Suppress("UNCHECKED_CAST")
                                        return FriendsViewModel(db) as T
                                    }
                                }
                            )

                            FriendsScreen(
                                viewModel = friendsViewModel,
                                paddingValues = innerPadding,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable(RootDestinations.HISTORY) {
                            val historyViewModel = viewModel<HistoryViewModel>(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        @Suppress("UNCHECKED_CAST")
                                        return HistoryViewModel(db) as T
                                    }
                                }
                            )

                            HistoryScreen(
                                viewModel = historyViewModel,
                                paddingValues = innerPadding,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // ... (PEER destination logic can be simplified if it routes to NFC)
                        composable(route= RootDestinations.PEER) {
                            // If PEER is the same as NFC for now, you can just navigate:
                            navController.navigate(RootDestinations.NFC)
                        }

                        composable(
                            route = "playlist_view/{playlistId}",
                            arguments = listOf(
                                navArgument("playlistId") {
                                    type = NavType.IntType  // Specify it's an Int, not a String
                                }
                            )
                        ) { backStackEntry ->
                            val playlistId = backStackEntry.arguments?.getInt("playlistId") ?: 0

                            val playlistViewModel = viewModel<PlaylistViewModel>(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        @Suppress("UNCHECKED_CAST")
                                        return PlaylistViewModel(db, playlistId) as T
                                    }
                                }
                            )
                            PlaylistView(
                                playlistId = playlistId,
                                onSongClick = { clickedSong ->
                                    val playlistTrackList = playlistViewModel.currentPlaylistTracks
                                    allSongsViewModel.selectTrackInContext(clickedSong, playlistTrackList)
                                },
                                viewModel = playlistViewModel,
                                paddingValues = innerPadding
                            )

                        } // End NavHost
                } // End Scaffold Content Lambda

                // --- 2. MODAL BOTTOM SHEET (Sits above the Scaffold) ---
                if (showCreatePlaylistModal.value) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion { showCreatePlaylistModal.value = false }
                        },
                        sheetState = sheetState,
                    ) {
                        SongSelectorModalContent(
                            allSongsViewModel = allSongsViewModel,
                            onClose = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showCreatePlaylistModal.value = false
                                }
                            },
                            onPlaylistCreated = {name, selectedSongs ->
                                // 1. Launch a coroutine for the suspend function call
                                scope.launch {
                                    // 2. Call the ViewModel to save the playlist and get the real ID
                                    // NOTE: The name "New Playlist" is a placeholder. You'll need to collect a name from the UI later.
                                    val newPlaylistId = playlistSelectionViewModel.createPlaylistWithTracks(
                                        name = name,
                                        tracks = selectedSongs // Assuming 'selectedSongs' are actually 'Track' objects
                                    )
                                    Log.e("MainActivity", "Created playlist with ID: $newPlaylistId")
                                    Log.e("MainActivity", "Navigating to: playlist_view/$newPlaylistId")
                                    // Dismiss Modal first
                                    sheetState.hide()

                                    // 3. Navigation after dismissal
                                    if (!sheetState.isVisible) {
                                        showCreatePlaylistModal.value = false
                                        // **NAVIGATE TO THE REAL NEW PLAYLIST ID**
                                        navController.navigate("playlist_view/$newPlaylistId")
                                        }
                                    }
                                }
                            )
                        }
                    } // End ModalBottomSheet conditional
                } // End SharityTheme
            } // End setContent
        }   }// End onCreate
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
            val result = withTimeoutOrNull(2_000) {
                fileTransfer.receiveFiles(port, filesDir)
            }

            if (result == null) {
                Log.d("TRANSFER", "Receive timed out after 2s")
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
    // ... (onResume, onPause, onNewIntent, logNfcMessages remain the same)
}
suspend fun generateTestConnections(db: AppDatabase) {
    withContext(Dispatchers.IO) {
        val connectionDao = db.connectionDao()

        val names = listOf(
            "Alice", "Bob", "Charlie", "Diana", "Eve",
            "Frank", "Grace", "Henry", "Ivy", "Jack"
        )

        val connections = mutableListOf<Connection>()

        // Generate 30 test connections
        for (i in 1..30) {
            val randomName = names.random()
            val uuid = "uuid-$randomName-${Random.nextInt(1000)}"

            connections.add(
                Connection(
                    connectionID = i,
                    username = randomName,
                    connectionUuid = uuid,
                    tracksSent = Random.nextInt(0, 20),
                    tracksReceived = Random.nextInt(0, 20)
                )
            )
        }

        connectionDao.insertAll(*connections.toTypedArray())
    }
}

// Then call it from your MainActivity's onCreat
