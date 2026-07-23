package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.User
import com.example.data.repository.SessionManager
import com.example.data.repository.UserRepository
import com.example.utils.DateUtils
import com.example.utils.HashUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    val sessionManager = SessionManager(application)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.isLoggedIn.collect { loggedIn ->
                _isLoggedIn.value = loggedIn
            }
        }
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(sessionManager.username, sessionManager.displayName) { username, displayName ->
                if (username.isNotEmpty()) {
                    User(username = username, displayName = displayName.ifEmpty { username })
                } else null
            }.collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun login(username: String, passwordRaw: String) {
        if (username.length < 5) {
            _uiState.value = AuthUiState.Error("username_min_length")
            return
        }
        if (passwordRaw.length < 4) {
            _uiState.value = AuthUiState.Error("password_min_length")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val passHash = HashUtils.sha256(passwordRaw)
            val res = userRepository.loginUser(username, passHash)
            if (res.isSuccess) {
                val user = res.getOrThrow()
                sessionManager.saveSession(user.username, user.displayName.ifEmpty { user.username })
                _currentUser.value = user
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Error("invalid_credentials")
            }
        }
    }

    fun register(username: String, passwordRaw: String, name: String) {
        if (username.length < 5) {
            _uiState.value = AuthUiState.Error("username_min_length")
            return
        }
        if (passwordRaw.length < 4) {
            _uiState.value = AuthUiState.Error("password_min_length")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val passHash = HashUtils.sha256(passwordRaw)
            val newUser = User(
                username = username,
                passwordHash = passHash,
                name = name,
                displayName = name.ifEmpty { username },
                registeredAt = DateUtils.getCurrentIsoDate()
            )
            val res = userRepository.registerUser(newUser)
            if (res.isSuccess) {
                sessionManager.saveSession(username, newUser.displayName)
                _currentUser.value = newUser
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Error("user_exists")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _isLoggedIn.value = false
            _currentUser.value = null
            _uiState.value = AuthUiState.Idle
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val messageKey: String) : AuthUiState()
}
