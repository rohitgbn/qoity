package com.example.quoteverse.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.model.User
import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.AuthRepository
import com.example.quoteverse.data.repository.InteractionRepository
import com.example.quoteverse.data.repository.ModerationRepository
import com.example.quoteverse.data.repository.PostRepository
import com.example.quoteverse.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class ProfileTab {
    POSTS,
    LIKED,
    SAVED // Only available on own profile
}

data class ProfileUiState(
    val user: User? = null,
    val isMyProfile: Boolean = false,
    val selectedTab: ProfileTab = ProfileTab.POSTS,
    val publishedPosts: List<Post> = emptyList(),
    val likedPosts: List<Post> = emptyList(),
    val savedPosts: List<Post> = emptyList(),
    val isFollowing: Boolean = false,
    val isBlocked: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val interactionRepository: InteractionRepository,
    private val moderationRepository: ModerationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val currentUserId: String get() = authRepository.currentUserId ?: ""

    fun loadProfile(targetUserId: String? = null) {
        val uid = targetUserId ?: currentUserId
        if (uid.isBlank()) return

        val isMine = uid == currentUserId
        _uiState.update { it.copy(isMyProfile = isMine, isLoading = true) }

        viewModelScope.launch {
            // Load user data
            userRepository.getUserProfile(uid).collect { res ->
                when (res) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                user = res.data,
                                isFollowing = res.data.isFollowedByMe,
                                isBlocked = res.data.isBlockedByMe,
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(errorMessage = res.message, isLoading = false) }
                    }
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }

            // Load published posts
            postRepository.getUserPosts(uid).collect { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(publishedPosts = res.data) }
                }
            }

            // Load liked posts
            interactionRepository.getLikedPosts(uid).collect { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(likedPosts = res.data) }
                }
            }

            // Load saved posts (only if my profile)
            if (isMine) {
                interactionRepository.getSavedPosts(uid).collect { res ->
                    if (res is Resource.Success) {
                        _uiState.update { it.copy(savedPosts = res.data) }
                    }
                }
            }
        }
    }

    fun selectTab(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleFollow() {
        val targetUser = _uiState.value.user ?: return
        val currentUid = currentUserId
        if (currentUid.isBlank() || _uiState.value.isMyProfile) return

        val currentlyFollowing = _uiState.value.isFollowing
        val newFollowersCount = if (currentlyFollowing) {
            (targetUser.followersCount - 1).coerceAtLeast(0)
        } else {
            targetUser.followersCount + 1
        }

        _uiState.update {
            it.copy(
                isFollowing = !currentlyFollowing,
                user = it.user?.copy(followersCount = newFollowersCount)
            )
        }

        viewModelScope.launch {
            if (currentlyFollowing) {
                userRepository.unfollowUser(currentUid, targetUser.userId).collect {}
            } else {
                userRepository.followUser(currentUid, targetUser.userId).collect {}
            }
        }
    }

    fun toggleBlock() {
        val targetUser = _uiState.value.user ?: return
        val currentUid = currentUserId
        if (currentUid.isBlank() || _uiState.value.isMyProfile) return

        val currentlyBlocked = _uiState.value.isBlocked
        _uiState.update { it.copy(isBlocked = !currentlyBlocked) }

        viewModelScope.launch {
            if (currentlyBlocked) {
                moderationRepository.unblockUser(currentUid, targetUser.userId).collect {}
            } else {
                moderationRepository.blockUser(currentUid, targetUser.userId).collect {}
            }
        }
    }

    fun updateProfile(displayName: String, bio: String, avatarFile: File?) {
        val uid = currentUserId
        if (uid.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.updateProfile(uid, displayName, bio, avatarFile).collect { res ->
                when (res) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(user = res.data, isLoading = false) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(errorMessage = res.message, isLoading = false) }
                    }
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
