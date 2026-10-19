package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.User
import com.example.quoteverse.core.result.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    override fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid == null) {
                trySend(null)
            } else {
                firestore.collection("users").document(uid)
                    .addSnapshotListener { snapshot, _ ->
                        val user = snapshot?.toObject(User::class.java)
                        trySend(user)
                    }
            }
        }
        auth.addAuthStateListener(authListener)
        awaitClose { auth.removeAuthStateListener(authListener) }
    }

    override suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (_: Exception) {
            null
        }
    }

    override fun signInWithEmail(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading("Signing in..."))
        try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: throw IllegalStateException("User ID not found")
            val userDoc = firestore.collection("users").document(uid).get().await()
            val user = userDoc.toObject(User::class.java)
                ?: throw IllegalStateException("User profile document not found")
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Sign in failed", e))
        }
    }

    override fun signUpWithEmail(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading("Creating your account..."))
        try {
            val cleanUsername = username.trim().lowercase()
            val usernameRef = firestore.collection("usernames").document(cleanUsername)

            // Step 1: Check username reservation
            val usernameSnap = usernameRef.get().await()
            if (usernameSnap.exists()) {
                emit(Resource.Error("Username '@$cleanUsername' is already taken. Please choose another."))
                return@flow
            }

            // Step 2: Create Firebase Auth User
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: throw IllegalStateException("Failed to create user ID")

            val now = Timestamp.now()
            val user = User(
                userId = uid,
                username = username.trim(),
                usernameLower = cleanUsername,
                displayName = displayName.trim(),
                email = email.trim(),
                bio = "",
                profileImageUrl = null,
                followersCount = 0,
                followingCount = 0,
                postsCount = 0,
                createdAt = now,
                updatedAt = now
            )

            // Step 3: Write user doc and reserve username atomically in a batch
            val batch = firestore.batch()
            batch.set(firestore.collection("users").document(uid), user)
            batch.set(usernameRef, mapOf("uid" to uid, "createdAt" to now))
            batch.commit().await()

            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Registration failed", e))
        }
    }

    override fun signInWithGoogle(idToken: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading("Signing in with Google..."))
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Google authentication failed")
            val uid = firebaseUser.uid

            val userDocRef = firestore.collection("users").document(uid)
            val docSnap = userDocRef.get().await()

            val user = if (docSnap.exists()) {
                docSnap.toObject(User::class.java) ?: throw IllegalStateException("Failed to read user data")
            } else {
                // Initialize profile for first-time Google sign-in
                val defaultUsername = (firebaseUser.displayName?.replace("\\s+".toRegex(), "_")?.lowercase()
                    ?: "user_${uid.take(6)}").take(20)
                val cleanUsername = defaultUsername.filter { it.isLetterOrDigit() || it == '_' }
                val now = Timestamp.now()

                val newUser = User(
                    userId = uid,
                    username = cleanUsername,
                    usernameLower = cleanUsername,
                    displayName = firebaseUser.displayName ?: "Quote Creator",
                    email = firebaseUser.email ?: "",
                    bio = "",
                    profileImageUrl = firebaseUser.photoUrl?.toString(),
                    createdAt = now,
                    updatedAt = now
                )

                val batch = firestore.batch()
                batch.set(userDocRef, newUser)
                batch.set(firestore.collection("usernames").document(cleanUsername), mapOf("uid" to uid, "createdAt" to now))
                batch.commit().await()
                newUser
            }

            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Google sign in failed", e))
        }
    }

    override fun sendPasswordResetEmail(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading("Sending reset link..."))
        try {
            auth.sendPasswordResetEmail(email.trim()).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to send reset email", e))
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun deleteAccount(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading("Deleting your account..."))
        val user = auth.currentUser ?: throw IllegalStateException("Not authenticated")
        val uid = user.uid

        try {
            // Read user doc to get username
            val userDoc = firestore.collection("users").document(uid).get().await()
            val username = userDoc.getString("usernameLower")

            val batch = firestore.batch()
            batch.delete(firestore.collection("users").document(uid))
            if (!username.isNullOrBlank()) {
                batch.delete(firestore.collection("usernames").document(username))
            }
            batch.commit().await()

            user.delete().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Account deletion failed", e))
        }
    }

    override fun checkUsernameAvailability(username: String): Flow<Resource<Boolean>> = flow {
        try {
            val clean = username.trim().lowercase()
            if (clean.length < 3 || clean.length > 30) {
                emit(Resource.Success(false))
                return@flow
            }
            val snap = firestore.collection("usernames").document(clean).get().await()
            emit(Resource.Success(!snap.exists()))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Could not verify username", e))
        }
    }
}
