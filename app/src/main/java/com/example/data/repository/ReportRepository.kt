package com.example.data.repository

import com.example.data.models.Report
import com.example.data.network.ApiConstants
import com.example.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportRepository {

    private val localReportsCache = mutableListOf<Report>()

    suspend fun getReports(): Result<List<Report>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getBinData(
                ApiConstants.BIN_REPORTS,
                ApiConstants.KEY_REPORTS
            )
            if (response.isSuccessful && response.body() != null) {
                val reports = ApiClient.parseRecordList<Report>(response.body().toString())
                localReportsCache.clear()
                localReportsCache.addAll(reports)
                Result.success(reports)
            } else {
                Result.success(localReportsCache.toList())
            }
        } catch (e: Exception) {
            Result.success(localReportsCache.toList())
        }
    }

    suspend fun addReport(report: Report): Result<Report> = withContext(Dispatchers.IO) {
        try {
            val currentRes = getReports()
            val currentReports = currentRes.getOrDefault(emptyList()).toMutableList()

            currentReports.add(0, report)

            // Keeping size under control to prevent JSONBin 100KB limit overflow
            val truncatedList = currentReports.take(150)

            ApiClient.apiService.updateBinData(
                ApiConstants.BIN_REPORTS,
                ApiConstants.KEY_REPORTS,
                truncatedList
            )

            localReportsCache.clear()
            localReportsCache.addAll(truncatedList)
            Result.success(report)
        } catch (e: Exception) {
            localReportsCache.add(0, report)
            Result.success(report)
        }
    }

    suspend fun deleteReport(reportId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val currentRes = getReports()
            val currentReports = currentRes.getOrDefault(emptyList()).toMutableList()
            currentReports.removeAll { it.id == reportId }

            ApiClient.apiService.updateBinData(
                ApiConstants.BIN_REPORTS,
                ApiConstants.KEY_REPORTS,
                currentReports
            )

            localReportsCache.clear()
            localReportsCache.addAll(currentReports)
            Result.success(true)
        } catch (e: Exception) {
            localReportsCache.removeAll { it.id == reportId }
            Result.success(true)
        }
    }
}
