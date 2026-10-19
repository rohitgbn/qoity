package com.example.quoteverse.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quoteverse.core.model.Category
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.model.User
import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.CategoryRepository
import com.example.quoteverse.data.repository.InteractionRepository
import com.example.quoteverse.data.repository.PostRepository
import com.example.quoteverse.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val categories: List<Category> = emptyList(),
    val searchPostResults: List<Post> = emptyList(),
    val searchUserResults: List<User> = emptyList(),
    val popularCreators: List<User> = emptyList(),
    val trendingPosts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val interactionRepository: InteractionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadInitialData()
        observeSearch()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Categories
            categoryRepository.getCategories().collect { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(categories = res.data) }
                }
            }

            // Popular Creators
            userRepository.getPopularCreators().collect { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(popularCreators = res.data) }
                }
            }

            // Trending Posts
            postRepository.getTrendingPosts(6).collect { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(trendingPosts = res.data, isLoading = false) }
                } else if (res is Resource.Error) {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun observeSearch() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(350)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                searchPostResults = emptyList(),
                                searchUserResults = emptyList()
                            )
                        }
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery, isSearching = newQuery.isNotBlank()) }
        searchQueryFlow.value = newQuery
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            // Search Posts
            postRepository.searchPosts(query).collect { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(searchPostResults = res.data) }
                }
            }

            // Search Users
            userRepository.searchUsers(query).collect { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(searchUserResults = res.data) }
                }
            }
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", isSearching = false) }
        searchQueryFlow.value = ""
    }
}
