package com.example.sharity.ui.feature.setupProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sharity.R
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.ui.feature.AvatarPickerDialog
import com.example.sharity.ui.feature.ProfileImageOption
import com.example.sharity.ui.feature.sanitizeBioText
import com.example.sharity.ui.feature.sanitizeDisplayName
import com.example.sharity.ui.feature.validateBioText
import com.example.sharity.ui.feature.validateDisplayName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileSetupScreen(
    db: AppDatabase,
    paddingValues: PaddingValues,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf(ProfileImageOption.ROCK) }

    var isAvatarDialogOpen by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val nameError = remember(displayName) { validateDisplayName(displayName) }
    val bioError = remember(bio) { validateBioText(bio) }
    val canContinue = !isSaving && nameError == null && bioError == null

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            // Background logo
            Image(
                painter = painterResource(id = R.drawable.logo_label),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.08f),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center
            )


            // Foreground content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Set up your profile",
                    style = MaterialTheme.typography.headlineSmall
                )

                // Avatar + Display name (ohne Done/Cancel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .clickable { isAvatarDialogOpen = true }
                    ) {
                        Image(
                            painter = painterResource(id = avatar.resId),
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        TextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            singleLine = true,
                            isError = nameError != null,
                            label = { Text("Display name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (nameError != null) {
                            Text(
                                text = nameError,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                }

                // Bio (ohne Done/Cancel)
                TextField(
                    value = bio,
                    onValueChange = { bio = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    minLines = 4,
                    maxLines = 8,
                    isError = bioError != null,
                    label = { Text("Bio / music interests") }
                )
                if (bioError != null) {
                    Text(
                        text = bioError,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val finalName = sanitizeDisplayName(displayName)
                        val finalBio = sanitizeBioText(bio)

                        isSaving = true
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    val dao = db.userInfoDao()
                                    dao.upsert("name", finalName)
                                    dao.upsert("bio", finalBio)
                                    dao.upsert("profile_complete", "true")
                                }
                            }.onSuccess {
                                onContinue()
                            }
                            isSaving = false
                        }
                    },
                    enabled = canContinue,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Continue")
                }


                // Avatar picker dialog
                /*if (isAvatarDialogOpen) {
                AvatarPickerDialog(
                    current = avatar,
                    onSelect = { selected -> avatar = selected },
                    onDismiss = { isAvatarDialogOpen = false }
                )
            }*/
            }
        }
    }
}
