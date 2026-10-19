package com.example.quoteverse.core.model

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val username: String = "",
    val usernameLower: String = "",
    val displayName: String = "",
    val email: String = "",
    val bio: String = "",
    val profileImageUrl: String? = null,
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val postsCount: Long = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isFollowingMe: Boolean = false,
    val isFollowedByMe: Boolean = false,
    val isBlockedByMe: Boolean = false
)
