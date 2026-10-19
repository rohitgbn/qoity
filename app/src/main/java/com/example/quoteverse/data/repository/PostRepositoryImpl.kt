package com.example.quoteverse.data.repository

import android.net.Uri
import com.example.quoteverse.core.model.ModerationStatus
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.model.PostType
import com.example.quoteverse.core.result.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : PostRepository {

    private val currentUserId: String? get() = auth.currentUser?.uid

    override fun getLatestPosts(limit: Long, lastPostId: String?): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            var query = firestore.collection("posts")
                .whereEqualTo("moderationStatus", ModerationStatus.APPROVED.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)

            if (lastPostId != null) {
                val lastDoc = firestore.collection("posts").document(lastPostId).get().await()
                if (lastDoc.exists()) {
                    query = query.startAfter(lastDoc)
                }
            }

            val snapshot = query.get().await()
            val posts = snapshot.toObjects(Post::class.java)
            val enrichedPosts = enrichWithInteractionFlags(posts)
            emit(Resource.Success(enrichedPosts))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load posts", e))
        }
    }

    override fun getFollowingFeed(currentUserId: String, limit: Long): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            // Step 1: Get following user IDs
            val followsSnap = firestore.collection("follows")
                .whereEqualTo("followerId", currentUserId)
                .get()
                .await()

            val followingIds = followsSnap.documents.mapNotNull { it.getString("followingId") }
            if (followingIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Step 2: Fetch posts (Firestore limits 'in' queries to 30 items)
            val targetIds = followingIds.take(30)
            val postsSnap = firestore.collection("posts")
                .whereIn("authorId", targetIds)
                .whereEqualTo("moderationStatus", ModerationStatus.APPROVED.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            val posts = postsSnap.toObjects(Post::class.java)
            val enrichedPosts = enrichWithInteractionFlags(posts)
            emit(Resource.Success(enrichedPosts))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load following feed", e))
        }
    }

    override fun getTrendingPosts(limit: Long): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("posts")
                .whereEqualTo("moderationStatus", ModerationStatus.APPROVED.name)
                .orderBy("likeCount", Query.Direction.DESCENDING)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            val posts = snapshot.toObjects(Post::class.java)
            val enrichedPosts = enrichWithInteractionFlags(posts)
            emit(Resource.Success(enrichedPosts))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load trending posts", e))
        }
    }

    override fun getPostsByCategory(categoryId: String, limit: Long): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("posts")
                .whereEqualTo("moderationStatus", ModerationStatus.APPROVED.name)
                .whereEqualTo("categoryId", categoryId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            val posts = snapshot.toObjects(Post::class.java)
            val enrichedPosts = enrichWithInteractionFlags(posts)
            emit(Resource.Success(enrichedPosts))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load category posts", e))
        }
    }

    override fun getUserPosts(userId: String): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("posts")
                .whereEqualTo("authorId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val posts = snapshot.toObjects(Post::class.java)
            val enrichedPosts = enrichWithInteractionFlags(posts)
            emit(Resource.Success(enrichedPosts))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load user posts", e))
        }
    }

    override fun createPost(post: Post, imageFile: File?): Flow<Resource<Post>> = flow {
        emit(Resource.Loading("Uploading and publishing quote..."))
        val uid = currentUserId ?: throw IllegalStateException("Must be logged in to create a post")

        try {
            val postId = UUID.randomUUID().toString()
            var finalImageUrl: String? = post.imageUrl

            // Upload image to Cloud Storage if provided
            if (imageFile != null && imageFile.exists()) {
                val storageRef = storage.reference.child("posts/$uid/${postId}_${System.currentTimeMillis()}.png")
                val uploadTask = storageRef.putFile(Uri.fromFile(imageFile)).await()
                finalImageUrl = uploadTask.storage.downloadUrl.await().toString()
            }

            val now = Timestamp.now()
            val newPost = post.copy(
                postId = postId,
                authorId = uid,
                imageUrl = finalImageUrl,
                createdAt = now,
                updatedAt = now,
                moderationStatus = ModerationStatus.APPROVED,
                likeCount = 0,
                commentCount = 0,
                bookmarkCount = 0
            )

            // Atomic batch: Create post and increment user's postsCount
            val batch = firestore.batch()
            batch.set(firestore.collection("posts").document(postId), newPost)
            batch.update(firestore.collection("users").document(uid), "postsCount", FieldValue.increment(1))
            batch.commit().await()

            emit(Resource.Success(newPost))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Post publication failed", e))
        }
    }

    override fun updatePost(
        postId: String,
        text: String,
        caption: String,
        categoryId: String,
        categoryName: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val uid = currentUserId ?: throw IllegalStateException("Not logged in")

        try {
            val postRef = firestore.collection("posts").document(postId)
            val postSnap = postRef.get().await()
            val authorId = postSnap.getString("authorId")
            if (authorId != uid) {
                emit(Resource.Error("Unauthorized: You can only edit your own posts"))
                return@flow
            }

            postRef.update(
                mapOf(
                    "text" to text,
                    "caption" to caption,
                    "categoryId" to categoryId,
                    "categoryName" to categoryName,
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to update post", e))
        }
    }

    override fun deletePost(postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val uid = currentUserId ?: throw IllegalStateException("Not logged in")

        try {
            val postRef = firestore.collection("posts").document(postId)
            val postSnap = postRef.get().await()
            val authorId = postSnap.getString("authorId")
            if (authorId != uid) {
                emit(Resource.Error("Unauthorized: You can only delete your own posts"))
                return@flow
            }

            val batch = firestore.batch()
            batch.delete(postRef)
            batch.update(firestore.collection("users").document(uid), "postsCount", FieldValue.increment(-1))
            batch.commit().await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to delete post", e))
        }
    }

    override fun searchPosts(query: String): Flow<Resource<List<Post>>> = flow {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            emit(Resource.Success(emptyList()))
            return@flow
        }
        try {
            // Perform text search across posts
            val snapshot = firestore.collection("posts")
                .whereEqualTo("moderationStatus", ModerationStatus.APPROVED.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val posts = snapshot.toObjects(Post::class.java).filter { post ->
                post.text.contains(trimmed, ignoreCase = true) ||
                post.caption.contains(trimmed, ignoreCase = true) ||
                post.categoryName.contains(trimmed, ignoreCase = true)
            }

            emit(Resource.Success(enrichWithInteractionFlags(posts)))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Search failed", e))
        }
    }

    override fun getPostById(postId: String): Flow<Resource<Post>> = flow {
        try {
            val snap = firestore.collection("posts").document(postId).get().await()
            val post = snap.toObject(Post::class.java)
            if (post != null) {
                val enriched = enrichWithInteractionFlags(listOf(post)).first()
                emit(Resource.Success(enriched))
            } else {
                emit(Resource.Error("Post not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load post", e))
        }
    }

    /**
     * Checks if current user has liked or saved any of the returned posts.
     */
    private suspend fun enrichWithInteractionFlags(posts: List<Post>): List<Post> {
        val uid = currentUserId ?: return posts
        if (posts.isEmpty()) return posts

        return try {
            val enriched = posts.map { post ->
                val likeDocId = "${post.postId}_$uid"
                val isLiked = firestore.collection("likes").document(likeDocId).get().await().exists()
                val isSaved = firestore.collection("users").document(uid).collection("bookmarks").document(post.postId).get().await().exists()
                post.copy(isLikedByMe = isLiked, isSavedByMe = isSaved)
            }
            enriched
        } catch (_: Exception) {
            posts
        }
    }
}
