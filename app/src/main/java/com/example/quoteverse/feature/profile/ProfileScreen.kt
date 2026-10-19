package com.example.quoteverse.feature.profile

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quoteverse.core.design.components.EmptyStateView
import com.example.quoteverse.core.design.components.QuoteCard
import com.example.quoteverse.core.design.components.UserAvatar
import com.example.quoteverse.core.model.User
import com.example.quoteverse.feature.moderation.ReportDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String? = null,
    viewModel: ProfileViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToComments: (String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.user != null) "@${uiState.user?.username}" else "Profile",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (uiState.isMyProfile) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                        }
                    } else {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Outlined.MoreVert, contentDescription = "Options")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (uiState.isBlocked) "Unblock User" else "Block User") },
                                    leadingIcon = { Icon(Icons.Outlined.Block, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.toggleBlock()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Report User") },
                                    leadingIcon = { Icon(Icons.Outlined.Flag, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        showReportDialog = true
                                    }
                                )
                            }
                        }
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
                uiState.isLoading && uiState.user == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.user != null -> {
                    val user = uiState.user!!

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // User Header
                        item {
                            ProfileHeader(
                                user = user,
                                isMyProfile = uiState.isMyProfile,
                                isFollowing = uiState.isFollowing,
                                isBlocked = uiState.isBlocked,
                                onFollowClick = { viewModel.toggleFollow() },
                                onEditProfileClick = onNavigateToEditProfile
                            )
                        }

                        // Profile Tabs
                        item {
                            PrimaryTabRow(
                                selectedTabIndex = uiState.selectedTab.ordinal,
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.primary,
                                divider = {}
                            ) {
                                Tab(
                                    selected = uiState.selectedTab == ProfileTab.POSTS,
                                    onClick = { viewModel.selectTab(ProfileTab.POSTS) },
                                    text = { Text("Quotes (${uiState.publishedPosts.size})") },
                                    icon = { Icon(Icons.Outlined.GridOn, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                                Tab(
                                    selected = uiState.selectedTab == ProfileTab.LIKED,
                                    onClick = { viewModel.selectTab(ProfileTab.LIKED) },
                                    text = { Text("Liked") },
                                    icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                                // Private saved posts tab (only visible to owner)
                                if (uiState.isMyProfile) {
                                    Tab(
                                        selected = uiState.selectedTab == ProfileTab.SAVED,
                                        onClick = { viewModel.selectTab(ProfileTab.SAVED) },
                                        text = { Text("Saved") },
                                        icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                    )
                                }
                            }
                            HorizontalDivider()
                        }

                        // Feed of posts based on selected tab
                        val postsToShow = when (uiState.selectedTab) {
                            ProfileTab.POSTS -> uiState.publishedPosts
                            ProfileTab.LIKED -> uiState.likedPosts
                            ProfileTab.SAVED -> uiState.savedPosts
                        }

                        if (postsToShow.isEmpty()) {
                            item {
                                val emptyTitle = when (uiState.selectedTab) {
                                    ProfileTab.POSTS -> if (uiState.isMyProfile) "You haven't posted any quotes yet" else "No quotes published yet"
                                    ProfileTab.LIKED -> "No liked quotes yet"
                                    ProfileTab.SAVED -> "No saved bookmarks yet"
                                }
                                val emptyDesc = when (uiState.selectedTab) {
                                    ProfileTab.POSTS -> "Create and publish your first original verse to build your social portfolio."
                                    ProfileTab.LIKED -> "Quotes you like across QuoteVerse will be collected here."
                                    ProfileTab.SAVED -> "Tap the bookmark icon on any quote to save it to your private library."
                                }
                                EmptyStateView(
                                    title = emptyTitle,
                                    message = emptyDesc,
                                    icon = Icons.Outlined.FormatQuote
                                )
                            }
                        } else {
                            items(postsToShow, key = { it.postId }) { post ->
                                QuoteCard(
                                    post = post,
                                    currentUserId = viewModel.currentUserId,
                                    onLikeClick = {},
                                    onCommentClick = { onNavigateToComments(post.postId) },
                                    onBookmarkClick = {},
                                    onUserClick = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReportDialog && uiState.user != null) {
        ReportDialog(
            targetTitle = "Report User",
            targetSubtitle = "@${uiState.user?.username}",
            onDismiss = { showReportDialog = false },
            onSubmitReport = { _, _ -> showReportDialog = false }
        )
    }
}

@Composable
fun ProfileHeader(
    user: User,
    isMyProfile: Boolean,
    isFollowing: Boolean,
    isBlocked: Boolean,
    onFollowClick: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            UserAvatar(imageUrl = user.profileImageUrl, displayName = user.displayName, size = 76.dp)

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                ProfileStatColumn(count = user.postsCount, label = "Quotes")
                ProfileStatColumn(count = user.followersCount, label = "Followers")
                ProfileStatColumn(count = user.followingCount, label = "Following")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = user.displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (user.bio.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        if (isMyProfile) {
            OutlinedButton(
                onClick = onEditProfileClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile", fontWeight = FontWeight.SemiBold)
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isBlocked) {
                    FilledTonalButton(
                        onClick = onFollowClick,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text("Blocked", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                } else {
                    Button(
                        onClick = onFollowClick,
                        colors = if (isFollowing) ButtonDefaults.filledTonalButtonColors() else ButtonDefaults.buttonColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text(
                            text = if (isFollowing) "Following" else "Follow",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun ProfileStatColumn(count: Long, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
