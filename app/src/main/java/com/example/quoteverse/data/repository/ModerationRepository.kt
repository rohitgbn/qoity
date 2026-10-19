package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.ReportReason
import com.example.quoteverse.core.model.ReportTargetType
import com.example.quoteverse.core.result.Resource
import kotlinx.coroutines.flow.Flow

interface ModerationRepository {
    fun reportTarget(
        reporterId: String,
        targetType: ReportTargetType,
        targetId: String,
        reason: ReportReason,
        details: String
    ): Flow<Resource<Unit>>

    fun blockUser(currentUserId: String, blockedUserId: String): Flow<Resource<Unit>>
    fun unblockUser(currentUserId: String, blockedUserId: String): Flow<Resource<Unit>>
    fun getBlockedUserIds(currentUserId: String): Flow<Resource<Set<String>>>
    fun isUserBlocked(currentUserId: String, targetUserId: String): Flow<Boolean>
}
