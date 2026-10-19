package com.example.quoteverse.core.model

data class QuoteDesignConfig(
    val quoteText: String = "",
    val authorSignature: String = "",
    val showSignature: Boolean = true,
    val backgroundIndex: Int = 0,
    val fontStyleIndex: Int = 0,
    val fontSizeSp: Float = 22f,
    val textColorLong: Long = 0xFFFFFFFF,
    val textAlignment: String = "center", // "start", "center", "end"
    val contentPaddingDp: Int = 24
) {
    companion object {
        data class BackgroundPreset(
            val id: String,
            val name: String,
            val isGradient: Boolean,
            val primaryColor: Long,
            val secondaryColor: Long = primaryColor,
            val textColor: Long = 0xFFFFFFFF
        )

        val BACKGROUND_PRESETS = listOf(
            BackgroundPreset("midnight", "Midnight Dream", true, 0xFF0F2027, 0xFF2C5364, 0xFFFFFFFF),
            BackgroundPreset("sunset", "Warm Sunset", true, 0xFFEE0979, 0xFFFF6A00, 0xFFFFFFFF),
            BackgroundPreset("royal_purple", "Royal Mystic", true, 0xFF654EA3, 0xFFEAAFC8, 0xFFFFFFFF),
            BackgroundPreset("emerald", "Emerald Forest", true, 0xFF134E5E, 0xFF71B280, 0xFFFFFFFF),
            BackgroundPreset("velvet_noir", "Velvet Noir", false, 0xFF121212, 0xFF121212, 0xFFF5F5F5),
            BackgroundPreset("amber_glow", "Amber Glow", true, 0xFFB24592, 0xFFF15F79, 0xFFFFFFFF),
            BackgroundPreset("ocean_breeze", "Deep Ocean", true, 0xFF2B5876, 0xFF4E4376, 0xFFFFFFFF),
            BackgroundPreset("vintage_paper", "Parchment", false, 0xFFF7F2E7, 0xFFF7F2E7, 0xFF2C2523),
            BackgroundPreset("rose_gold", "Rose Blossom", true, 0xFFF4C4F3, 0xFFFC67FA, 0xFF221122),
            BackgroundPreset("aurora", "Northern Lights", true, 0xFF00C9FF, 0xFF92FE9D, 0xFF0A192F)
        )

        data class FontPreset(
            val id: String,
            val name: String,
            val fontFamilyKey: String,
            val isItalic: Boolean = false,
            val isBold: Boolean = false
        )

        val FONT_PRESETS = listOf(
            FontPreset("serif_elegant", "Classic Serif", "serif", isItalic = false, isBold = false),
            FontPreset("serif_italic", "Poetic Italic", "serif", isItalic = true, isBold = false),
            FontPreset("sans_clean", "Modern Minimal", "sans-serif", isItalic = false, isBold = false),
            FontPreset("sans_bold", "Bold Statement", "sans-serif", isItalic = false, isBold = true),
            FontPreset("mono_quote", "Vintage Typewriter", "monospace", isItalic = false, isBold = false),
            FontPreset("cursive_soft", "Calligraphy", "cursive", isItalic = false, isBold = false)
        )
    }
}
