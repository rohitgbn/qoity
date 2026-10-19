package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.Category
import com.example.quoteverse.core.model.Comment
import com.example.quoteverse.core.model.ModerationStatus
import com.example.quoteverse.core.model.NotificationItem
import com.example.quoteverse.core.model.NotificationType
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.model.PostType
import com.example.quoteverse.core.model.ReportReason
import com.example.quoteverse.core.model.ReportTargetType
import com.example.quoteverse.core.model.User
import com.example.quoteverse.core.result.Resource
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID

/**
 * In-memory repository implementation for local UI preview and offline development.
 * Contains user-created quotes in Hindi, English, and Hinglish across diverse categories.
 */
class MockDataRepository : AuthRepository, PostRepository, InteractionRepository, UserRepository, CategoryRepository, NotificationRepository, ModerationRepository {

    private val currentUserState = MutableStateFlow<User?>(
        User(
            userId = "mock_user_1",
            username = "poetic_soul",
            usernameLower = "poetic_soul",
            displayName = "Aarav Sharma",
            email = "aarav@quoteverse.example",
            bio = "Words that touch the soul • Shayar & Writer • Delhi",
            profileImageUrl = null,
            followersCount = 428,
            followingCount = 189,
            postsCount = 12
        )
    )

    private val postsList = MutableStateFlow(
        mutableListOf(
            Post(
                postId = "post_1",
                authorId = "user_2",
                authorUsername = "rahat_fan",
                authorDisplayName = "Zoya Akhtar",
                authorPhotoUrl = null,
                text = "कभी किसी को मुकम्मल जहाँ नहीं मिलता,\nकहीं ज़मीं तो कहीं आसमाँ नहीं मिलता।",
                caption = "Timeless words on life and acceptance.",
                categoryId = "sad_shayari",
                categoryName = "Sad Shayari",
                postType = PostType.DESIGNED_IMAGE,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0),
                moderationStatus = ModerationStatus.APPROVED,
                likeCount = 142,
                commentCount = 18,
                bookmarkCount = 45,
                isLikedByMe = true,
                isSavedByMe = false,
                backgroundColor = 0xFF0F2027,
                gradientColors = listOf(0xFF0F2027, 0xFF2C5364),
                textColor = 0xFFFFFFFF,
                fontFamily = "serif",
                textAlign = "center"
            ),
            Post(
                postId = "post_2",
                authorId = "user_3",
                authorUsername = "kabir_thoughts",
                authorDisplayName = "Kabir Verma",
                authorPhotoUrl = null,
                text = "खामोशी में भी एक आवाज़ होती है,\nजो सिर्फ समझने वाले ही सुन पाते हैं।",
                caption = "The power of silence in a noisy world. #DeepThoughts",
                categoryId = "life_quotes",
                categoryName = "Life Quotes",
                postType = PostType.DESIGNED_IMAGE,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 7200, 0),
                moderationStatus = ModerationStatus.APPROVED,
                likeCount = 89,
                commentCount = 7,
                bookmarkCount = 22,
                isLikedByMe = false,
                isSavedByMe = true,
                backgroundColor = 0xFF654EA3,
                gradientColors = listOf(0xFF654EA3, 0xFFEAAFC8),
                textColor = 0xFFFFFFFF,
                fontFamily = "serif",
                textAlign = "center"
            ),
            Post(
                postId = "post_3",
                authorId = "mock_user_1",
                authorUsername = "poetic_soul",
                authorDisplayName = "Aarav Sharma",
                authorPhotoUrl = null,
                text = "Your only limit is the courage to stand up after every stumble. Rise again.",
                caption = "Morning motivation for everyone chasing their dreams today!",
                categoryId = "motivation",
                categoryName = "Motivation",
                postType = PostType.TEXT,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 14400, 0),
                moderationStatus = ModerationStatus.APPROVED,
                likeCount = 210,
                commentCount = 34,
                bookmarkCount = 67,
                isLikedByMe = true,
                isSavedByMe = true,
                backgroundColor = 0xFFEE0979,
                gradientColors = listOf(0xFFEE0979, 0xFFFF6A00),
                textColor = 0xFFFFFFFF,
                fontFamily = "sans-serif",
                textAlign = "center"
            ),
            Post(
                postId = "post_4",
                authorId = "user_4",
                authorUsername = "ishqbaaz",
                authorDisplayName = "Meera Sen",
                authorPhotoUrl = null,
                text = "तेरी मुस्कान ही काफी है इस दिन को रौशन करने के लिए।",
                caption = "Pure romance in Devanagari. Written for someone special.",
                categoryId = "love",
                categoryName = "Love",
                postType = PostType.DESIGNED_IMAGE,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 86400, 0),
                moderationStatus = ModerationStatus.APPROVED,
                likeCount = 312,
                commentCount = 41,
                bookmarkCount = 98,
                isLikedByMe = false,
                isSavedByMe = false,
                backgroundColor = 0xFFB24592,
                gradientColors = listOf(0xFFB24592, 0xFFF15F79),
                textColor = 0xFFFFFFFF,
                fontFamily = "serif",
                textAlign = "center"
            ),
            Post(
                postId = "post_5",
                authorId = "user_5",
                authorUsername = "sher_e_hind",
                authorDisplayName = "Rohan Malhotra",
                authorPhotoUrl = null,
                text = "हम वो नहीं जो हवा का रुख देखकर बदलते हैं,\nहम वो हैं जो अपना रुख बदल दें तो हवाएं मुड़ जाती हैं।",
                caption = "Attitude shayari for the bold. #SelfBelief",
                categoryId = "attitude",
                categoryName = "Attitude",
                postType = PostType.DESIGNED_IMAGE,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 100000, 0),
                moderationStatus = ModerationStatus.APPROVED,
                likeCount = 540,
                commentCount = 62,
                bookmarkCount = 112,
                isLikedByMe = true,
                isSavedByMe = false,
                backgroundColor = 0xFF121212,
                textColor = 0xFFF5F5F5,
                fontFamily = "monospace",
                textAlign = "center"
            )
        )
    )

    private val commentsMap = mutableMapOf<String, MutableList<Comment>>(
        "post_1" to mutableListOf(
            Comment(
                commentId = "c1",
                postId = "post_1",
                authorId = "user_3",
                authorUsername = "kabir_thoughts",
                authorDisplayName = "Kabir Verma",
                authorPhotoUrl = null,
                text = "बेहद खूबसूरत पंक्तियाँ! दिल को छू लिया।",
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 1800, 0)
            ),
            Comment(
                commentId = "c2",
                postId = "post_1",
                authorId = "mock_user_1",
                authorUsername = "poetic_soul",
                authorDisplayName = "Aarav Sharma",
                authorPhotoUrl = null,
                text = "Nida Fazli sahab ki classic shayari. Truly unforgettable.",
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 900, 0),
                isMyComment = true
            )
        )
    )

    private val blockedUsersSet = mutableSetOf<String>()
    private val followingUsersSet = mutableSetOf("user_2", "user_3")

    // --- AuthRepository implementation ---
    override val currentUserId: String? get() = currentUserState.value?.userId
    override val isUserLoggedIn: Boolean get() = currentUserState.value != null

    override fun getCurrentUserFlow(): Flow<User?> = currentUserState
    override suspend fun getCurrentUser(): User? = currentUserState.value

    override fun signInWithEmail(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading("Signing in..."))
        delay(600)
        val user = User(
            userId = "mock_user_1",
            username = "poetic_soul",
            usernameLower = "poetic_soul",
            displayName = "Aarav Sharma",
            email = email,
            bio = "Words that touch the soul • Shayar & Writer • Delhi",
            followersCount = 428,
            followingCount = 189,
            postsCount = 12
        )
        currentUserState.value = user
        emit(Resource.Success(user))
    }

    override fun signUpWithEmail(email: String, password: String, username: String, displayName: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading("Creating account..."))
        delay(700)
        val user = User(
            userId = UUID.randomUUID().toString(),
            username = username,
            usernameLower = username.lowercase(),
            displayName = displayName,
            email = email,
            bio = "New creator on QuoteVerse",
            followersCount = 0,
            followingCount = 0,
            postsCount = 0
        )
        currentUserState.value = user
        emit(Resource.Success(user))
    }

    override fun signInWithGoogle(idToken: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading("Authenticating with Google..."))
        delay(800)
        val user = User(
            userId = "google_user_99",
            username = "google_creator",
            usernameLower = "google_creator",
            displayName = "Google User",
            email = "user@gmail.com",
            bio = "Quote enthusiast"
        )
        currentUserState.value = user
        emit(Resource.Success(user))
    }

    override fun sendPasswordResetEmail(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        delay(400)
        emit(Resource.Success(Unit))
    }

    override fun signOut() {
        currentUserState.value = null
    }

    override fun deleteAccount(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading("Deleting your account data..."))
        delay(800)
        postsList.value.removeAll { it.authorId == currentUserId }
        currentUserState.value = null
        emit(Resource.Success(Unit))
    }

    override fun checkUsernameAvailability(username: String): Flow<Resource<Boolean>> = flow {
        delay(300)
        val clean = username.trim().lowercase()
        if (clean.length < 3 || clean.length > 30) {
            emit(Resource.Success(false))
            return@flow
        }
        val isTaken = clean == "admin" || clean == "root" || clean == "poetic_soul"
        emit(Resource.Success(!isTaken))
    }

    // --- PostRepository implementation ---
    override fun getLatestPosts(limit: Long, lastPostId: String?): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        delay(300)
        val filtered = postsList.value.filter { it.authorId !in blockedUsersSet && it.moderationStatus == ModerationStatus.APPROVED }
        emit(Resource.Success(filtered))
    }

    override fun getFollowingFeed(currentUserId: String, limit: Long): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        delay(300)
        val filtered = postsList.value.filter { it.authorId in followingUsersSet && it.authorId !in blockedUsersSet }
        emit(Resource.Success(filtered))
    }

    override fun getTrendingPosts(limit: Long): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        delay(300)
        val trending = postsList.value.sortedByDescending { it.likeCount }.filter { it.authorId !in blockedUsersSet }
        emit(Resource.Success(trending))
    }

    override fun getPostsByCategory(categoryId: String, limit: Long): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        delay(300)
        val filtered = postsList.value.filter { it.categoryId.equals(categoryId, ignoreCase = true) && it.authorId !in blockedUsersSet }
        emit(Resource.Success(filtered))
    }

    override fun getUserPosts(userId: String): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        delay(300)
        val userPosts = postsList.value.filter { it.authorId == userId }
        emit(Resource.Success(userPosts))
    }

    override fun createPost(post: Post, imageFile: File?): Flow<Resource<Post>> = flow {
        emit(Resource.Loading("Publishing quote..."))
        delay(800)
        val newPost = post.copy(
            postId = UUID.randomUUID().toString(),
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            moderationStatus = ModerationStatus.APPROVED
        )
        postsList.value.add(0, newPost)
        currentUserState.value?.let { user ->
            currentUserState.value = user.copy(postsCount = user.postsCount + 1)
        }
        emit(Resource.Success(newPost))
    }

    override fun updatePost(postId: String, text: String, caption: String, categoryId: String, categoryName: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val index = postsList.value.indexOfFirst { it.postId == postId }
        if (index != -1) {
            val old = postsList.value[index]
            postsList.value[index] = old.copy(
                text = text,
                caption = caption,
                categoryId = categoryId,
                categoryName = categoryName,
                updatedAt = Timestamp.now()
            )
        }
        emit(Resource.Success(Unit))
    }

    override fun deletePost(postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        postsList.value.removeAll { it.postId == postId }
        currentUserState.value?.let { user ->
            currentUserState.value = user.copy(postsCount = (user.postsCount - 1).coerceAtLeast(0))
        }
        emit(Resource.Success(Unit))
    }

    override fun searchPosts(query: String): Flow<Resource<List<Post>>> = flow {
        delay(200)
        val results = if (query.isBlank()) emptyList() else {
            postsList.value.filter {
                it.text.contains(query, ignoreCase = true) ||
                it.caption.contains(query, ignoreCase = true) ||
                it.categoryName.contains(query, ignoreCase = true)
            }
        }
        emit(Resource.Success(results))
    }

    override fun getPostById(postId: String): Flow<Resource<Post>> = flow {
        val post = postsList.value.find { it.postId == postId }
        if (post != null) {
            emit(Resource.Success(post))
        } else {
            emit(Resource.Error("Post not found"))
        }
    }

    // --- InteractionRepository implementation ---
    override fun likePost(postId: String, userId: String, authorId: String): Flow<Resource<Unit>> = flow {
        val index = postsList.value.indexOfFirst { it.postId == postId }
        if (index != -1) {
            val old = postsList.value[index]
            if (!old.isLikedByMe) {
                postsList.value[index] = old.copy(likeCount = old.likeCount + 1, isLikedByMe = true)
            }
        }
        emit(Resource.Success(Unit))
    }

    override fun unlikePost(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        val index = postsList.value.indexOfFirst { it.postId == postId }
        if (index != -1) {
            val old = postsList.value[index]
            if (old.isLikedByMe) {
                postsList.value[index] = old.copy(likeCount = (old.likeCount - 1).coerceAtLeast(0), isLikedByMe = false)
            }
        }
        emit(Resource.Success(Unit))
    }

    override fun isPostLiked(postId: String, userId: String): Flow<Boolean> = postsList.map { list ->
        list.find { it.postId == postId }?.isLikedByMe ?: false
    }

    override fun getComments(postId: String): Flow<Resource<List<Comment>>> = flow {
        emit(Resource.Loading())
        delay(200)
        val comments = commentsMap[postId] ?: emptyList()
        emit(Resource.Success(comments))
    }

    override fun addComment(postId: String, text: String, author: User, postAuthorId: String): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())
        val newComment = Comment(
            commentId = UUID.randomUUID().toString(),
            postId = postId,
            authorId = author.userId,
            authorUsername = author.username,
            authorDisplayName = author.displayName,
            authorPhotoUrl = author.profileImageUrl,
            text = text,
            createdAt = Timestamp.now(),
            isMyComment = true
        )
        val list = commentsMap.getOrPut(postId) { mutableListOf() }
        list.add(newComment)

        // Increment post comment count
        val index = postsList.value.indexOfFirst { it.postId == postId }
        if (index != -1) {
            val old = postsList.value[index]
            postsList.value[index] = old.copy(commentCount = old.commentCount + 1)
        }
        emit(Resource.Success(newComment))
    }

    override fun deleteComment(postId: String, commentId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        commentsMap[postId]?.removeAll { it.commentId == commentId }
        val index = postsList.value.indexOfFirst { it.postId == postId }
        if (index != -1) {
            val old = postsList.value[index]
            postsList.value[index] = old.copy(commentCount = (old.commentCount - 1).coerceAtLeast(0))
        }
        emit(Resource.Success(Unit))
    }

    override fun bookmarkPost(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        val index = postsList.value.indexOfFirst { it.postId == postId }
        if (index != -1) {
            val old = postsList.value[index]
            if (!old.isSavedByMe) {
                postsList.value[index] = old.copy(bookmarkCount = old.bookmarkCount + 1, isSavedByMe = true)
            }
        }
        emit(Resource.Success(Unit))
    }

    override fun unbookmarkPost(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        val index = postsList.value.indexOfFirst { it.postId == postId }
        if (index != -1) {
            val old = postsList.value[index]
            if (old.isSavedByMe) {
                postsList.value[index] = old.copy(bookmarkCount = (old.bookmarkCount - 1).coerceAtLeast(0), isSavedByMe = false)
            }
        }
        emit(Resource.Success(Unit))
    }

    override fun isPostBookmarked(postId: String, userId: String): Flow<Boolean> = postsList.map { list ->
        list.find { it.postId == postId }?.isSavedByMe ?: false
    }

    override fun getSavedPosts(userId: String): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        delay(200)
        val saved = postsList.value.filter { it.isSavedByMe }
        emit(Resource.Success(saved))
    }

    override fun getLikedPosts(userId: String): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        delay(200)
        val liked = postsList.value.filter { it.isLikedByMe }
        emit(Resource.Success(liked))
    }

    // --- UserRepository implementation ---
    override fun getUserProfile(userId: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        delay(200)
        if (userId == currentUserId) {
            currentUserState.value?.let { emit(Resource.Success(it)) }
        } else {
            val sampleCreator = User(
                userId = userId,
                username = "creator_$userId",
                usernameLower = "creator_$userId",
                displayName = "Shayari Maestro",
                bio = "Writing heartfelt couplets in Hindi and Urdu • Follow for daily verses",
                followersCount = 1250,
                followingCount = 310,
                postsCount = 48,
                isFollowedByMe = userId in followingUsersSet
            )
            emit(Resource.Success(sampleCreator))
        }
    }

    override fun updateProfile(userId: String, displayName: String, bio: String, avatarFile: File?): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        delay(500)
        val updated = currentUserState.value?.copy(
            displayName = displayName,
            bio = bio,
            updatedAt = Timestamp.now()
        )
        currentUserState.value = updated
        if (updated != null) {
            emit(Resource.Success(updated))
        } else {
            emit(Resource.Error("User not found"))
        }
    }

    override fun followUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>> = flow {
        followingUsersSet.add(targetUserId)
        currentUserState.value?.let { user ->
            currentUserState.value = user.copy(followingCount = user.followingCount + 1)
        }
        emit(Resource.Success(Unit))
    }

    override fun unfollowUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>> = flow {
        followingUsersSet.remove(targetUserId)
        currentUserState.value?.let { user ->
            currentUserState.value = user.copy(followingCount = (user.followingCount - 1).coerceAtLeast(0))
        }
        emit(Resource.Success(Unit))
    }

    override fun isFollowing(currentUserId: String, targetUserId: String): Flow<Boolean> = flow {
        emit(targetUserId in followingUsersSet)
    }

    override fun getFollowers(userId: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        val mockFollowers = listOf(
            User(userId = "user_2", username = "rahat_fan", displayName = "Zoya Akhtar", bio = "Urdu poetry lover"),
            User(userId = "user_3", username = "kabir_thoughts", displayName = "Kabir Verma", bio = "Philosophical mind")
        )
        emit(Resource.Success(mockFollowers))
    }

    override fun getFollowing(userId: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        val mockFollowing = listOf(
            User(userId = "user_4", username = "ishqbaaz", displayName = "Meera Sen", bio = "Romantic poetry"),
            User(userId = "user_5", username = "sher_e_hind", displayName = "Rohan Malhotra", bio = "Motivation & Grit")
        )
        emit(Resource.Success(mockFollowing))
    }

    override fun searchUsers(query: String): Flow<Resource<List<User>>> = flow {
        delay(200)
        val sampleUsers = listOf(
            User(userId = "user_2", username = "rahat_fan", displayName = "Zoya Akhtar", bio = "Urdu poetry lover"),
            User(userId = "user_3", username = "kabir_thoughts", displayName = "Kabir Verma", bio = "Philosophical mind"),
            User(userId = "user_4", username = "ishqbaaz", displayName = "Meera Sen", bio = "Romantic poetry"),
            User(userId = "user_5", username = "sher_e_hind", displayName = "Rohan Malhotra", bio = "Motivation & Grit")
        )
        val filtered = sampleUsers.filter {
            it.username.contains(query, ignoreCase = true) || it.displayName.contains(query, ignoreCase = true)
        }
        emit(Resource.Success(filtered))
    }

    override fun getPopularCreators(): Flow<Resource<List<User>>> = flow {
        val popular = listOf(
            User(userId = "pop_1", username = "ghalib_admirer", displayName = "Mirza Faizan", bio = "Dedicated to classic Urdu poetry", followersCount = 14200),
            User(userId = "pop_2", username = "hindikavya", displayName = "Kavya Sharma", bio = "Hindi poetry & Geet writer", followersCount = 8900),
            User(userId = "pop_3", username = "jazbaat", displayName = "Aman Jaiswal", bio = "Life quotes & Shayari", followersCount = 6700)
        )
        emit(Resource.Success(popular))
    }

    // --- CategoryRepository implementation ---
    override fun getCategories(): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Success(Category.DEFAULT_CATEGORIES))
    }

    // --- NotificationRepository implementation ---
    override fun getNotifications(userId: String): Flow<Resource<List<NotificationItem>>> = flow {
        emit(Resource.Loading())
        val notifications = listOf(
            NotificationItem(
                id = "notif_1",
                userId = userId,
                actorId = "user_2",
                actorUsername = "rahat_fan",
                actorDisplayName = "Zoya Akhtar",
                type = NotificationType.LIKE,
                postId = "post_3",
                postSnippet = "Your only limit is the courage...",
                isRead = false,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 600, 0)
            ),
            NotificationItem(
                id = "notif_2",
                userId = userId,
                actorId = "user_3",
                actorUsername = "kabir_thoughts",
                actorDisplayName = "Kabir Verma",
                type = NotificationType.COMMENT,
                postId = "post_3",
                postSnippet = "Great thought! Keep inspiring.",
                isRead = true,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0)
            ),
            NotificationItem(
                id = "notif_3",
                userId = userId,
                actorId = "user_4",
                actorUsername = "ishqbaaz",
                actorDisplayName = "Meera Sen",
                type = NotificationType.FOLLOW,
                isRead = true,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 86400, 0)
            )
        )
        emit(Resource.Success(notifications))
    }

    override fun markNotificationAsRead(userId: String, notificationId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Success(Unit))
    }

    override fun createNotification(notification: NotificationItem): Flow<Resource<Unit>> = flow {
        emit(Resource.Success(Unit))
    }

    override fun updateFcmToken(userId: String, token: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Success(Unit))
    }

    // --- ModerationRepository implementation ---
    override fun reportTarget(
        reporterId: String,
        targetType: ReportTargetType,
        targetId: String,
        reason: ReportReason,
        details: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading("Submitting report..."))
        delay(400)
        emit(Resource.Success(Unit))
    }

    override fun blockUser(currentUserId: String, blockedUserId: String): Flow<Resource<Unit>> = flow {
        blockedUsersSet.add(blockedUserId)
        emit(Resource.Success(Unit))
    }

    override fun unblockUser(currentUserId: String, blockedUserId: String): Flow<Resource<Unit>> = flow {
        blockedUsersSet.remove(blockedUserId)
        emit(Resource.Success(Unit))
    }

    override fun getBlockedUserIds(currentUserId: String): Flow<Resource<Set<String>>> = flow {
        emit(Resource.Success(blockedUsersSet.toSet()))
    }

    override fun isUserBlocked(currentUserId: String, targetUserId: String): Flow<Boolean> = flow {
        emit(targetUserId in blockedUsersSet)
    }
}
