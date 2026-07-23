package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Announcement
import com.example.data.models.Report
import com.example.data.models.Status
import com.example.data.repository.ReportRepository
import com.example.data.repository.StatusRepository
import com.example.utils.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val reportRepository = ReportRepository()
    private val statusRepository = StatusRepository()

    private val _totalStudyMinutes = MutableStateFlow(0)
    val totalStudyMinutes: StateFlow<Int> = _totalStudyMinutes.asStateFlow()

    private val _todayStudyMinutes = MutableStateFlow(0)
    val todayStudyMinutes: StateFlow<Int> = _todayStudyMinutes.asStateFlow()

    private val _streakDays = MutableStateFlow(1)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private val _recentSessions = MutableStateFlow<List<Report>>(emptyList())
    val recentSessions: StateFlow<List<Report>> = _recentSessions.asStateFlow()

    private val _announcement = MutableStateFlow<Announcement?>(null)
    val announcement: StateFlow<Announcement?> = _announcement.asStateFlow()

    private val _konkurCountdown = MutableStateFlow(DateUtils.getKonkurCountdown())
    val konkurCountdown: StateFlow<DateUtils.CountdownTime> = _konkurCountdown.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        startKonkurCountdownTimer()
    }

    fun startSyncLoop(username: String) {
        viewModelScope.launch {
            while (isActive) {
                if (username.isNotEmpty()) {
                    loadData(username)
                    statusRepository.updateStatus(
                        Status(
                            username = username,
                            isOnline = true,
                            isStudying = false,
                            lastSeen = System.currentTimeMillis()
                        )
                    )
                }
                delay(2000) // 2-second auto-sync coroutine
            }
        }
    }

    private fun loadData(username: String) {
        viewModelScope.launch {
            val reportsRes = reportRepository.getReports()
            val reports = reportsRes.getOrDefault(emptyList())

            val userReports = reports.filter { it.username.equals(username, ignoreCase = true) }
            val total = userReports.sumOf { it.duration }
            _totalStudyMinutes.value = total

            // Today's reports
            val todayStr = DateUtils.getCurrentIsoDate().take(10)
            val todayTotal = userReports
                .filter { it.date.take(10) == todayStr || it.date == DateUtils.toPersianDate(System.currentTimeMillis()) }
                .sumOf { it.duration }
            _todayStudyMinutes.value = todayTotal

            // Streak estimate
            _streakDays.value = if (userReports.isNotEmpty()) (userReports.size % 10) + 1 else 1

            // Recent 24h
            _recentSessions.value = userReports.take(10)

            // Announcements
            val announcementsRes = statusRepository.getAnnouncements()
            val annList = announcementsRes.getOrDefault(emptyList())
            _announcement.value = annList.firstOrNull { it.active }
        }
    }

    private fun startKonkurCountdownTimer() {
        viewModelScope.launch {
            while (isActive) {
                _konkurCountdown.value = DateUtils.getKonkurCountdown()
                delay(1000)
            }
        }
    }
}
