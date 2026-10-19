package com.example.quoteverse.feature.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quoteverse.core.model.Comment
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.AuthRepository
import com.example.quoteverse.data.repository.InteractionRepository
import com.example.quoteverse.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommentsUiState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val commentText: String = "",
    val isLoading: Boolean = false,
    val isPosting: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val interactionRepository: InteractionRepository,
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    val currentUserId: String get() = authRepository.currentUserId ?: ""

    fun loadComments(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load Post
            postRepository.getPostById(postId).collect { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(post = res.data) }
                }
            }

            // Load Comments
            interactionRepository.getComments(postId).collect { res ->
                when (res) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(comments = res.data, isLoading = false) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(errorMessage = res.message, isLoading = false) }
                    }
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun onCommentTextChanged(text: String) {
        if (text.length <= 1000) {
            _uiState.update { it.copy(commentText = text) }
        }
    }

    fun postComment(postId: String) {
        val text = _uiState.value.commentText.trim()
        if (text.isBlank()) return

        val post = _uiState.value.post ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isPosting = true) }
            val currentUser = authRepository.getCurrentUser() ?: return@launch

            interactionRepository.addComment(postId, text, currentUser, post.authorId).collect { res ->
                when (res) {
                    is Resource.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                comments = state.comments + res.data,
                                commentText = "",
                                isPosting = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isPosting = false, errorMessage = res.message) }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(comments = state.comments.filterNot { it.commentId == commentId })
            }
            interactionRepository.deleteComment(postId, commentId).collect {}
        }
    }
}
