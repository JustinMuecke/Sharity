package com.example.sharity

import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pManager
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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import com.example.sharity.data.device.MP3Indexer
import com.example.sharity.data.device.NfcClient
import com.example.sharity.data.local.PrimaryUser
import com.example.sharity.data.wrapper.Database
import com.example.sharity.data.wrapper.FileTransferController
import com.example.sharity.data.wrapper.NfcController
import com.example.sharity.ui.feature.ProfileScreen
import com.example.sharity.ui.feature.homescreen.HomeScreen
import com.example.sharity.ui.feature.homescreen.HomeScreenViewModel
import com.example.sharity.ui.theme.SharityTheme
import kotlinx.coroutines.launch

enum class RootScreen { HOME, PROFILE }

private lateinit var nfcController: NfcController
private val nfcClient = NfcClient()

class MainActivity : ComponentActivity() {

    private lateinit var fileTransferController: FileTransferController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (android.os.Build.MODEL == "Pixel 4a") {
            Thread({
                val userInstance = PrimaryUser.init(this.contentResolver, "b", "some_font")
                fileTransferController = FileTransferController(this)
                fileTransferController.exchange("b", "a", ByteArray(0))
            }).start()
        } else {
            Thread({
                val userInstance = PrimaryUser.init(this.contentResolver, "a", "some_font")
                fileTransferController = FileTransferController(this)
                fileTransferController.exchange("a", "b", ByteArray(0))
            }).start()
        }

        // Wait for FTC to initialize
        Thread.sleep(500)

        nfcController = NfcController(this) { tag ->
            logNfcMessages(tag)
        }

        val db = Database.createDatabaseConnector(this.applicationContext)
        val exoPlayer = ExoPlayer.Builder(applicationContext).build()

        Thread({
            try {
                val indexer =
                    MP3Indexer(applicationContext, db, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                indexer.index()
            } catch (e: Exception) {
                // TODO: Handle error!
                Log.e("ERROR", "", e)
            }
        }).start()

        Log.i("", "Model: ${android.os.Build.MODEL}")

        /*
        val record: Map<String, String> = mapOf(
            "listenport" to "1",
            "buddyname" to "John Doe${(Math.random() * 1000).toInt()}",
            "available" to "visible"
        )

        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record)

        manager.addLocalService(channel, serviceInfo, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.i("", "Registered service")
            }

            override fun onFailure(arg0: Int) {
                Log.i("", "Failed to register service")
            }
        })

        val txtListener = WifiP2pManager.DnsSdTxtRecordListener { fullDomain, record, device ->
            Log.i("", "DnsSdTxtRecord available: \"$record\" \"$fullDomain\" \"${device.deviceAddress}\" \"${device.deviceName}\"")
        }

        val servListener = WifiP2pManager.DnsSdServiceResponseListener { instanceName, registrationType, resourceType ->
            Log.i("", "${instanceName}")
        }

        manager.setDnsSdResponseListeners(channel, servListener, txtListener)

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
*/

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
        fileTransferController.onResume()
        nfcController.onResume()
    }

    override fun onPause() {
        super.onPause()
        fileTransferController.onPause()
        nfcController.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nfcController.onNewIntent(intent)
    }


    // TODO: Move into own component with rendering features
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
