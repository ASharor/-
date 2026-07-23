package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Profile
import com.example.data.repository.SessionManager
import com.example.data.repository.UserRepository
import com.example.utils.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    val sessionManager = SessionManager(application)

    private val _profile = MutableStateFlow(Profile())
    val profile: StateFlow<Profile> = _profile.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _language = MutableStateFlow("fa")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _allowGroupInvites = MutableStateFlow(true)
    val allowGroupInvites: StateFlow<Boolean> = _allowGroupInvites.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.isDarkMode.collect { _isDarkMode.value = it }
        }
        viewModelScope.launch {
            sessionManager.language.collect { _language.value = it }
        }
    }

    fun loadProfile(username: String) {
        viewModelScope.launch {
            val res = userRepository.getProfile(username)
            if (res.isSuccess) {
                _profile.value = res.getOrThrow()
            }
        }
    }

    fun updateProfilePicture(bitmap: Bitmap) {
        val base64 = ImageUtils.compressBitmapToBase64(bitmap, maxDimension = 250, quality = 65)
        _profile.value = _profile.value.copy(avatar = base64)
    }

    fun setBio(bio: String) {
        _profile.value = _profile.value.copy(bio = bio)
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            _isDarkMode.value = isDark
            sessionManager.updateDarkMode(isDark)
        }
    }

    fun toggleLanguage(lang: String) {
        viewModelScope.launch {
            _language.value = lang
            sessionManager.updateLanguage(lang)
        }
    }

    fun toggleGroupInvites(allow: Boolean) {
        _allowGroupInvites.value = allow
    }

    fun saveSettings(username: String, displayName: String, bio: String) {
        viewModelScope.launch {
            val updated = _profile.value.copy(
                username = username,
                bio = bio
            )
            userRepository.saveProfile(updated)
            sessionManager.saveSession(username, displayName)
            _uiMessage.value = "Settings saved successfully"
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }
}
