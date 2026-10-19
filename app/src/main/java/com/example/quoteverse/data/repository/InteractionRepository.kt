package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.Comment
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.model.User
import com.example.quoteverse.core.result.Resource
import kotlinx.coroutines.flow.Flow

interface InteractionRepository {
    fun likePost(postId: String, userId: String, authorId: String): Flow<Resource<Unit>>
    fun unlikePost(postId: String, userId: String): Flow<Resource<Unit>>
    fun isPostLiked(postId: String, userId: String): Flow<Boolean>
    
    fun getComments(postId: String): Flow<Resource<List<Comment>>>
    fun addComment(postId: String, text: String, author: User, postAuthorId: String): Flow<Resource<Comment>>
    fun deleteComment(postId: String, commentId: String): Flow<Resource<Unit>>
    
    fun bookmarkPost(postId: String, userId: String): Flow<Resource<Unit>>
    fun unbookmarkPost(postId: String, userId: String): Flow<Resource<Unit>>
    fun isPostBookmarked(postId: String, userId: String): Flow<Boolean>
    
    fun getSavedPosts(userId: String): Flow<Resource<List<Post>>>
    fun getLikedPosts(userId: String): Flow<Resource<List<Post>>>
}
