package com.example.sharity.ui.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.domain.model.Connection
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
    //for testing
    // ConnectionDao - constructor mit parametern
    // private val _firends = ConnectionDao.insertAll(3, "Chris", "uuid-3", 200, 180)
    // private val friendsDemo = MutableStateFlow<List<NameStatsJunction>>(sampleFriends)

    val friends: StateFlow<List<NameStatsJunction>> = _friends.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /*
    val sampleFriends = List<NameStatsJunction>(
        Connection(3, "Chris", "uuid-3", 200, 180),
        Connection(4, "Dana", "uuid-4", 20, 0),
        Connection(5, "Eve", "uuid-5", 15, 2000),
        Connection(7, "John", "uuid-7", 1, 4)
    )*/

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

fun ConnectionDao.Companion.insertAll(i: 5, string: "Eve", string: "uuid-5", i2: 15, i3: 2000) {

}
