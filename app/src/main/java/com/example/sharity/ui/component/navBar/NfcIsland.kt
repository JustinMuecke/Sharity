package com.example.sharity.ui.component.navBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.theme.DarkBlackberry
import com.example.sharity.ui.theme.GrapeGlimmer
import com.example.sharity.ui.theme.PureWhite

@Composable
fun NfcIsland(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = DarkBlackberry,
    text: String = "Share",
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White

        )
    }
}
