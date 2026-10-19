package com.example.quoteverse.core.design.components

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.model.PostType
import com.example.quoteverse.theme.AccentRose
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuoteCard(
    post: Post,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    onReportClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    val isMyPost = post.authorId == currentUserId

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: User info + Category + Overflow menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUserClick(post.authorId) }
                ) {
                    UserAvatar(
                        imageUrl = post.authorPhotoUrl,
                        displayName = post.authorDisplayName.ifBlank { post.authorUsername },
                        size = 42.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = post.authorDisplayName.ifBlank { post.authorUsername },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "@${post.authorUsername}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = " • ${formatTimestamp(post.createdAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Category Tag
                if (post.categoryName.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = post.categoryName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Menu options
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (isMyPost && onDeleteClick != null) {
                            DropdownMenuItem(
                                text = { Text("Delete Post", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                }
                            )
                        } else if (!isMyPost && onReportClick != null) {
                            DropdownMenuItem(
                                text = { Text("Report Post") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Flag, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onReportClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Content Area
            if (post.postType == PostType.UPLOADED_IMAGE && !post.imageUrl.isNullOrBlank()) {
                // Uploaded Photo
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = post.caption.ifBlank { post.text },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                // Designed or Text Quote Canvas
                val backgroundBrush = if (post.gradientColors.isNotEmpty()) {
                    Brush.linearGradient(post.gradientColors.map { Color(it) })
                } else {
                    Brush.linearGradient(listOf(Color(post.backgroundColor), Color(post.backgroundColor)))
                }

                val font = when (post.fontFamily) {
                    "serif" -> FontFamily.Serif
                    "monospace" -> FontFamily.Monospace
                    "cursive" -> FontFamily.Cursive
                    else -> FontFamily.Default
                }

                val alignment = when (post.textAlign) {
                    "start" -> TextAlign.Start
                    "end" -> TextAlign.End
                    else -> TextAlign.Center
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(backgroundBrush)
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = when (post.textAlign) {
                            "start" -> Alignment.Start
                            "end" -> Alignment.End
                            else -> Alignment.CenterHorizontally
                        }
                    ) {
                        // Decorative Opening Quote Symbol
                        Text(
                            text = "“",
                            color = Color(post.textColor).copy(alpha = 0.35f),
                            fontSize = 44.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = post.text,
                            color = Color(post.textColor),
                            fontSize = 20.sp,
                            lineHeight = 30.sp,
                            fontFamily = font,
                            textAlign = alignment,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "— @${post.authorUsername}",
                            color = Color(post.textColor).copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Caption (if any)
            if (post.caption.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Bar: Likes, Comments, Bookmark, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Like button with scale bounce
                    val heartScale by animateFloatAsState(
                        targetValue = if (post.isLikedByMe) 1.2f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "heartScale"
                    )
                    val heartColor by animateColorAsState(
                        targetValue = if (post.isLikedByMe) AccentRose else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "heartColor"
                    )

                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = heartColor,
                            modifier = Modifier.scale(heartScale)
                        )
                    }
                    Text(
                        text = "${post.likeCount}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Comment button
                    IconButton(
                        onClick = onCommentClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${post.commentCount}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bookmark / Save button
                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isSavedByMe) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (post.isSavedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // External Share button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                val shareText = buildString {
                                    appendLine("“${post.text}”")
                                    appendLine("— @${post.authorUsername} on QuoteVerse")
                                    if (post.caption.isNotBlank()) {
                                        appendLine(post.caption)
                                    }
                                }
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Quote"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "Just now"
    val diff = System.currentTimeMillis() - timestamp.toDate().time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            sdf.format(Date(timestamp.toDate().time))
        }
    }
}
