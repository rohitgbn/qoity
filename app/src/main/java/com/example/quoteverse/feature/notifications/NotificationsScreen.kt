package com.example.quoteverse.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quoteverse.core.design.components.EmptyStateView
import com.example.quoteverse.core.design.components.UserAvatar
import com.example.quoteverse.core.design.components.formatTimestamp
import com.example.quoteverse.core.model.NotificationItem
import com.example.quoteverse.core.model.NotificationType
import com.example.quoteverse.theme.AccentRose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToComments: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity", fontWeight = FontWeight.Bold) },
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
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.notifications.isEmpty() -> {
                    EmptyStateView(
                        title = "No activity yet",
                        message = "When other creators like your quotes, comment, or follow you, you'll see them here.",
                        icon = Icons.Outlined.Notifications
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.notifications, key = { it.id }) { notif ->
                            NotificationRowItem(
                                item = notif,
                                onClick = {
                                    viewModel.markAsRead(notif.id)
                                    if (notif.postId != null) {
                                        onNavigateToComments(notif.postId)
                                    } else {
                                        onNavigateToUserProfile(notif.actorId)
                                    }
                                }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationRowItem(
    item: NotificationItem,
    onClick: () -> Unit
) {
    val unreadBg = if (!item.isRead) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(unreadBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            UserAvatar(
                imageUrl = item.actorPhotoUrl,
                displayName = item.actorDisplayName.ifBlank { item.actorUsername },
                size = 46.dp
            )
            // Small badge icon
            val badgeIcon = when (item.type) {
                NotificationType.LIKE -> Icons.Filled.Favorite
                NotificationType.COMMENT, NotificationType.REPLY -> Icons.Outlined.ChatBubbleOutline
                NotificationType.FOLLOW -> Icons.Filled.PersonAdd
            }
            val badgeColor = when (item.type) {
                NotificationType.LIKE -> AccentRose
                NotificationType.COMMENT, NotificationType.REPLY -> MaterialTheme.colorScheme.primary
                NotificationType.FOLLOW -> MaterialTheme.colorScheme.secondary
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.BottomEnd)
                    .background(badgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            val actorName = item.actorDisplayName.ifBlank { "@${item.actorUsername}" }
            val actionText = when (item.type) {
                NotificationType.LIKE -> "liked your quote: "
                NotificationType.COMMENT -> "commented on your quote: "
                NotificationType.FOLLOW -> "started following you"
                NotificationType.REPLY -> "replied to your comment"
            }

            val text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                    append(actorName)
                }
                append(" ")
                append(actionText)
                if (item.postSnippet != null) {
                    withStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                        append("“${item.postSnippet}”")
                    }
                }
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatTimestamp(item.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!item.isRead) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}
