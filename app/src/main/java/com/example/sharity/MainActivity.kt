package com.example.sharity

import android.content.Intent
import android.nfc.Tag
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import com.example.sharity.data.device.MP3Indexer

import com.example.sharity.data.wrapper.Database
import androidx.lifecycle.lifecycleScope
import com.example.sharity.data.device.NfcClient
import com.example.sharity.data.local.PrimaryUser
import com.example.sharity.data.wrapper.NfcController
import kotlinx.coroutines.launch
import com.example.sharity.ui.theme.SharityTheme
import com.example.sharity.data.local.Database
import com.example.sharity.ui.feature.ProfileScreen
import com.example.sharity.ui.feature.homescreen.HomeScreen
import com.example.sharity.ui.feature.homescreen.HomeScreenViewModel
import com.example.sharity.ui.theme.SharityTheme

enum class RootScreen { HOME, PROFILE }

private lateinit var nfcController: NfcController
private val nfcClient = NfcClient()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userInstance = PrimaryUser.init(this.contentResolver, "user1", "some_font")
        nfcController = NfcController(this) { tag ->
            logNfcMessages(tag)
        }

        val db = Database.createDatabaseConnector(this.applicationContext)
        val exoPlayer = ExoPlayer.Builder(applicationContext).build()

        Thread({
            try {
                val indexer = MP3Indexer(applicationContext, db, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                indexer.index()
            } catch (e: Exception) {
                // TODO: Handle error!
                Log.e("ERROR", "", e)
            }
        }).start()
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
                                onProfileClick = { currentScreen = RootScreen.PROFILE }
                            )
                        }

                        RootScreen.PROFILE -> {
                            ProfileScreen(
                                modifier = Modifier.padding(innerPadding),
                                onBackClick = { currentScreen = RootScreen.HOME }
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
