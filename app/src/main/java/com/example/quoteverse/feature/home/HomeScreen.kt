package com.example.quoteverse.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quoteverse.core.design.components.EmptyStateView
import com.example.quoteverse.core.design.components.ErrorStateView
import com.example.quoteverse.core.design.components.FeedNativeAdItem
import com.example.quoteverse.core.design.components.QuoteCard
import com.example.quoteverse.core.design.components.QuoteCardSkeleton
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.model.ReportReason
import com.example.quoteverse.core.model.ReportTargetType
import com.example.quoteverse.feature.moderation.ReportDialog
import com.example.quoteverse.theme.BrandPrimaryDark
import com.example.quoteverse.theme.BrandPrimaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToComments: (String) -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var reportingPost by remember { mutableStateOf<Post?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(BrandPrimaryLight, BrandPrimaryDark))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FormatQuote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "QuoteVerse",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Feed Tabs: For You, Following, Trending
            PrimaryTabRow(
                selectedTabIndex = uiState.currentTab.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(
                    selected = uiState.currentTab == HomeFeedTab.FOR_YOU,
                    onClick = { viewModel.selectTab(HomeFeedTab.FOR_YOU) },
                    text = { Text("For You", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = uiState.currentTab == HomeFeedTab.FOLLOWING,
                    onClick = { viewModel.selectTab(HomeFeedTab.FOLLOWING) },
                    text = { Text("Following", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = uiState.currentTab == HomeFeedTab.TRENDING,
                    onClick = { viewModel.selectTab(HomeFeedTab.TRENDING) },
                    text = { Text("Trending", fontWeight = FontWeight.SemiBold) }
                )
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(4) {
                                QuoteCardSkeleton()
                            }
                        }
                    }

                    uiState.errorMessage != null -> {
                        ErrorStateView(
                            message = uiState.errorMessage ?: "Failed to load feed",
                            onRetry = { viewModel.loadFeed() }
                        )
                    }

                    uiState.posts.isEmpty() -> {
                        when (uiState.currentTab) {
                            HomeFeedTab.FOLLOWING -> {
                                EmptyStateView(
                                    title = "No posts from followed creators",
                                    message = "Follow talented writers and poets to see their newest verses here.",
                                    icon = Icons.Outlined.People,
                                    actionLabel = "Discover Creators",
                                    onActionClick = onNavigateToExplore
                                )
                            }
                            HomeFeedTab.TRENDING -> {
                                EmptyStateView(
                                    title = "No trending quotes yet",
                                    message = "Trending quotes will appear as creators publish and like posts.",
                                    icon = Icons.Outlined.Whatshot
                                )
                            }
                            else -> {
                                EmptyStateView(
                                    title = "Welcome to QuoteVerse",
                                    message = "No posts published yet. Be the first to share an inspiring quote or shayari!",
                                    icon = Icons.Outlined.FormatQuote
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(
                                items = uiState.posts,
                                key = { _, post -> post.postId }
                            ) { index, post ->
                                QuoteCard(
                                    post = post,
                                    currentUserId = viewModel.currentUserId,
                                    onLikeClick = { viewModel.toggleLike(post) },
                                    onCommentClick = { onNavigateToComments(post.postId) },
                                    onBookmarkClick = { viewModel.toggleBookmark(post) },
                                    onUserClick = { authorId -> onNavigateToUserProfile(authorId) },
                                    onDeleteClick = { viewModel.deletePost(post.postId) },
                                    onReportClick = { reportingPost = post }
                                )

                                // Periodic monetization placeholder (native ad after every 5 posts)
                                if ((index + 1) % 5 == 0 && index != uiState.posts.lastIndex) {
                                    FeedNativeAdItem()
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Content Report Dialog
    if (reportingPost != null) {
        ReportDialog(
            targetTitle = "Report Post",
            targetSubtitle = "By @${reportingPost?.authorUsername}",
            onDismiss = { reportingPost = null },
            onSubmitReport = { reason, details ->
                // Handled in dialog / repository
                reportingPost = null
            }
        )
    }
}
