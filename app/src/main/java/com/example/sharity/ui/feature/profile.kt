package com.example.sharity.ui.feature


import com.example.sharity.R

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.AlertDialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import com.example.sharity.ui.component.BackButton


// reference to own files
import com.example.sharity.ui.theme.DarkBlackberry
import com.example.sharity.ui.theme.DustyPurple
import com.example.sharity.ui.theme.GrapeGlimmer
import com.example.sharity.ui.theme.SheerLilac
import com.example.sharity.ui.theme.AccentDeepIndigo




data class UserStats(
    val songs: Int,
    val sent: Int,
    val received: Int
)

data class Badge(
    val label: String
)

enum class ProfileImageOption(@DrawableRes val resId: Int) {
    ROCK(R.drawable.guitar_boy),
    POP(R.drawable.singerin),
    JAZZ(R.drawable.vinyl)
}


@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    val bioState = remember { mutableStateOf("Tell us about your music taste, last concert, etc.") }
    val stats = UserStats(songs = 42, sent = 10, received = 5)
    val badges = listOf(
        Badge("First Share"),
        Badge("10 Songs"),
        Badge("Night Listener")
    )

    val userNameState = remember { mutableStateOf("Your Name") }
    val isEditingName = remember { mutableStateOf(false) }

    val profileImageState = remember { mutableStateOf(ProfileImageOption.ROCK) }
    val isAvatarDialogOpen = remember { mutableStateOf(false) }

    val isEditingBio = remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                BackButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart as Alignment.Vertical)
                )
            }
            ProfileHeader(
                nameState = userNameState,
                isEditing = isEditingName.value,
                onStartEdit = { isEditingName.value = true },
                onDone = { newName ->
                    val sanitized = sanitizeDisplayName(newName)
                    userNameState.value = sanitized
                    isEditingName.value = false
                    // later: save to DB
                },
                onCancel = { isEditingName.value = false },
                profileImage = profileImageState.value,
                onAvatarClick = { isAvatarDialogOpen.value = true }
            )

            StatsSection(stats = stats)

            BioSection(
                bioState = bioState,
                isEditing = isEditingBio.value,
                onStartEdit = { isEditingBio.value = true },
                onDone = { newBio ->
                    val sanitized = sanitizeBioText(newBio)
                    bioState.value = sanitized
                    isEditingBio.value = false
                    // later: save to DB
                },
                onCancel = { isEditingBio.value = false }
            )

            BadgesSection(badges = badges)
        }

        if (isAvatarDialogOpen.value) {
            AvatarPickerDialog(
                current = profileImageState.value,
                onSelect = { selected ->
                    profileImageState.value = selected
                    // later: save to DB
                },
                onDismiss = { isAvatarDialogOpen.value = false }
            )
        }
    }
}

@Composable
fun ProfileHeader(
    nameState: MutableState<String>,
    isEditing: Boolean,
    onStartEdit: () -> Unit,
    onDone: (String) -> Unit,
    onCancel: () -> Unit,
    profileImage: ProfileImageOption,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tempName = remember(isEditing) { mutableStateOf(nameState.value) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    if (isEditing) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable { onAvatarClick() }
                ) {
                    Image(
                        painter = painterResource(id = profileImage.resId),
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "Edit name",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GrapeGlimmer
                    )
                )
            }

            TextField(
                value = tempName.value,
                onValueChange = { value ->
                    tempName.value = value
                    errorMessage.value = validateDisplayName(value)
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage.value != null,
                label = { Text("Display name") }
            )

            if (errorMessage.value != null) {
                Text(
                    text = errorMessage.value.orEmpty(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error
                    )
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = {
                        if (errorMessage.value == null) {
                            onDone(tempName.value)
                        }
                    },
                    enabled = errorMessage.value == null
                ) {
                    Text("Done")
                }
                TextButton(onClick = { onCancel() }) {
                    Text("Cancel")
                }
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable { onAvatarClick() }
            ) {
                Image(
                    painter = painterResource(id = profileImage.resId),
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = nameState.value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = GrapeGlimmer
                    )
                )
                Text(
                    text = "Music sharer",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            IconButton(onClick = onStartEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit name",
                    tint = GrapeGlimmer
                )
            }
        }
    }
}



@Composable
fun AvatarPickerDialog(
    current: ProfileImageOption,
    onSelect: (ProfileImageOption) -> Unit,
    onDismiss: () -> Unit
) {
    val tempSelection = remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onSelect(tempSelection.value)
                onDismiss()
            }) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Choose profile picture") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = tempSelection.value.resId),
                        contentDescription = "Selected profile picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileImageOption.values().forEach { option ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable { tempSelection.value = option }
                        ) {
                            Image(
                                painter = painterResource(id = option.resId),
                                contentDescription = option.name,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { /* TODO: upload from gallery */ },
                    enabled = false
                ) {
                    Text("Upload image (coming soon)")
                }
            }
        }
    )
}



@Composable
fun StatsSection(
    stats: UserStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Songs",
            value = stats.songs.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Sent",
            value = stats.sent.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Received",
            value = stats.received.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}
@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,  // no weight here
        colors = CardDefaults.cardColors(
            containerColor = DustyPurple
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun BioSection(
    bioState: MutableState<String>,
    isEditing: Boolean,
    onStartEdit: () -> Unit,
    onDone: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tempBio = remember(isEditing) { mutableStateOf(bioState.value) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "About you",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            if (!isEditing) {
                IconButton(onClick = onStartEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit bio",
                        tint = GrapeGlimmer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isEditing) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkBlackberry
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    TextField(
                        value = tempBio.value,
                        onValueChange = { value ->
                            tempBio.value = value
                            errorMessage.value = validateBioText(value)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        minLines = 3,
                        maxLines = 6,
                        isError = errorMessage.value != null,
                        label = { Text("Bio / music interests") }
                    )

                    if (errorMessage.value != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage.value.orEmpty(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = {
                                if (errorMessage.value == null) {
                                    onDone(tempBio.value)
                                }
                            },
                            enabled = errorMessage.value == null
                        ) {
                            Text("Done")
                        }
                        TextButton(onClick = { onCancel() }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkBlackberry
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    val text = bioState.value.ifBlank {
                        "Add your bio, music interests, last concerts..."
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BadgesSection(
    badges: List<Badge>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Badges",
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            badges.forEach { badge ->
                BadgeChip(label = badge.label)
            }
        }
    }
}

@Composable
fun BadgeChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = AccentDeepIndigo,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = SheerLilac,
                fontSize = 12.sp
            )
        )
    }
}


//TODO
// Basic display name sanitizing before persisting
fun sanitizeDisplayName(raw: String): String {
    return raw.trim().replace(Regex("[\"'`;\\\\]"), "")
}

//TODO
// Returns error message or null if valid
fun validateDisplayName(input: String): String? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return "Name cannot be empty"
    if (trimmed.length > 40) return "Name is too long"
    if (Regex("[\"'`;\\\\]").containsMatchIn(trimmed)) {
        return "Characters \" ' ; and \\ are not allowed"
    }
    return null
}


fun sanitizeBioText(raw: String): String {
    return raw.trim().replace(Regex("[\"'`;\\\\]"), "")
}

fun validateBioText(input: String): String? {
    val trimmed = input.trim()
    if (trimmed.length > 280) return "Bio is too long"
    if (Regex("[\\u0000-\\u001F]").containsMatchIn(trimmed)) {
        return "Bio contains invalid control characters"
    }
    if (Regex("[\"'`;\\\\]").containsMatchIn(trimmed)) {
        return "Characters \" ' ; and \\ are not allowed"
    }
    return null
}
