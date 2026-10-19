package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.Comment
import com.example.quoteverse.core.model.NotificationItem
import com.example.quoteverse.core.model.NotificationType
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.model.User
import com.example.quoteverse.core.result.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteractionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : InteractionRepository {

    override fun likePost(postId: String, userId: String, authorId: String): Flow<Resource<Unit>> = flow {
        try {
            val likeDocId = "${postId}_$userId"
            val likeRef = firestore.collection("likes").document(likeDocId)
            val postRef = firestore.collection("posts").document(postId)

            firestore.runTransaction { transaction ->
                val likeSnap = transaction.get(likeRef)
                if (!likeSnap.exists()) {
                    val now = Timestamp.now()
                    transaction.set(likeRef, mapOf(
                        "postId" to postId,
                        "userId" to userId,
                        "createdAt" to now
                    ))
                    transaction.update(postRef, "likeCount", FieldValue.increment(1))

                    // Create in-app notification if not liking own post
                    if (authorId != userId) {
                        val notifId = UUID.randomUUID().toString()
                        val notifRef = firestore.collection("users").document(authorId)
                            .collection("notifications").document(notifId)
                        val notification = NotificationItem(
                            id = notifId,
                            userId = authorId,
                            actorId = userId,
                            type = NotificationType.LIKE,
                            postId = postId,
                            createdAt = now,
                            isRead = false
                        )
                        transaction.set(notifRef, notification)
                    }
                }
            }.await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to like post", e))
        }
    }

    override fun unlikePost(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        try {
            val likeDocId = "${postId}_$userId"
            val likeRef = firestore.collection("likes").document(likeDocId)
            val postRef = firestore.collection("posts").document(postId)

            firestore.runTransaction { transaction ->
                val likeSnap = transaction.get(likeRef)
                if (likeSnap.exists()) {
                    transaction.delete(likeRef)
                    transaction.update(postRef, "likeCount", FieldValue.increment(-1))
                }
            }.await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to unlike post", e))
        }
    }

    override fun isPostLiked(postId: String, userId: String): Flow<Boolean> = flow {
        val likeDocId = "${postId}_$userId"
        val snap = firestore.collection("likes").document(likeDocId).get().await()
        emit(snap.exists())
    }

    override fun getComments(postId: String): Flow<Resource<List<Comment>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()

            val comments = snapshot.toObjects(Comment::class.java)
            emit(Resource.Success(comments))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load comments", e))
        }
    }

    override fun addComment(
        postId: String,
        text: String,
        author: User,
        postAuthorId: String
    ): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading("Posting comment..."))
        try {
            val commentId = UUID.randomUUID().toString()
            val now = Timestamp.now()
            val comment = Comment(
                commentId = commentId,
                postId = postId,
                authorId = author.userId,
                authorUsername = author.username,
                authorDisplayName = author.displayName,
                authorPhotoUrl = author.profileImageUrl,
                text = text.trim(),
                createdAt = now,
                isMyComment = true
            )

            val commentRef = firestore.collection("comments").document(commentId)
            val postRef = firestore.collection("posts").document(postId)

            val batch = firestore.batch()
            batch.set(commentRef, comment)
            batch.update(postRef, "commentCount", FieldValue.increment(1))

            // Notification for post author
            if (postAuthorId != author.userId) {
                val notifId = UUID.randomUUID().toString()
                val notifRef = firestore.collection("users").document(postAuthorId)
                    .collection("notifications").document(notifId)
                val notification = NotificationItem(
                    id = notifId,
                    userId = postAuthorId,
                    actorId = author.userId,
                    actorUsername = author.username,
                    actorDisplayName = author.displayName,
                    actorPhotoUrl = author.profileImageUrl,
                    type = NotificationType.COMMENT,
                    postId = postId,
                    postSnippet = text.take(60),
                    createdAt = now,
                    isRead = false
                )
                batch.set(notifRef, notification)
            }

            batch.commit().await()
            emit(Resource.Success(comment))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to post comment", e))
        }
    }

    override fun deleteComment(postId: String, commentId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val batch = firestore.batch()
            batch.delete(firestore.collection("comments").document(commentId))
            batch.update(firestore.collection("posts").document(postId), "commentCount", FieldValue.increment(-1))
            batch.commit().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to delete comment", e))
        }
    }

    override fun bookmarkPost(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        try {
            val bookmarkRef = firestore.collection("users").document(userId)
                .collection("bookmarks").document(postId)
            val postRef = firestore.collection("posts").document(postId)

            val batch = firestore.batch()
            batch.set(bookmarkRef, mapOf(
                "postId" to postId,
                "createdAt" to Timestamp.now()
            ))
            batch.update(postRef, "bookmarkCount", FieldValue.increment(1))
            batch.commit().await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to save post", e))
        }
    }

    override fun unbookmarkPost(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        try {
            val bookmarkRef = firestore.collection("users").document(userId)
                .collection("bookmarks").document(postId)
            val postRef = firestore.collection("posts").document(postId)

            val batch = firestore.batch()
            batch.delete(bookmarkRef)
            batch.update(postRef, "bookmarkCount", FieldValue.increment(-1))
            batch.commit().await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to remove saved post", e))
        }
    }

    override fun isPostBookmarked(postId: String, userId: String): Flow<Boolean> = flow {
        val snap = firestore.collection("users").document(userId)
            .collection("bookmarks").document(postId).get().await()
        emit(snap.exists())
    }

    override fun getSavedPosts(userId: String): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val bookmarksSnap = firestore.collection("users").document(userId)
                .collection("bookmarks")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val postIds = bookmarksSnap.documents.map { it.id }
            if (postIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Fetch each bookmarked post
            val posts = postIds.mapNotNull { pid ->
                val doc = firestore.collection("posts").document(pid).get().await()
                doc.toObject(Post::class.java)?.copy(isSavedByMe = true)
            }
            emit(Resource.Success(posts))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load saved posts", e))
        }
    }

    override fun getLikedPosts(userId: String): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val likesSnap = firestore.collection("likes")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val postIds = likesSnap.documents.mapNotNull { it.getString("postId") }
            if (postIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            val posts = postIds.mapNotNull { pid ->
                val doc = firestore.collection("posts").document(pid).get().await()
                doc.toObject(Post::class.java)?.copy(isLikedByMe = true)
            }
            emit(Resource.Success(posts))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load liked posts", e))
        }
    }
}
