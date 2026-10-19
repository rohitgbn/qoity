package com.example.quoteverse.core.model

data class Category(
    val categoryId: String = "",
    val name: String = "",
    val hindiName: String = "",
    val description: String = "",
    val iconName: String = "FormatQuote",
    val sortOrder: Int = 0,
    val isTrending: Boolean = false
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category("love", "Love", "प्यार व मोहब्बत", "Romantic expressions and heartfelt quotes", "Favorite", 1, true),
            Category("sad_shayari", "Sad Shayari", "दर्द भरी शायरी", "Expressing grief, heartbreaks and solace", "HeartBroken", 2, true),
            Category("life_quotes", "Life Quotes", "ज़िन्दगी के फलसफे", "Deep philosophical insights into life and realities", "Spa", 3, true),
            Category("motivation", "Motivation", "प्रेरणा व हौसला", "Daily energy, confidence and perseverance", "Bolt", 4, true),
            Category("attitude", "Attitude", "तेवर और रुतबा", "Bold words, self-respect, and swag", "Whatshot", 5, true),
            Category("friendship", "Friendship", "दोस्ती व याराना", "Celebrating true friends and bonds of lifetime", "People", 6, false),
            Category("family", "Family", "परिवार और अपने", "Gratitude and warmth for parents and loved ones", "Diversity1", 7, false),
            Category("breakup", "Breakup", "अलविदा और जुदाई", "Healing quotes for wounded hearts and moving on", "HeartBroken", 8, false),
            Category("romantic", "Romantic", "इश्क़ और नज़ाकत", "Poetic whispers and tender romance", "FavoriteBorder", 9, true),
            Category("happiness", "Happiness", "मुस्कान और ख़ुशी", "Positivity, contentment, and joy in small things", "SentimentVerySatisfied", 10, false),
            Category("success", "Success", "सफलता व लक्ष्य", "Focus, ambition, career drive, and determination", "EmojiEvents", 11, true),
            Category("hindi_shayari", "Hindi Shayari", "क्लासिक हिंदी शायरी", "Classic Urdu-Hindi nazm, ghazals and two-liners", "AutoStories", 12, true),
            Category("good_morning", "Good Morning", "सुप्रभात संदेश", "Fresh morning wishes, blessings and greetings", "WbSunny", 13, false),
            Category("festival_wishes", "Festival Wishes", "त्यौहार की बधाई", "Wishes for Diwali, Eid, Holi, Christmas, and New Year", "Celebration", 14, false)
        )
    }
}
