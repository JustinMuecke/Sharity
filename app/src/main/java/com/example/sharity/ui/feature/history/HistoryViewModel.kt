package com.example.sharity.ui.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharity.data.local.AppDatabase
import com.example.sharity.domain.model.Connection
import com.example.sharity.domain.usecase.ConnectionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(db: AppDatabase) : ViewModel() {

    private val connectionDao: ConnectionDao = db.connectionDao()

    private val _history = MutableStateFlow<List<Connection>>(emptyList())
    val history: StateFlow<List<Connection>> = _history.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val historyList = connectionDao.getLatest()
            _history.value = historyList
            _isLoading.value = false
        }
    }

    fun refresh() {
        loadHistory()
    }
}