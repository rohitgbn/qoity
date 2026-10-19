package com.example.quoteverse.core.model

import com.google.firebase.Timestamp

enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW,
    REPLY
}

data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val actorId: String = "",
    val actorUsername: String = "",
    val actorDisplayName: String = "",
    val actorPhotoUrl: String? = null,
    val type: NotificationType = NotificationType.LIKE,
    val postId: String? = null,
    val postSnippet: String? = null,
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)
