package com.example.sharity.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrackCard(title : String,
              isSelected: Boolean,      // <--- New
              onClick: () -> Unit   ){
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth() // Take full width
            .height(100.dp), // Fixed height for visual consistency
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(4.dp) // Adds shadow
    ) {
        // Content inside the card
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // Centers text inside card
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}