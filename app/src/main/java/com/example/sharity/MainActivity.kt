package com.example.sharity

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sharity.data.device.MP3Indexer

import com.example.sharity.data.wrapper.Database
import androidx.lifecycle.lifecycleScope
import com.example.sharity.data.device.NfcClient
import com.example.sharity.data.wrapper.NfcController
import kotlinx.coroutines.launch
import com.example.sharity.ui.theme.SharityTheme
import com.example.sharity.ui.feature.homescreen.HomeScreen
import com.example.sharity.ui.feature.homescreen.HomeScreenViewModel

private lateinit var nfcController: NfcController
private val nfcClient = NfcClient()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dbInstance = Database.createDatabaseConnector(this.applicationContext)
        nfcController = NfcController(this) { tag ->
            logNfcMessages(tag)
        }
        Thread({
            MP3Indexer(applicationContext, dbInstance).index()
        }).start()

        setContent {
            val homeViewModel = viewModel<HomeScreenViewModel>()
            SharityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        viewModel = homeViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
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
                    val profile = String(bytes)
                    Log.d("NFC", "Profile exchange successfully: $profile")
                }
                .onFailure {
                    Log.e("NFC", "Profile exchange failed", it)
                }
        }
    }}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SharityTheme {
        Greeting("Android")
    }
}