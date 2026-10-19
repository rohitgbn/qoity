package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.User
import com.example.quoteverse.core.result.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    val isUserLoggedIn: Boolean
    fun getCurrentUserFlow(): Flow<User?>
    suspend fun getCurrentUser(): User?
    fun signInWithEmail(email: String, password: String): Flow<Resource<User>>
    fun signUpWithEmail(email: String, password: String, username: String, displayName: String): Flow<Resource<User>>
    fun signInWithGoogle(idToken: String): Flow<Resource<User>>
    fun sendPasswordResetEmail(email: String): Flow<Resource<Unit>>
    fun signOut()
    fun deleteAccount(): Flow<Resource<Unit>>
    fun checkUsernameAvailability(username: String): Flow<Resource<Boolean>>
}
