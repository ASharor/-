package com.example.data.repository

import com.example.data.models.Announcement
import com.example.data.models.ButtonInfo
import com.example.data.models.LeaderboardEntry
import com.example.data.models.Status
import com.example.data.network.ApiConstants
import com.example.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatusRepository {

    private val localStatusCache = mutableListOf<Status>()
    private val localLeaderboardCache = mutableListOf<LeaderboardEntry>()

    suspend fun getStatusList(): Result<List<Status>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getBinData(
                ApiConstants.BIN_STATUS,
                ApiConstants.KEY_STATUS
            )
            if (response.isSuccessful && response.body() != null) {
                val list = ApiClient.parseRecordList<Status>(response.body().toString())
                localStatusCache.clear()
                localStatusCache.addAll(list)
                Result.success(list)
            } else {
                Result.success(localStatusCache.toList())
            }
        } catch (e: Exception) {
            Result.success(localStatusCache.toList())
        }
    }

    suspend fun updateStatus(status: Status): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val list = localStatusCache.toMutableList()
            val idx = list.indexOfFirst { it.username.equals(status.username, ignoreCase = true) }
            if (idx >= 0) {
                list[idx] = status
            } else {
                list.add(status)
            }

            ApiClient.apiService.updateBinData(
                ApiConstants.BIN_STATUS,
                ApiConstants.KEY_STATUS,
                list
            )
            localStatusCache.clear()
            localStatusCache.addAll(list)
            Result.success(true)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getBinData(
                ApiConstants.BIN_LEADERBOARD,
                ApiConstants.KEY_LEADERBOARD
            )
            if (response.isSuccessful && response.body() != null) {
                val list = ApiClient.parseRecordList<LeaderboardEntry>(response.body().toString())
                localLeaderboardCache.clear()
                localLeaderboardCache.addAll(list)
                Result.success(list)
            } else {
                Result.success(localLeaderboardCache.toList())
            }
        } catch (e: Exception) {
            Result.success(localLeaderboardCache.toList())
        }
    }

    suspend fun getAnnouncements(): Result<List<Announcement>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getBinData(
                ApiConstants.BIN_ANNOUNCEMENTS,
                ApiConstants.KEY_ANNOUNCEMENTS
            )
            if (response.isSuccessful && response.body() != null) {
                val list = ApiClient.parseRecordList<Announcement>(response.body().toString())
                Result.success(list)
            } else {
                Result.success(listOf(getSampleAnnouncement()))
            }
        } catch (e: Exception) {
            Result.success(listOf(getSampleAnnouncement()))
        }
    }

    private fun getSampleAnnouncement() = Announcement(
        id = "1",
        title = "اطلاعیه شروع دوره مطالعه کنکور",
        message = "برنامه‌ریزی جدید مطالعه گروهی فعال شد. گزارش‌های روزانه خود را حتماً ثبت کنید.",
        date = "1403/05/01",
        active = true,
        button = ButtonInfo("مشاهده جزییات", "https://partino.app")
    )
}
