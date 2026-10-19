package com.example.quoteverse.core.model

import com.google.firebase.Timestamp

enum class PostType {
    TEXT,
    DESIGNED_IMAGE,
    UPLOADED_IMAGE
}

enum class ModerationStatus {
    APPROVED,
    PENDING,
    FLAGGED,
    REMOVED
}

data class Post(
    val postId: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorDisplayName: String = "",
    val authorPhotoUrl: String? = null,
    val text: String = "",
    val caption: String = "",
    val imageUrl: String? = null,
    val categoryId: String = "",
    val categoryName: String = "",
    val postType: PostType = PostType.TEXT,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val moderationStatus: ModerationStatus = ModerationStatus.APPROVED,
    val likeCount: Long = 0,
    val commentCount: Long = 0,
    val bookmarkCount: Long = 0,
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false,
    // Styling properties for rendered text posts
    val backgroundColor: Long = 0xFF1E1E2E,
    val textColor: Long = 0xFFFFFFFF,
    val fontFamily: String = "Serif",
    val textAlign: String = "center",
    val gradientColors: List<Long> = emptyList()
)
