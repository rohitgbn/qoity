package com.example.quoteverse.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.AuthRepository
import com.example.quoteverse.data.repository.InteractionRepository
import com.example.quoteverse.data.repository.ModerationRepository
import com.example.quoteverse.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeFeedTab {
    FOR_YOU,
    FOLLOWING,
    TRENDING
}

data class HomeUiState(
    val currentTab: HomeFeedTab = HomeFeedTab.FOR_YOU,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val blockedUserIds: Set<String> = emptySet()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val interactionRepository: InteractionRepository,
    private val moderationRepository: ModerationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val currentUserId: String get() = authRepository.currentUserId ?: ""

    init {
        loadBlockedUsers()
        loadFeed(HomeFeedTab.FOR_YOU)
    }

    private fun loadBlockedUsers() {
        val uid = currentUserId
        if (uid.isNotBlank()) {
            viewModelScope.launch {
                moderationRepository.getBlockedUserIds(uid).collect { res ->
                    if (res is Resource.Success) {
                        _uiState.update { it.copy(blockedUserIds = res.data) }
                    }
                }
            }
        }
    }

    fun selectTab(tab: HomeFeedTab) {
        if (_uiState.value.currentTab != tab) {
            _uiState.update { it.copy(currentTab = tab) }
            loadFeed(tab)
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadFeed(_uiState.value.currentTab, isRefresh = true)
    }

    fun loadFeed(tab: HomeFeedTab = _uiState.value.currentTab, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            val flow = when (tab) {
                HomeFeedTab.FOR_YOU -> postRepository.getLatestPosts()
                HomeFeedTab.FOLLOWING -> postRepository.getFollowingFeed(currentUserId)
                HomeFeedTab.TRENDING -> postRepository.getTrendingPosts()
            }

            flow.collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        if (!isRefresh) _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val filtered = result.data.filter { it.authorId !in _uiState.value.blockedUserIds }
                        _uiState.update {
                            it.copy(
                                posts = filtered,
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun toggleLike(post: Post) {
        val uid = currentUserId
        if (uid.isBlank()) return

        val isLiked = post.isLikedByMe
        val newLikeCount = if (isLiked) (post.likeCount - 1).coerceAtLeast(0) else post.likeCount + 1

        // Optimistic UI update
        updatePostInState(post.postId) { it.copy(isLikedByMe = !isLiked, likeCount = newLikeCount) }

        viewModelScope.launch {
            if (isLiked) {
                interactionRepository.unlikePost(post.postId, uid).collect {}
            } else {
                interactionRepository.likePost(post.postId, uid, post.authorId).collect {}
            }
        }
    }

    fun toggleBookmark(post: Post) {
        val uid = currentUserId
        if (uid.isBlank()) return

        val isSaved = post.isSavedByMe
        val newBookmarkCount = if (isSaved) (post.bookmarkCount - 1).coerceAtLeast(0) else post.bookmarkCount + 1

        // Optimistic UI update
        updatePostInState(post.postId) { it.copy(isSavedByMe = !isSaved, bookmarkCount = newBookmarkCount) }

        viewModelScope.launch {
            if (isSaved) {
                interactionRepository.unbookmarkPost(post.postId, uid).collect {}
            } else {
                interactionRepository.bookmarkPost(post.postId, uid).collect {}
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(posts = state.posts.filterNot { it.postId == postId })
            }
            postRepository.deletePost(postId).collect {}
        }
    }

    private fun updatePostInState(postId: String, transform: (Post) -> Post) {
        _uiState.update { state ->
            val updated = state.posts.map { if (it.postId == postId) transform(it) else it }
            state.copy(posts = updated)
        }
    }
}
