package com.example.sharity.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TagSelection(){
    Box(
        modifier = Modifier
            .fillMaxWidth() // Take full width
            .height(100.dp),
    ) {
        Text(
            text="Tag Selection"
        )
    }
}