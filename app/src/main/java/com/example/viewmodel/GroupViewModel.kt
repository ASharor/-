package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Group
import com.example.data.models.Status
import com.example.data.repository.GroupRepository
import com.example.data.repository.StatusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroupViewModel : ViewModel() {

    private val groupRepository = GroupRepository()
    private val statusRepository = StatusRepository()

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    private val _memberStatusMap = MutableStateFlow<Map<String, Status>>(emptyMap())
    val memberStatusMap: StateFlow<Map<String, Status>> = _memberStatusMap.asStateFlow()

    private val _selectedGroup = MutableStateFlow<Group?>(null)
    val selectedGroup: StateFlow<Group?> = _selectedGroup.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun loadGroups(username: String) {
        viewModelScope.launch {
            val res = groupRepository.getGroups()
            val allGroups = res.getOrDefault(emptyList())
            val userGroups = allGroups.filter { group ->
                group.members.any { member -> member.equals(username, ignoreCase = true) }
            }
            _groups.value = userGroups

            // Load live member statuses
            val statusRes = statusRepository.getStatusList()
            val statusList = statusRes.getOrDefault(emptyList())
            val map = statusList.associateBy { it.username.lowercase() }
            _memberStatusMap.value = map
        }
    }

    fun createGroup(name: String, owner: String, avatar: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val res = groupRepository.createGroup(name.trim(), owner, avatar)
            if (res.isSuccess) {
                _uiMessage.value = "Group created successfully"
                loadGroups(owner)
            }
        }
    }

    fun selectGroup(group: Group) {
        _selectedGroup.value = group
    }

    fun addMember(groupId: String, rawUsername: String, currentUsername: String) {
        if (rawUsername.isBlank()) return
        viewModelScope.launch {
            val res = groupRepository.addMemberToGroup(groupId, rawUsername)
            if (res.isSuccess) {
                loadGroups(currentUsername)
                _selectedGroup.value = _groups.value.find { it.id == groupId }
            }
        }
    }

    fun leaveGroup(groupId: String, username: String) {
        viewModelScope.launch {
            groupRepository.leaveGroup(groupId, username)
            _selectedGroup.value = null
            loadGroups(username)
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }
}
