package com.example.sharity.ui.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.domain.model.NameStatsJunction
import com.example.sharity.domain.usecase.ConnectionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FriendsViewModel(db: AppDatabase) : ViewModel() {

    private val connectionDao: ConnectionDao = db.connectionDao()

    private val _friends = MutableStateFlow<List<NameStatsJunction>>(emptyList())
    val friends: StateFlow<List<NameStatsJunction>> = _friends.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadFriends()
    }

    private fun loadFriends() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val friendsList = connectionDao.getDistinctByMax()
            _friends.value = friendsList
            _isLoading.value = false
        }
    }

    fun refresh() {
        loadFriends()
    }
}