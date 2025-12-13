package com.example.sharity.ui.component.navBar

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.theme.DarkBlackberry
import com.example.sharity.ui.theme.DustyPurple

@Composable
fun ProfileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = DarkBlackberry,
    iconColor: Color = Color.White
) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = backgroundColor,
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "Profile",
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
