package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.NotificationItem
import com.example.quoteverse.core.result.Resource
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<Resource<List<NotificationItem>>>
    fun markNotificationAsRead(userId: String, notificationId: String): Flow<Resource<Unit>>
    fun createNotification(notification: NotificationItem): Flow<Resource<Unit>>
    fun updateFcmToken(userId: String, token: String): Flow<Resource<Unit>>
}
