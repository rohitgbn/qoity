package com.example.quoteverse.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quoteverse.core.model.NotificationItem
import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.AuthRepository
import com.example.quoteverse.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    val currentUserId: String get() = authRepository.currentUserId ?: ""

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        val uid = currentUserId
        if (uid.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            notificationRepository.getNotifications(uid).collect { res ->
                when (res) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(notifications = res.data, isLoading = false) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(errorMessage = res.message, isLoading = false) }
                    }
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        val uid = currentUserId
        if (uid.isBlank()) return

        viewModelScope.launch {
            _uiState.update { state ->
                val updated = state.notifications.map {
                    if (it.id == notificationId) it.copy(isRead = true) else it
                }
                state.copy(notifications = updated)
            }
            notificationRepository.markNotificationAsRead(uid, notificationId).collect {}
        }
    }
}
