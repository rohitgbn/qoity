package com.example.quoteverse.data.repository

import android.net.Uri
import com.example.quoteverse.core.model.NotificationItem
import com.example.quoteverse.core.model.NotificationType
import com.example.quoteverse.core.model.User
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
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : UserRepository {

    private val currentUserId: String? get() = auth.currentUser?.uid

    override fun getUserProfile(userId: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection("users").document(userId).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) {
                val isFollowing = if (currentUserId != null && currentUserId != userId) {
                    val followDocId = "${currentUserId}_$userId"
                    firestore.collection("follows").document(followDocId).get().await().exists()
                } else false

                val isBlocked = if (currentUserId != null && currentUserId != userId) {
                    val blockDocId = "${currentUserId}_$userId"
                    firestore.collection("userBlocks").document(blockDocId).get().await().exists()
                } else false

                emit(Resource.Success(user.copy(isFollowedByMe = isFollowing, isBlockedByMe = isBlocked)))
            } else {
                emit(Resource.Error("User not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load user profile", e))
        }
    }

    override fun updateProfile(
        userId: String,
        displayName: String,
        bio: String,
        avatarFile: File?
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading("Saving profile..."))
        try {
            var avatarUrl: String? = null
            if (avatarFile != null && avatarFile.exists()) {
                val ref = storage.reference.child("avatars/$userId/avatar_${System.currentTimeMillis()}.png")
                val upload = ref.putFile(Uri.fromFile(avatarFile)).await()
                avatarUrl = upload.storage.downloadUrl.await().toString()
            }

            val updates = mutableMapOf<String, Any>(
                "displayName" to displayName.trim(),
                "bio" to bio.trim(),
                "updatedAt" to Timestamp.now()
            )
            if (avatarUrl != null) {
                updates["profileImageUrl"] = avatarUrl
            }

            firestore.collection("users").document(userId).update(updates).await()

            val updatedSnap = firestore.collection("users").document(userId).get().await()
            val updatedUser = updatedSnap.toObject(User::class.java)
                ?: throw IllegalStateException("Failed to retrieve updated profile")

            emit(Resource.Success(updatedUser))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to update profile", e))
        }
    }

    override fun followUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>> = flow {
        if (currentUserId == targetUserId) {
            emit(Resource.Error("You cannot follow yourself"))
            return@flow
        }

        try {
            val followDocId = "${currentUserId}_$targetUserId"
            val followRef = firestore.collection("follows").document(followDocId)
            val currentRef = firestore.collection("users").document(currentUserId)
            val targetRef = firestore.collection("users").document(targetUserId)

            firestore.runTransaction { transaction ->
                val followSnap = transaction.get(followRef)
                if (!followSnap.exists()) {
                    val now = Timestamp.now()
                    transaction.set(followRef, mapOf(
                        "followerId" to currentUserId,
                        "followingId" to targetUserId,
                        "createdAt" to now
                    ))
                    transaction.update(currentRef, "followingCount", FieldValue.increment(1))
                    transaction.update(targetRef, "followersCount", FieldValue.increment(1))

                    // Notification for target user
                    val notifId = UUID.randomUUID().toString()
                    val notifRef = firestore.collection("users").document(targetUserId)
                        .collection("notifications").document(notifId)
                    val notification = NotificationItem(
                        id = notifId,
                        userId = targetUserId,
                        actorId = currentUserId,
                        type = NotificationType.FOLLOW,
                        createdAt = now,
                        isRead = false
                    )
                    transaction.set(notifRef, notification)
                }
            }.await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to follow user", e))
        }
    }

    override fun unfollowUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>> = flow {
        try {
            val followDocId = "${currentUserId}_$targetUserId"
            val followRef = firestore.collection("follows").document(followDocId)
            val currentRef = firestore.collection("users").document(currentUserId)
            val targetRef = firestore.collection("users").document(targetUserId)

            firestore.runTransaction { transaction ->
                val followSnap = transaction.get(followRef)
                if (followSnap.exists()) {
                    transaction.delete(followRef)
                    transaction.update(currentRef, "followingCount", FieldValue.increment(-1))
                    transaction.update(targetRef, "followersCount", FieldValue.increment(-1))
                }
            }.await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to unfollow user", e))
        }
    }

    override fun isFollowing(currentUserId: String, targetUserId: String): Flow<Boolean> = flow {
        val followDocId = "${currentUserId}_$targetUserId"
        val snap = firestore.collection("follows").document(followDocId).get().await()
        emit(snap.exists())
    }

    override fun getFollowers(userId: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        try {
            val followsSnap = firestore.collection("follows")
                .whereEqualTo("followingId", userId)
                .get()
                .await()

            val followerIds = followsSnap.documents.mapNotNull { it.getString("followerId") }
            val users = followerIds.mapNotNull { uid ->
                firestore.collection("users").document(uid).get().await().toObject(User::class.java)
            }
            emit(Resource.Success(users))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load followers", e))
        }
    }

    override fun getFollowing(userId: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        try {
            val followsSnap = firestore.collection("follows")
                .whereEqualTo("followerId", userId)
                .get()
                .await()

            val followingIds = followsSnap.documents.mapNotNull { it.getString("followingId") }
            val users = followingIds.mapNotNull { uid ->
                firestore.collection("users").document(uid).get().await().toObject(User::class.java)
            }
            emit(Resource.Success(users))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load following", e))
        }
    }

    override fun searchUsers(query: String): Flow<Resource<List<User>>> = flow {
        val clean = query.trim().lowercase()
        if (clean.isBlank()) {
            emit(Resource.Success(emptyList()))
            return@flow
        }
        try {
            val snapshot = firestore.collection("users")
                .whereGreaterThanOrEqualTo("usernameLower", clean)
                .whereLessThanOrEqualTo("usernameLower", clean + "\uf8ff")
                .limit(25)
                .get()
                .await()

            val users = snapshot.toObjects(User::class.java)
            emit(Resource.Success(users))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "User search failed", e))
        }
    }

    override fun getPopularCreators(): Flow<Resource<List<User>>> = flow {
        try {
            val snapshot = firestore.collection("users")
                .orderBy("followersCount", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            val users = snapshot.toObjects(User::class.java)
            emit(Resource.Success(users))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load creators", e))
        }
    }
}
