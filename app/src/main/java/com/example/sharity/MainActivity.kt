package com.example.sharity

import android.os.Bundle
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
import com.example.sharity.data.local.Database
import com.example.sharity.ui.feature.ProfileScreen
import com.example.sharity.ui.feature.homescreen.HomeScreen
import com.example.sharity.ui.feature.homescreen.HomeScreenViewModel
import com.example.sharity.ui.theme.SharityTheme

enum class RootScreen { HOME, PROFILE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Database.createDatabaseConnector(this.applicationContext)
        val exoPlayer = ExoPlayer.Builder(applicationContext).build()

        Thread {
            MP3Indexer(applicationContext, db).index()
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
                                onProfileClick = { currentScreen = RootScreen.PROFILE }
                            )
                        }

                        RootScreen.PROFILE -> {
                            // profile.kt is in package com.example.sharity, so no import needed
                            ProfileScreen(
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
