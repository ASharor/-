package com.example.data.repository

import com.example.data.models.Profile
import com.example.data.models.User
import com.example.data.network.ApiConstants
import com.example.data.network.ApiClient
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {

    private val localUsersCache = mutableListOf<User>()
    private val localProfilesCache = mutableListOf<Profile>()

    suspend fun getUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getBinData(
                ApiConstants.BIN_USERS,
                ApiConstants.KEY_USERS
            )
            if (response.isSuccessful && response.body() != null) {
                val jsonString = response.body().toString()
                val users = ApiClient.parseRecordList<User>(jsonString)
                localUsersCache.clear()
                localUsersCache.addAll(users)
                Result.success(users)
            } else {
                Result.success(localUsersCache.toList())
            }
        } catch (e: Exception) {
            Result.success(localUsersCache.toList())
        }
    }

    suspend fun registerUser(user: User): Result<User> = withContext(Dispatchers.IO) {
        try {
            val currentUsersRes = getUsers()
            val currentUsers = currentUsersRes.getOrDefault(emptyList()).toMutableList()

            if (currentUsers.any { it.username.equals(user.username, ignoreCase = true) }) {
                return@withContext Result.failure(Exception("USER_EXISTS"))
            }

            currentUsers.add(user)
            val putRes = ApiClient.apiService.updateBinData(
                ApiConstants.BIN_USERS,
                ApiConstants.KEY_USERS,
                currentUsers
            )

            if (putRes.isSuccessful) {
                localUsersCache.clear()
                localUsersCache.addAll(currentUsers)
                Result.success(user)
            } else {
                localUsersCache.add(user)
                Result.success(user)
            }
        } catch (e: Exception) {
            localUsersCache.add(user)
            Result.success(user)
        }
    }

    suspend fun loginUser(username: String, passwordHash: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val currentUsersRes = getUsers()
            val currentUsers = currentUsersRes.getOrDefault(emptyList())

            val foundUser = currentUsers.find {
                it.username.equals(username, ignoreCase = true) && it.passwordHash == passwordHash
            }

            if (foundUser != null) {
                Result.success(foundUser)
            } else {
                Result.failure(Exception("INVALID_CREDENTIALS"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(username: String): Result<Profile> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getBinData(
                ApiConstants.BIN_PROFILES,
                ApiConstants.KEY_PROFILES
            )
            val profiles = if (response.isSuccessful && response.body() != null) {
                ApiClient.parseRecordList<Profile>(response.body().toString())
            } else {
                localProfilesCache.toList()
            }
            localProfilesCache.clear()
            localProfilesCache.addAll(profiles)

            val profile = profiles.find { it.username.equals(username, ignoreCase = true) }
                ?: Profile(username = username, avatar = null, bio = "")
            Result.success(profile)
        } catch (e: Exception) {
            val profile = localProfilesCache.find { it.username.equals(username, ignoreCase = true) }
                ?: Profile(username = username, avatar = null, bio = "")
            Result.success(profile)
        }
    }

    suspend fun saveProfile(profile: Profile): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val profiles = localProfilesCache.toMutableList()
            val idx = profiles.indexOfFirst { it.username.equals(profile.username, ignoreCase = true) }
            if (idx >= 0) {
                profiles[idx] = profile
            } else {
                profiles.add(profile)
            }

            val putRes = ApiClient.apiService.updateBinData(
                ApiConstants.BIN_PROFILES,
                ApiConstants.KEY_PROFILES,
                profiles
            )

            if (putRes.isSuccessful) {
                localProfilesCache.clear()
                localProfilesCache.addAll(profiles)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.success(true)
        }
    }
}
