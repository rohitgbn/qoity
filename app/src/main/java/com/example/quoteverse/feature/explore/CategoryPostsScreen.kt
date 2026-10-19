package com.example.quoteverse.feature.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.quoteverse.core.design.components.EmptyStateView
import com.example.quoteverse.core.design.components.ErrorStateView
import com.example.quoteverse.core.design.components.QuoteCard
import com.example.quoteverse.core.design.components.QuoteCardSkeleton
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.PostRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPostsScreen(
    categoryId: String,
    categoryName: String,
    postRepository: PostRepository,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onNavigateToComments: (String) -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(categoryId) {
        isLoading = true
        postRepository.getPostsByCategory(categoryId).collect { res ->
            when (res) {
                is Resource.Loading -> isLoading = true
                is Resource.Success -> {
                    posts = res.data
                    isLoading = false
                    errorMessage = null
                }
                is Resource.Error -> {
                    isLoading = false
                    errorMessage = res.message
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(3) { QuoteCardSkeleton() }
                    }
                }
                errorMessage != null -> {
                    ErrorStateView(message = errorMessage ?: "Failed to load posts")
                }
                posts.isEmpty() -> {
                    EmptyStateView(
                        title = "No quotes in $categoryName yet",
                        message = "Be the first to create and publish a quote in this category!",
                        icon = Icons.Outlined.FormatQuote
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(posts, key = { it.postId }) { post ->
                            QuoteCard(
                                post = post,
                                currentUserId = currentUserId,
                                onLikeClick = {},
                                onCommentClick = { onNavigateToComments(post.postId) },
                                onBookmarkClick = {},
                                onUserClick = onNavigateToUserProfile
                            )
                        }
                    }
                }
            }
        }
    }
}
