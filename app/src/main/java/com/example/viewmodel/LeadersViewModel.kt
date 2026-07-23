package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.LeaderboardEntry
import com.example.data.models.Profile
import com.example.data.repository.StatusRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LeadersViewModel : ViewModel() {

    private val statusRepository = StatusRepository()
    private val userRepository = UserRepository()

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private val _selectedProfile = MutableStateFlow<Profile?>(null)
    val selectedProfile: StateFlow<Profile?> = _selectedProfile.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        startAutoRefreshLoop()
    }

    private fun startAutoRefreshLoop() {
        viewModelScope.launch {
            while (isActive) {
                refreshLeaderboard()
                delay(10000) // Auto-refresh every 10 seconds with Coroutine
            }
        }
    }

    fun refreshLeaderboard() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val res = statusRepository.getLeaderboard()
            val list = res.getOrDefault(emptyList())

            if (list.isNotEmpty()) {
                val sorted = list.sortedByDescending { it.totalStudy }.take(50).mapIndexed { index, entry ->
                    entry.copy(rank = index + 1)
                }
                _leaderboard.value = sorted
            } else {
                // Generate sample top 50 leaderboard if server bin is empty
                val sample = (1..50).map { i ->
                    LeaderboardEntry(
                        username = "student_$i",
                        totalStudy = (600 - i * 10).coerceAtLeast(60),
                        rank = i,
                        todayStudy = (180 - i * 3).coerceAtLeast(15)
                    )
                }
                _leaderboard.value = sample
            }
            _isRefreshing.value = false
        }
    }

    fun selectUserForProfile(username: String) {
        viewModelScope.launch {
            val res = userRepository.getProfile(username)
            _selectedProfile.value = res.getOrDefault(Profile(username = username))
        }
    }

    fun clearSelectedProfile() {
        _selectedProfile.value = null
    }
}
