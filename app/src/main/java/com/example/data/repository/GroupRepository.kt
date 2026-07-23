package com.example.data.repository

import com.example.data.models.Group
import com.example.data.models.MemberList
import com.example.data.network.ApiConstants
import com.example.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class GroupRepository {

    private val localGroupsCache = mutableListOf<Group>()

    suspend fun getGroups(): Result<List<Group>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getBinData(
                ApiConstants.BIN_GROUPS,
                ApiConstants.KEY_GROUPS
            )
            if (response.isSuccessful && response.body() != null) {
                val groups = ApiClient.parseRecordList<Group>(response.body().toString())
                localGroupsCache.clear()
                localGroupsCache.addAll(groups)
                Result.success(groups)
            } else {
                Result.success(localGroupsCache.toList())
            }
        } catch (e: Exception) {
            Result.success(localGroupsCache.toList())
        }
    }

    suspend fun createGroup(name: String, owner: String, avatar: String?): Result<Group> = withContext(Dispatchers.IO) {
        try {
            val currentRes = getGroups()
            val currentGroups = currentRes.getOrDefault(emptyList()).toMutableList()

            val newGroup = Group(
                id = UUID.randomUUID().toString().take(8),
                name = name,
                owner = owner,
                members = listOf(owner),
                avatar = avatar,
                createdAt = System.currentTimeMillis().toString()
            )

            currentGroups.add(newGroup)
            ApiClient.apiService.updateBinData(
                ApiConstants.BIN_GROUPS,
                ApiConstants.KEY_GROUPS,
                currentGroups
            )

            localGroupsCache.clear()
            localGroupsCache.addAll(currentGroups)
            Result.success(newGroup)
        } catch (e: Exception) {
            val newGroup = Group(
                id = UUID.randomUUID().toString().take(8),
                name = name,
                owner = owner,
                members = listOf(owner),
                avatar = avatar,
                createdAt = System.currentTimeMillis().toString()
            )
            localGroupsCache.add(newGroup)
            Result.success(newGroup)
        }
    }

    suspend fun addMemberToGroup(groupId: String, rawUsername: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val cleanUsername = rawUsername.trim().removePrefix("@")
            if (cleanUsername.isEmpty()) return@withContext Result.failure(Exception("EMPTY_USERNAME"))

            val currentGroups = getGroups().getOrDefault(emptyList()).toMutableList()
            val idx = currentGroups.indexOfFirst { it.id == groupId }
            if (idx >= 0) {
                val group = currentGroups[idx]
                if (!group.members.contains(cleanUsername)) {
                    val updatedMembers = group.members + cleanUsername
                    currentGroups[idx] = group.copy(members = updatedMembers)

                    ApiClient.apiService.updateBinData(
                        ApiConstants.BIN_GROUPS,
                        ApiConstants.KEY_GROUPS,
                        currentGroups
                    )
                    localGroupsCache.clear()
                    localGroupsCache.addAll(currentGroups)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    suspend fun leaveGroup(groupId: String, username: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val currentGroups = getGroups().getOrDefault(emptyList()).toMutableList()
            val idx = currentGroups.indexOfFirst { it.id == groupId }
            if (idx >= 0) {
                val group = currentGroups[idx]
                val updatedMembers = group.members.filter { !it.equals(username, ignoreCase = true) }
                if (updatedMembers.isEmpty()) {
                    currentGroups.removeAt(idx)
                } else {
                    currentGroups[idx] = group.copy(members = updatedMembers)
                }

                ApiClient.apiService.updateBinData(
                    ApiConstants.BIN_GROUPS,
                    ApiConstants.KEY_GROUPS,
                    currentGroups
                )
                localGroupsCache.clear()
                localGroupsCache.addAll(currentGroups)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.success(true)
        }
    }
}
