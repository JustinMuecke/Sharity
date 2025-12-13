package com.example.sharity.ui.feature.homescreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sharity.ui.component.AudioControl
import com.example.sharity.ui.component.SearchBar
import com.example.sharity.ui.component.TagSelection
import com.example.sharity.ui.component.TrackList


@Composable
fun HomeScreen(viewModel : HomeScreenViewModel, modifier : Modifier){
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = modifier.fillMaxSize() // Fill the screen
        ) {
            TagSelection()
            SearchBar(viewModel)
            TrackList(viewModel,
                modifier = Modifier.weight(1f))
            AudioControl(viewModel)
        }
    }
}