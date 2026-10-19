package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.Category
import com.example.quoteverse.core.result.Resource
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<Resource<List<Category>>>
}
