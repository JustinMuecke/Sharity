package com.example.sharity.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharity.ui.feature.playlistscreen.PlaylistViewModel

@Composable
fun SearchBar(viewModel : PlaylistViewModel){
    // Collect the search query state from the ViewModel
    val currentQuery by viewModel.searchQuery.collectAsState()

    OutlinedTextField(
        value = currentQuery,
        onValueChange = { newQuery ->
            viewModel.onSearchQueryChange(newQuery)
        },
        placeholder = { Text("Search songs by title...") },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = "Search Icon")
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp)
    )
}

