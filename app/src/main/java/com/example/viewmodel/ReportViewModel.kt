package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Report
import com.example.data.repository.ReportRepository
import com.example.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {

    private val reportRepository = ReportRepository()

    private val _allReports = MutableStateFlow<List<Report>>(emptyList())
    val allReports: StateFlow<List<Report>> = _allReports.asStateFlow()

    private val _weeklyStudyMinutes = MutableStateFlow<List<Int>>(listOf(0, 0, 0, 0, 0, 0, 0))
    val weeklyStudyMinutes: StateFlow<List<Int>> = _weeklyStudyMinutes.asStateFlow()

    private val _saveState = MutableStateFlow<SaveReportState>(SaveReportState.Idle)
    val saveState: StateFlow<SaveReportState> = _saveState.asStateFlow()

    fun loadReports(username: String) {
        viewModelScope.launch {
            val res = reportRepository.getReports()
            val reports = res.getOrDefault(emptyList())
            val userReports = reports.filter { it.username.equals(username, ignoreCase = true) }
            _allReports.value = userReports

            // Calculate mock last 7 days chart data based on reports
            val chart = MutableList(7) { 0 }
            userReports.take(14).forEachIndexed { idx, report ->
                val dayIdx = idx % 7
                chart[dayIdx] = (chart[dayIdx] + report.duration)
            }
            _weeklyStudyMinutes.value = chart
        }
    }

    fun submitReport(
        username: String,
        book: String,
        topic: String,
        grade: String,
        durationStr: String,
        testCountStr: String,
        correctCountStr: String,
        wrongCountStr: String,
        note: String
    ) {
        if (book.isBlank()) {
            _saveState.value = SaveReportState.ValidationError("book", "field_required")
            return
        }
        if (topic.isBlank()) {
            _saveState.value = SaveReportState.ValidationError("topic", "field_required")
            return
        }

        val duration = durationStr.toIntOrNull()
        if (duration == null || duration <= 0) {
            _saveState.value = SaveReportState.ValidationError("duration", "invalid_number")
            return
        }

        val testCount = testCountStr.toIntOrNull() ?: 0
        val correctCount = correctCountStr.toIntOrNull() ?: 0
        val wrongCount = wrongCountStr.toIntOrNull() ?: 0

        _saveState.value = SaveReportState.Loading
        viewModelScope.launch {
            val report = Report(
                username = username,
                book = book.trim(),
                topic = topic.trim(),
                grade = grade.ifBlank { "کنکور" },
                duration = duration,
                testCount = testCount,
                correctCount = correctCount,
                wrongCount = wrongCount,
                date = DateUtils.toPersianDate(System.currentTimeMillis()),
                note = note.trim()
            )

            val res = reportRepository.addReport(report)
            if (res.isSuccess) {
                _saveState.value = SaveReportState.Success
                loadReports(username)
            } else {
                _saveState.value = SaveReportState.Error("Failed to save report")
            }
        }
    }

    fun deleteReport(username: String, reportId: String) {
        viewModelScope.launch {
            reportRepository.deleteReport(reportId)
            loadReports(username)
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveReportState.Idle
    }
}

sealed class SaveReportState {
    object Idle : SaveReportState()
    object Loading : SaveReportState()
    object Success : SaveReportState()
    data class ValidationError(val field: String, val messageKey: String) : SaveReportState()
    data class Error(val message: String) : SaveReportState()
}
