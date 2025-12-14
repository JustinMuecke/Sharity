package com.example.sharity.ui.feature.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sharity.domain.model.Connection
import com.example.sharity.domain.model.NameStatsJunction

@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    val friends by viewModel.friends.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()



    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            friends.isEmpty() -> {
                EmptyFriendsState(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                FriendsGrid(
                    friends = friends,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun FriendsGrid(
    friends: List<NameStatsJunction>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header as a full-width item (spans both columns)
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "People you exchanged tracks with",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(friends) { friend ->
            FriendTile(
                friend = friend,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    // Optional: later navigate to friend details/history
                    // Keep empty for now to preserve existing logic.
                }
            )
        }
    }
}

@Composable
private fun FriendTile(
    friend: NameStatsJunction,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .aspectRatio(1.25f)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = friend.stats.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = friend.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${friend.stats} tracks exchanged",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptyFriendsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No friends yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Share music with others to see them here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
