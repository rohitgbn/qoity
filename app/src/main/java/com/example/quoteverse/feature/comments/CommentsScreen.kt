package com.example.quoteverse.feature.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quoteverse.core.design.components.EmptyStateView
import com.example.quoteverse.core.design.components.UserAvatar
import com.example.quoteverse.core.design.components.formatTimestamp
import com.example.quoteverse.core.model.Comment
import com.example.quoteverse.feature.moderation.ReportDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    postId: String,
    viewModel: CommentsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var reportingComment by remember { mutableStateOf<Comment?>(null) }

    LaunchedEffect(postId) {
        viewModel.loadComments(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comments", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            // Comment input field at bottom
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.commentText,
                        onValueChange = { viewModel.onCommentTextChanged(it) },
                        placeholder = { Text("Add a thoughtful comment...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                focusManager.clearFocus()
                                viewModel.postComment(postId)
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.postComment(postId)
                        },
                        enabled = uiState.commentText.isNotBlank() && !uiState.isPosting,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (uiState.commentText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        if (uiState.isPosting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                contentDescription = "Post Comment",
                                tint = if (uiState.commentText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Post Snippet Header
            uiState.post?.let { post ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "“${post.text}”",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Serif,
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "— @${post.authorUsername}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider()
            }

            // Comments List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.comments.isEmpty()) {
                EmptyStateView(
                    title = "No comments yet",
                    message = "Be the first to share your thoughts on this verse!",
                    icon = Icons.Outlined.ChatBubbleOutline
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.comments, key = { it.commentId }) { comment ->
                        CommentRowItem(
                            comment = comment,
                            currentUserId = viewModel.currentUserId,
                            onUserClick = { onNavigateToUserProfile(comment.authorId) },
                            onDeleteClick = { viewModel.deleteComment(postId, comment.commentId) },
                            onReportClick = { reportingComment = comment }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }

    if (reportingComment != null) {
        ReportDialog(
            targetTitle = "Report Comment",
            targetSubtitle = "Comment by @${reportingComment?.authorUsername}",
            onDismiss = { reportingComment = null },
            onSubmitReport = { _, _ -> reportingComment = null }
        )
    }
}

@Composable
fun CommentRowItem(
    comment: Comment,
    currentUserId: String,
    onUserClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit
) {
    val isMyComment = comment.authorId == currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        UserAvatar(
            imageUrl = comment.authorPhotoUrl,
            displayName = comment.authorDisplayName.ifBlank { comment.authorUsername },
            size = 38.dp,
            onClick = onUserClick
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.authorDisplayName.ifBlank { comment.authorUsername },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onUserClick() }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• ${formatTimestamp(comment.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (isMyComment) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete comment",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            IconButton(
                onClick = onReportClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = "Report comment",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
