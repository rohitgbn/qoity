package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.Report
import com.example.quoteverse.core.model.ReportReason
import com.example.quoteverse.core.model.ReportStatus
import com.example.quoteverse.core.model.ReportTargetType
import com.example.quoteverse.core.result.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModerationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ModerationRepository {

    override fun reportTarget(
        reporterId: String,
        targetType: ReportTargetType,
        targetId: String,
        reason: ReportReason,
        details: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading("Submitting report..."))
        try {
            val reportId = UUID.randomUUID().toString()
            val report = Report(
                reportId = reportId,
                reporterId = reporterId,
                targetType = targetType,
                targetId = targetId,
                reason = reason,
                details = details.trim(),
                createdAt = Timestamp.now(),
                status = ReportStatus.PENDING
            )

            firestore.collection("reports").document(reportId).set(report).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to submit report", e))
        }
    }

    override fun blockUser(currentUserId: String, blockedUserId: String): Flow<Resource<Unit>> = flow {
        try {
            val docId = "${currentUserId}_$blockedUserId"
            firestore.collection("userBlocks").document(docId).set(mapOf(
                "blockerId" to currentUserId,
                "blockedUserId" to blockedUserId,
                "createdAt" to Timestamp.now()
            )).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to block user", e))
        }
    }

    override fun unblockUser(currentUserId: String, blockedUserId: String): Flow<Resource<Unit>> = flow {
        try {
            val docId = "${currentUserId}_$blockedUserId"
            firestore.collection("userBlocks").document(docId).delete().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to unblock user", e))
        }
    }

    override fun getBlockedUserIds(currentUserId: String): Flow<Resource<Set<String>>> = flow {
        try {
            val snapshot = firestore.collection("userBlocks")
                .whereEqualTo("blockerId", currentUserId)
                .get()
                .await()

            val ids = snapshot.documents.mapNotNull { it.getString("blockedUserId") }.toSet()
            emit(Resource.Success(ids))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to get blocked users", e))
        }
    }

    override fun isUserBlocked(currentUserId: String, targetUserId: String): Flow<Boolean> = flow {
        val docId = "${currentUserId}_$targetUserId"
        val snap = firestore.collection("userBlocks").document(docId).get().await()
        emit(snap.exists())
    }
}
