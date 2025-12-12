package com.example.sharity.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.feature.validateBioText
import com.example.sharity.ui.theme.DarkBlackberry
import com.example.sharity.ui.theme.GrapeGlimmer


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