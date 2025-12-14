import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sharity.domain.model.Playlist
import com.example.sharity.domain.model.Track

@Composable
fun ReusableTrackList(
    trackList: List<Track>,
    selectedTracks: Set<Track>,
    onTrackClick: (Track) -> Unit,
    onTrackSelect: (Track) -> Unit,
    isSelectionMode: Boolean,
    currentListName: String,
    // New parameters for playlist functionality
    playlists: List<Playlist> = emptyList(),
    onAddTrackToPlaylist: ((Track, Playlist) -> Unit)? = null,
    onCreateNewPlaylist: (() -> Unit)? = null,
    // Search parameters
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var selectedTrackForPlaylist by remember { mutableStateOf<Track?>(null) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // List Name/Title
        Text(
            text = currentListName,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Search Bar (optional, only shows if onSearchQueryChange is provided)
        if (onSearchQueryChange != null) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search songs by title...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search Icon")
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp)
            )
        }

        // Track List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = trackList) { track ->
                val clickAction = if (isSelectionMode) {
                    { onTrackSelect(track) }
                } else {
                    { onTrackClick(track) }
                }

                val isSelected = selectedTracks.contains(track)

                TrackCard(
                    uri = track.contentUri,
                    title = track.title,
                    artist = track.artist ?: "Unknown Artist",
                    isSelected = isSelected,
                    onClick = clickAction,
                )
            }
        }
    }


}