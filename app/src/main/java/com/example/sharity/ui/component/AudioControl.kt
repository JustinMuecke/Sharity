package com.example.sharity.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.feature.homescreen.HomeScreenViewModel

@Composable
fun AudioControl(viewModel : HomeScreenViewModel){
    val title = viewModel.currentTrack.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth() // Take full width
            .height(100.dp),
    ) {
        Text(
            text= "Current Track: ${title.value}"
        )
    }
}