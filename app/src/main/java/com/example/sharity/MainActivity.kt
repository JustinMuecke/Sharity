package com.example.sharity

import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import com.example.sharity.data.device.MP3Indexer
import com.example.sharity.data.wrapper.Database
import androidx.lifecycle.lifecycleScope
import com.example.sharity.data.device.NfcClient
import com.example.sharity.data.local.PrimaryUser
import com.example.sharity.data.wrapper.Database
import com.example.sharity.data.wrapper.NfcController
import com.example.sharity.ui.feature.ProfileScreen
import com.example.sharity.ui.feature.homescreen.HomeScreen
import com.example.sharity.ui.feature.homescreen.HomeScreenViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.example.sharity.ui.theme.SharityTheme
import kotlinx.coroutines.launch


import androidx.compose.runtime.collectAsState
import com.example.sharity.ui.feature.peersongs.PeerSongsScreen
import com.example.sharity.ui.feature.peersongs.PeerSongsViewModel


enum class RootScreen { HOME, PROFILE, PEER_SONGS }

// FIXME: Move to viewmodel, to prevent Memoryleak
private lateinit var nfcController: NfcController
private val nfcClient = NfcClient()

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalUuidApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        nfcController = NfcController(this) { tag ->
            logNfcMessages(tag)
        }
        val db = Database.createDatabaseConnector(this.applicationContext)
        val exoPlayer = ExoPlayer.Builder(applicationContext).build()

        Thread {
            try {
                db.userInfoDao().createUuidIfEmpty(Uuid.random().toHexString())
                PrimaryUser.init(db, "user1", "some_font")
                val indexer =
                    MP3Indexer(applicationContext, db, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                val indexer = MP3Indexer(
                    applicationContext,
                    db,
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                )
                indexer.index()
            } catch (e: Exception) {
                Log.e("ERROR", "MP3Indexer failed", e)
            }
        }.start()
        
        setContent {
            SharityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    var currentScreen by remember { mutableStateOf(RootScreen.HOME) }

                    val homeViewModel = viewModel<HomeScreenViewModel>(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return HomeScreenViewModel(db, exoPlayer) as T
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

    private fun logNfcMessages(tag: Tag) {
        lifecycleScope.launch {
            nfcClient.fetchProfile(tag)
                .onSuccess { bytes ->
                    Log.e("NFC", "Profile bytes size = ${bytes.size}")

                    val profileString = String(bytes, Charsets.UTF_8)
                    Log.e("NFC", "Profile STRING = '$profileString'")

                    val hex = bytes.joinToString(" ") { "%02X".format(it) }
                    Log.e("NFC", "Profile HEX = $hex")
                }
                .onFailure {
                    Log.e("NFC", "Profile exchange failed", it)
                }
        }
    }
}
