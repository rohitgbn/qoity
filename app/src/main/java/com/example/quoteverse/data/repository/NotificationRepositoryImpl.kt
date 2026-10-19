package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.NotificationItem
import com.example.quoteverse.core.result.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<Resource<List<NotificationItem>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val list = snapshot.toObjects(NotificationItem::class.java)
            emit(Resource.Success(list))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load notifications", e))
        }
    }

    override fun markNotificationAsRead(userId: String, notificationId: String): Flow<Resource<Unit>> = flow {
        try {
            firestore.collection("users").document(userId)
                .collection("notifications").document(notificationId)
                .update("isRead", true)
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to update notification", e))
        }
    }

    override fun createNotification(notification: NotificationItem): Flow<Resource<Unit>> = flow {
        try {
            firestore.collection("users").document(notification.userId)
                .collection("notifications").document(notification.id)
                .set(notification)
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to dispatch notification", e))
        }
    }

    override fun updateFcmToken(userId: String, token: String): Flow<Resource<Unit>> = flow {
        try {
            firestore.collection("users").document(userId)
                .collection("fcmTokens").document(token)
                .set(mapOf(
                    "token" to token,
                    "updatedAt" to Timestamp.now()
                ))
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to update push token", e))
        }
    }
}
