package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.result.Resource
import kotlinx.coroutines.flow.Flow
import java.io.File

interface PostRepository {
    fun getLatestPosts(limit: Long = 20, lastPostId: String? = null): Flow<Resource<List<Post>>>
    fun getFollowingFeed(currentUserId: String, limit: Long = 20): Flow<Resource<List<Post>>>
    fun getTrendingPosts(limit: Long = 20): Flow<Resource<List<Post>>>
    fun getPostsByCategory(categoryId: String, limit: Long = 20): Flow<Resource<List<Post>>>
    fun getUserPosts(userId: String): Flow<Resource<List<Post>>>
    fun createPost(post: Post, imageFile: File? = null): Flow<Resource<Post>>
    fun updatePost(postId: String, text: String, caption: String, categoryId: String, categoryName: String): Flow<Resource<Unit>>
    fun deletePost(postId: String): Flow<Resource<Unit>>
    fun searchPosts(query: String): Flow<Resource<List<Post>>>
    fun getPostById(postId: String): Flow<Resource<Post>>
}
