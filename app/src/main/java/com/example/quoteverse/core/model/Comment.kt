package com.example.quoteverse.core.model

import com.google.firebase.Timestamp

data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorDisplayName: String = "",
    val authorPhotoUrl: String? = null,
    val text: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isMyComment: Boolean = false
)
