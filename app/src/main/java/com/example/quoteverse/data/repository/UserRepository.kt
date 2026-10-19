package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.User
import com.example.quoteverse.core.result.Resource
import kotlinx.coroutines.flow.Flow
import java.io.File

interface UserRepository {
    fun getUserProfile(userId: String): Flow<Resource<User>>
    fun updateProfile(userId: String, displayName: String, bio: String, avatarFile: File? = null): Flow<Resource<User>>
    fun followUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>>
    fun unfollowUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>>
    fun isFollowing(currentUserId: String, targetUserId: String): Flow<Boolean>
    fun getFollowers(userId: String): Flow<Resource<List<User>>>
    fun getFollowing(userId: String): Flow<Resource<List<User>>>
    fun searchUsers(query: String): Flow<Resource<List<User>>>
    fun getPopularCreators(): Flow<Resource<List<User>>>
}
