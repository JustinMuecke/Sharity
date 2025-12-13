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
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sharity.data.device.NfcClient
import com.example.sharity.data.local.PrimaryUser
import com.example.sharity.data.wrapper.NfcController
import com.example.sharity.ui.component.AudioControl
import com.example.sharity.ui.component.navBar.NavBar
import kotlinx.coroutines.launch
import com.example.sharity.ui.theme.SharityTheme
import com.example.sharity.ui.feature.ProfileScreen
import com.example.sharity.ui.feature.allsongsscreen.AllSongsView
import com.example.sharity.ui.feature.allsongsscreen.AllSongsViewModel
import com.example.sharity.ui.feature.homescreen.HomeScreen
import com.example.sharity.ui.feature.playlistscreen.PlaylistView
import com.example.sharity.ui.feature.playlistselection.PlaylistSelectionScreen


object RootDestinations {
    const val HOME = "home"
    const val PROFILE = "profile"
    const val NFC = "nfc"
    const val HISTORY = "history" // New route
    const val FRIENDS = "friends" // New route
    const val PLAYLISTS = "playlists" // New route
    const val PLAYLIST_SELECTION = "playlists_selection" // New route for this screen
    const val PLAYLIST_VIEW = "playlist_view/{playlistId}" // Route for viewing an individual playlist

    const val ALL_SONGS = "all_songs"
}

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
                val indexer =
                    MP3Indexer(applicationContext, db, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                indexer.index()
            } catch (e: Exception) {
                // TODO: Handle error!
                Log.e("ERROR", "", e)
            }
        }).start()



        setContent {
            val navController = rememberNavController()

            // 2. Observe the NavController's back stack state
            val navBackStackEntry by navController.currentBackStackEntryAsState()

            // Check if there is a previous screen to go back to
            val showBackButton = navController.previousBackStackEntry != null

            // Get the current route/destination for the title logic
            val currentRoute = navBackStackEntry?.destination?.route
            val allSongsViewModel = viewModel<AllSongsViewModel>(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AllSongsViewModel(db, exoPlayer) as T
                    }
                }
            )

            SharityTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        val screenTitle = when ("") {
                            "home" -> "My Music Library"
                            "now_playing" -> "Now Playing"
                            // Add more cases for other routes
                            else -> "App"
                        }
                        NavBar(
                            showBack = showBackButton, // Pass the derived state
                            onBackClick = {
                                // Use the NavController's standard function
                                navController.navigateUp()
                            },
                            onNfcClick = {
                                // Navigate to a specific NFC screen route
                                navController.navigate(RootDestinations.NFC)
                            },
                            onProfileClick = {
                                // Navigate to the Profile screen route
                                navController.navigate(RootDestinations.PROFILE)
                            }
                        )
                    },

                    bottomBar = {
                        if (allSongsViewModel.currentTrack.collectAsState().value != null) {
                            AudioControl(allSongsViewModel) //TODO Extract Audio Control from playlist View

                        }
                    }) { innerPadding ->

                    // 3. Replace 'when (currentScreen)' with NavHost
                    NavHost(
                        navController = navController,
                        startDestination = RootDestinations.HOME,
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable(RootDestinations.HOME) {
                            HomeScreen(
                                modifier = Modifier,
                                db = db,
                                exoPlayer = exoPlayer,
                                allSongsViewModel = allSongsViewModel,
                                onProfileClick = { navController.navigate(RootDestinations.PROFILE) },

                                onHistoryClick = { navController.navigate(RootDestinations.HISTORY) },
                                onFriendsClick = { navController.navigate(RootDestinations.FRIENDS) },
                                onPlaylistsClick = { navController.navigate(RootDestinations.PLAYLISTS) },
                                onListClick = { ("") },
                                onAllsongsClick = { navController.navigate(RootDestinations.ALL_SONGS)}
                            )
                        }

                        // Add the new destination composables
                        composable(RootDestinations.HISTORY) {
                            // Placeholder for the History Screen
                            Text(text = "History Screen Content")
                        }

                        composable(RootDestinations.FRIENDS) {
                            // Placeholder for the Friends Screen
                            Text(text = "Friends Screen Content")
                        }

                        composable(RootDestinations.PLAYLISTS) {
                            PlaylistSelectionScreen(
                                onViewPlaylist = { playlistId ->
                                    // Navigation remains t1he same, but 'playlistId' is now an Int
                                    navController.navigate("playlist_view/$playlistId")
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        composable(RootDestinations.PROFILE) {
                            ProfileScreen()
                        }
                        composable( RootDestinations.ALL_SONGS){
                            AllSongsView(
                                viewModel = allSongsViewModel,
                                modifier = Modifier.fillMaxSize(1f),
                            )
                        }
                        composable(RootDestinations.PLAYLISTS) { // Note: I believe you meant RootDestinations.PLAYLIST_SELECTION here, but I'll use RootDestinations.PLAYLISTS as per your code
                            PlaylistSelectionScreen(
                                onViewPlaylist = { playlistId ->
                                    // The 'playlistId' is now an Int
                                    navController.navigate("playlist_view/$playlistId")
                                },
                                // The modifier passed from Scaffold content should be used here
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        // 2. Destination for viewing the individual playlist (Must be a direct child of NavHost)
                        composable(
                            route = RootDestinations.PLAYLIST_VIEW, // "playlist_view/{playlistId}"
                            arguments = listOf(navArgument("playlistId") {
                                type = NavType.IntType // ⬅️ FIX: IntType to match navigation and retrieval
                            })
                        ) { backStackEntry ->
                            // Retrieve the argument as an Int
                            val playlistId = backStackEntry.arguments?.getInt("playlistId") ?: 0

                            // Pass the ViewModel and the derived ID to your view
                            PlaylistView(
                                playlistId
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
