package com.example.quoteverse

import com.example.quoteverse.core.model.Category
import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.MockDataRepository
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchAndCategoryTest {

    private lateinit var repository: MockDataRepository

    @Before
    fun setup() {
        repository = MockDataRepository()
    }

    @Test
    fun getCategories_returnsPreconfiguredCategoriesWithHindiNames() = runTest {
        val result = repository.getCategories().last()
        assertTrue(result is Resource.Success)
        val categories = (result as Resource.Success).data

        assertEquals(Category.DEFAULT_CATEGORIES.size, categories.size)
        assertTrue(categories.any { it.name == "Sad Shayari" && it.hindiName == "दर्द भरी शायरी" })
        assertTrue(categories.any { it.name == "Love" && it.hindiName == "प्यार व मोहब्बत" })
        assertTrue(categories.any { it.name == "Motivation" })
    }

    @Test
    fun searchPosts_byHindiKeyword_returnsMatchingPosts() = runTest {
        val res = repository.searchPosts("खामोशी").last()
        assertTrue(res is Resource.Success)
        val matching = (res as Resource.Success).data
        assertFalse(matching.isEmpty())
        assertTrue(matching.any { it.text.contains("खामोशी") })
    }

    @Test
    fun searchPosts_byEnglishKeyword_returnsMatchingPosts() = runTest {
        val res = repository.searchPosts("courage").last()
        assertTrue(res is Resource.Success)
        val matching = (res as Resource.Success).data
        assertFalse(matching.isEmpty())
        assertTrue(matching.any { it.text.contains("courage", ignoreCase = true) })
    }

    @Test
    fun searchUsers_byQuery_returnsMatchingCreators() = runTest {
        val res = repository.searchUsers("kabir").last()
        assertTrue(res is Resource.Success)
        val matching = (res as Resource.Success).data
        assertFalse(matching.isEmpty())
        assertTrue(matching.any { it.username.contains("kabir", ignoreCase = true) })
    }
}
