package com.example.quoteverse.core.model

import com.google.firebase.Timestamp

enum class ReportTargetType {
    POST,
    COMMENT,
    USER
}

enum class ReportReason(val displayName: String) {
    SPAM("Spam or misleading"),
    HATE_SPEECH("Hate speech or symbols"),
    HARASSMENT("Harassment or bullying"),
    INAPPROPRIATE_CONTENT("Inappropriate or sexual content"),
    VIOLENCE("Violence or dangerous organizations"),
    COPYRIGHT("Intellectual property infringement"),
    OTHER("Other violation")
}

enum class ReportStatus {
    PENDING,
    REVIEWED,
    RESOLVED,
    DISMISSED
}

data class Report(
    val reportId: String = "",
    val reporterId: String = "",
    val targetType: ReportTargetType = ReportTargetType.POST,
    val targetId: String = "",
    val reason: ReportReason = ReportReason.SPAM,
    val details: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val status: ReportStatus = ReportStatus.PENDING
)
