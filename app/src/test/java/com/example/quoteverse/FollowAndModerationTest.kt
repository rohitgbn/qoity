package com.example.quoteverse

import com.example.quoteverse.core.model.ReportReason
import com.example.quoteverse.core.model.ReportTargetType
import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.MockDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FollowAndModerationTest {

    private lateinit var repository: MockDataRepository

    @Before
    fun setup() {
        repository = MockDataRepository()
    }

    @Test
    fun followAndUnfollow_updatesFollowingCount() = runTest {
        val currentUser = repository.getCurrentUser()!!
        val targetUserId = "user_4"
        val initialFollowing = currentUser.followingCount

        // Follow user_4
        val followRes = repository.followUser(currentUser.userId, targetUserId).last()
        assertTrue(followRes is Resource.Success)

        val updatedUser = repository.getCurrentUser()!!
        assertEquals(initialFollowing + 1, updatedUser.followingCount)
        assertTrue(repository.isFollowing(currentUser.userId, targetUserId).first())

        // Unfollow user_4
        val unfollowRes = repository.unfollowUser(currentUser.userId, targetUserId).last()
        assertTrue(unfollowRes is Resource.Success)

        val finalUser = repository.getCurrentUser()!!
        assertEquals(initialFollowing, finalUser.followingCount)
        assertFalse(repository.isFollowing(currentUser.userId, targetUserId).first())
    }

    @Test
    fun blockUser_hidesBlockedUserPostsFromFeed() = runTest {
        val currentUid = repository.currentUserId!!
        val blockedUid = "user_2" // author of post_1

        val initialFeed = (repository.getLatestPosts().last() as Resource.Success).data
        assertTrue(initialFeed.any { it.authorId == blockedUid })

        // Block user_2
        val blockRes = repository.blockUser(currentUid, blockedUid).last()
        assertTrue(blockRes is Resource.Success)
        assertTrue(repository.isUserBlocked(currentUid, blockedUid).first())

        // Check latest feed now filters out blocked user's posts
        val updatedFeed = (repository.getLatestPosts().last() as Resource.Success).data
        assertFalse(updatedFeed.any { it.authorId == blockedUid })

        // Unblock user_2
        repository.unblockUser(currentUid, blockedUid).last()
        assertFalse(repository.isUserBlocked(currentUid, blockedUid).first())

        val restoredFeed = (repository.getLatestPosts().last() as Resource.Success).data
        assertTrue(restoredFeed.any { it.authorId == blockedUid })
    }

    @Test
    fun reportPost_submitsReportSuccessfully() = runTest {
        val result = repository.reportTarget(
            reporterId = repository.currentUserId!!,
            targetType = ReportTargetType.POST,
            targetId = "post_1",
            reason = ReportReason.SPAM,
            details = "Repetitive promotional content"
        ).last()

        assertTrue(result is Resource.Success)
    }

    @Test
    fun accountDeletion_wipesUserData() = runTest {
        val result = repository.deleteAccount().last()
        assertTrue(result is Resource.Success)

        assertNull(repository.currentUserId)
        assertFalse(repository.isUserLoggedIn)
    }
}
