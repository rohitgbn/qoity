package com.example.quoteverse

import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.MockDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SocialInteractionTest {

    private lateinit var repository: MockDataRepository

    @Before
    fun setup() {
        repository = MockDataRepository()
    }

    @Test
    fun likeAndUnlike_maintainsConsistentCounts() = runTest {
        val initialPost = (repository.getPostById("post_2").last() as Resource.Success).data
        val initialLikes = initialPost.likeCount
        val uid = repository.currentUserId!!

        // Like post_2
        val likeResult = repository.likePost("post_2", uid, initialPost.authorId).last()
        assertTrue(likeResult is Resource.Success)

        val likedPost = (repository.getPostById("post_2").last() as Resource.Success).data
        assertEquals(initialLikes + 1, likedPost.likeCount)
        assertTrue(likedPost.isLikedByMe)

        // Unlike post_2
        val unlikeResult = repository.unlikePost("post_2", uid).last()
        assertTrue(unlikeResult is Resource.Success)

        val unlikedPost = (repository.getPostById("post_2").last() as Resource.Success).data
        assertEquals(initialLikes, unlikedPost.likeCount)
        assertFalse(unlikedPost.isLikedByMe)
    }

    @Test
    fun addAndCommentDeletion_updatesCommentCount() = runTest {
        val initialPost = (repository.getPostById("post_1").last() as Resource.Success).data
        val initialCommentCount = initialPost.commentCount
        val currentUser = repository.getCurrentUser()!!

        // Add Comment
        val addResult = repository.addComment(
            postId = "post_1",
            text = "Splendid thoughts! Very inspiring.",
            author = currentUser,
            postAuthorId = initialPost.authorId
        ).last()

        assertTrue(addResult is Resource.Success)
        val createdComment = (addResult as Resource.Success).data
        assertEquals("post_1", createdComment.postId)
        assertEquals(currentUser.userId, createdComment.authorId)

        val updatedPost = (repository.getPostById("post_1").last() as Resource.Success).data
        assertEquals(initialCommentCount + 1, updatedPost.commentCount)

        // Delete Comment
        val deleteResult = repository.deleteComment("post_1", createdComment.commentId).last()
        assertTrue(deleteResult is Resource.Success)

        val finalPost = (repository.getPostById("post_1").last() as Resource.Success).data
        assertEquals(initialCommentCount, finalPost.commentCount)
    }

    @Test
    fun bookmarkPost_savesToPrivateBookmarks() = runTest {
        val uid = repository.currentUserId!!
        val postId = "post_4"

        // Bookmark
        val bookmarkRes = repository.bookmarkPost(postId, uid).last()
        assertTrue(bookmarkRes is Resource.Success)

        // Check in saved posts
        val savedPosts = (repository.getSavedPosts(uid).last() as Resource.Success).data
        assertTrue(savedPosts.any { it.postId == postId })

        // Unbookmark
        val unbookmarkRes = repository.unbookmarkPost(postId, uid).last()
        assertTrue(unbookmarkRes is Resource.Success)

        val afterSaved = (repository.getSavedPosts(uid).last() as Resource.Success).data
        assertFalse(afterSaved.any { it.postId == postId })
    }
}
