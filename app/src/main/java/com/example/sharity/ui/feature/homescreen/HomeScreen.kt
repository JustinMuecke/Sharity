package com.example.sharity.ui.feature.homescreen


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sharity.RootScreen
import com.example.sharity.ui.component.AudioControl
import com.example.sharity.ui.component.HomeTopBar
import com.example.sharity.ui.component.SearchBar
import com.example.sharity.ui.component.TagSelection
import com.example.sharity.ui.component.TrackList
import com.example.sharity.ui.component.navBar.NavBar

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // HomeTopBar(onProfileClick = onProfileClick)

            TagSelection()
            SearchBar(viewModel)
            TrackList(
                viewModel = viewModel,
                modifier = Modifier.weight(1f)
            )
            AudioControl(viewModel)
        }

    }
    NavBar(
        showBack = false,
        onBackClick = {},
        onNfcClick = { /* TODO */ },
        onProfileClick = onProfileClick
    )

}

