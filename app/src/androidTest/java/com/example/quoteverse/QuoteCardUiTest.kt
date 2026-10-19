package com.example.quoteverse

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.quoteverse.core.design.components.QuoteCard
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.theme.QuoteVerseTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuoteCardUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun quoteCard_displaysAuthorAndQuoteText() {
        var clickedLike = false

        val samplePost = Post(
            postId = "test_post_1",
            authorId = "author_1",
            authorUsername = "mirza_poet",
            authorDisplayName = "Mirza Ghalib",
            text = "हज़ारों ख्वाहिशें ऐसी कि हर ख्वाहिश पे दम निकले",
            caption = "Timeless philosophy",
            categoryName = "Classic Shayari",
            likeCount = 42,
            commentCount = 10
        )

        composeTestRule.setContent {
            QuoteVerseTheme {
                QuoteCard(
                    post = samplePost,
                    currentUserId = "user_99",
                    onLikeClick = { clickedLike = true },
                    onCommentClick = {},
                    onBookmarkClick = {},
                    onUserClick = {}
                )
            }
        }

        // Verify quote text is displayed
        composeTestRule.onNodeWithText("हज़ारों ख्वाहिशें ऐसी कि हर ख्वाहिश पे दम निकले").assertIsDisplayed()
        // Verify author is displayed
        composeTestRule.onNodeWithText("Mirza Ghalib").assertIsDisplayed()
        // Verify category is displayed
        composeTestRule.onNodeWithText("Classic Shayari").assertIsDisplayed()
        // Verify like count
        composeTestRule.onNodeWithText("42").assertIsDisplayed()
    }
}
