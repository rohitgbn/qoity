package com.example.quoteverse

import com.example.quoteverse.core.model.ModerationStatus
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.model.PostType
import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.MockDataRepository
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PostCreationAndOwnershipTest {

    private lateinit var repository: MockDataRepository

    @Before
    fun setup() {
        repository = MockDataRepository()
    }

    @Test
    fun createPost_publishesSuccessfullyWithApprovedStatus() = runTest {
        val initialUser = repository.getCurrentUser()!!
        val initialPostsCount = initialUser.postsCount

        val newPost = Post(
            authorId = initialUser.userId,
            authorUsername = initialUser.username,
            authorDisplayName = initialUser.displayName,
            text = "दर्द की दास्तां बहुत लंबी है,\nफिर कभी फुर्सत में सुनाएंगे।",
            caption = "Heartfelt Devanagari 2-liner.",
            categoryId = "sad_shayari",
            categoryName = "Sad Shayari",
            postType = PostType.DESIGNED_IMAGE
        )

        val result = repository.createPost(newPost).last()
        assertTrue(result is Resource.Success)
        val created = (result as Resource.Success).data

        assertNotNull(created.postId)
        assertEquals(initialUser.userId, created.authorId)
        assertEquals(ModerationStatus.APPROVED, created.moderationStatus)
        assertEquals(0L, created.likeCount)
        assertEquals(0L, created.commentCount)

        // Verifies user's postsCount incremented
        val updatedUser = repository.getCurrentUser()!!
        assertEquals(initialPostsCount + 1, updatedUser.postsCount)
    }

    @Test
    fun updatePost_editsTextAndCaption() = runTest {
        val posts = (repository.getLatestPosts().last() as Resource.Success).data
        val myPost = posts.first { it.authorId == repository.currentUserId }

        val newText = "Updated inspirational quote for testing"
        val newCaption = "Updated caption text"

        val updateResult = repository.updatePost(
            postId = myPost.postId,
            text = newText,
            caption = newCaption,
            categoryId = myPost.categoryId,
            categoryName = myPost.categoryName
        ).last()

        assertTrue(updateResult is Resource.Success)

        val fetched = (repository.getPostById(myPost.postId).last() as Resource.Success).data
        assertEquals(newText, fetched.text)
        assertEquals(newCaption, fetched.caption)
    }

    @Test
    fun deletePost_removesPostAndDecrementsCount() = runTest {
        val posts = (repository.getLatestPosts().last() as Resource.Success).data
        val myPost = posts.first { it.authorId == repository.currentUserId }
        val beforeCount = repository.getCurrentUser()!!.postsCount

        val deleteResult = repository.deletePost(myPost.postId).last()
        assertTrue(deleteResult is Resource.Success)

        val afterPosts = (repository.getLatestPosts().last() as Resource.Success).data
        assertTrue(afterPosts.none { it.postId == myPost.postId })

        val afterCount = repository.getCurrentUser()!!.postsCount
        assertEquals(beforeCount - 1, afterCount)
    }
}
