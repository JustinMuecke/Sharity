package com.example.sharity.ui.feature.setupProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.sharity.R
import com.example.sharity.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LandingScreen(
    db: AppDatabase,
    onGoToSetup: () -> Unit,
    onGoToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSetupButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val isComplete = withContext(Dispatchers.IO) {
            runCatching { db.userInfoDao().getValue("profile_complete") }
                .getOrNull() == "true"
        }

        if (isComplete) {
            onGoToHome()
        } else {
            showSetupButton = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_label),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.08f),
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center
        )

        if (!showSetupButton) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Button(
                onClick = onGoToSetup,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text("Set up profile")
            }
        }
    }
}
