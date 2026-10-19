package com.example.quoteverse

import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.MockDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthValidationTest {

    private lateinit var authRepository: MockDataRepository

    @Before
    fun setup() {
        authRepository = MockDataRepository()
    }

    @Test
    fun usernameAvailability_validAndUnique_returnsTrue() = runTest {
        val result = authRepository.checkUsernameAvailability("unique_writer_2026").first()
        assertTrue(result is Resource.Success)
        assertTrue((result as Resource.Success).data)
    }

    @Test
    fun usernameAvailability_reservedOrInvalid_returnsFalse() = runTest {
        val resultAdmin = authRepository.checkUsernameAvailability("admin").first()
        assertTrue(resultAdmin is Resource.Success)
        assertFalse((resultAdmin as Resource.Success).data)

        val resultShort = authRepository.checkUsernameAvailability("ab").first()
        assertTrue(resultShort is Resource.Success)
        assertFalse((resultShort as Resource.Success).data)
    }

    @Test
    fun signUpWithEmail_createsUserWithZeroCounters() = runTest {
        val result = authRepository.signUpWithEmail(
            email = "poet@example.com",
            password = "securePassword123",
            username = "shayari_lover",
            displayName = "Poetry Heart"
        ).last()

        assertTrue(result is Resource.Success)
        val user = (result as Resource.Success).data
        assertEquals("shayari_lover", user.username)
        assertEquals("Poetry Heart", user.displayName)
        assertEquals(0L, user.followersCount)
        assertEquals(0L, user.followingCount)
        assertEquals(0L, user.postsCount)
    }

    @Test
    fun passwordReset_sendsEmailSuccessfully() = runTest {
        val result = authRepository.sendPasswordResetEmail("poet@example.com").last()
        assertTrue(result is Resource.Success)
    }

    @Test
    fun signOut_clearsCurrentUserSession() = runTest {
        assertNotNull(authRepository.currentUserId)
        assertTrue(authRepository.isUserLoggedIn)

        authRepository.signOut()

        assertFalse(authRepository.isUserLoggedIn)
    }
}
