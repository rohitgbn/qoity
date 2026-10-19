package com.example.quoteverse.core.design.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.quoteverse.core.model.QuoteDesignConfig
import java.io.File
import java.io.FileOutputStream

object QuoteCanvasRenderer {

    /**
     * Renders a [QuoteDesignConfig] to a high-resolution Bitmap.
     * Default size: 1080 x 1080 pixels (Instagram-standard square post).
     */
    fun renderQuoteToBitmap(
        config: QuoteDesignConfig,
        width: Int = 1080,
        height: Int = 1080
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val preset = QuoteDesignConfig.BACKGROUND_PRESETS.getOrElse(config.backgroundIndex) {
            QuoteDesignConfig.BACKGROUND_PRESETS.first()
        }

        // Draw Background
        val bgPaint = Paint().apply { isAntiAlias = true }
        if (preset.isGradient) {
            val gradient = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                preset.primaryColor.toInt(),
                preset.secondaryColor.toInt(),
                Shader.TileMode.CLAMP
            )
            bgPaint.shader = gradient
        } else {
            bgPaint.color = preset.primaryColor.toInt()
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw Decorative Quote Mark "
        val quoteMarkPaint = Paint().apply {
            isAntiAlias = true
            color = preset.textColor.toInt()
            alpha = 50 // subtle watermark
            textSize = 200f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText("“", 90f, 210f, quoteMarkPaint)

        // Text typography setup
        val fontPreset = QuoteDesignConfig.FONT_PRESETS.getOrElse(config.fontStyleIndex) {
            QuoteDesignConfig.FONT_PRESETS.first()
        }
        val typefaceStyle = when {
            fontPreset.isBold && fontPreset.isItalic -> Typeface.BOLD_ITALIC
            fontPreset.isBold -> Typeface.BOLD
            fontPreset.isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        val baseTypeface = when (fontPreset.fontFamilyKey) {
            "serif" -> Typeface.SERIF
            "monospace" -> Typeface.MONOSPACE
            else -> Typeface.SANS_SERIF
        }
        val quoteTypeface = Typeface.create(baseTypeface, typefaceStyle)

        // Text Paint
        val padding = (config.contentPaddingDp * (width / 360f)).toInt().coerceIn(60, 200)
        val textWidth = width - (padding * 2)

        val scaledTextSize = (config.fontSizeSp * (width / 360f)).coerceIn(40f, 100f)
        val textPaint = TextPaint().apply {
            isAntiAlias = true
            color = config.textColorLong.toInt()
            textSize = scaledTextSize
            typeface = quoteTypeface
        }

        val alignment = when (config.textAlignment) {
            "start" -> Layout.Alignment.ALIGN_NORMAL
            "end" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }

        // StaticLayout for robust multiline and Hindi/English text layout
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(config.quoteText, 0, config.quoteText.length, textPaint, textWidth)
                .setAlignment(alignment)
                .setLineSpacing(16f, 1.25f)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                config.quoteText,
                textPaint,
                textWidth,
                alignment,
                1.25f,
                16f,
                true
            )
        }

        // Calculate vertical centering
        val totalTextHeight = staticLayout.height
        val startY = ((height - totalTextHeight) / 2f).coerceAtLeast(180f)

        canvas.save()
        canvas.translate(padding.toFloat(), startY)
        staticLayout.draw(canvas)
        canvas.restore()

        // Draw Author Attribution Signature
        if (config.showSignature && config.authorSignature.isNotBlank()) {
            val signatureText = "— ${config.authorSignature}"
            val signaturePaint = TextPaint().apply {
                isAntiAlias = true
                color = config.textColorLong.toInt()
                alpha = 200
                textSize = scaledTextSize * 0.48f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            }
            val signatureBounds = Rect()
            signaturePaint.getTextBounds(signatureText, 0, signatureText.length, signatureBounds)
            val sigX = (width - padding - signatureBounds.width()).toFloat()
            val sigY = (startY + totalTextHeight + 70f).coerceAtMost(height - 80f)
            canvas.drawText(signatureText, sigX, sigY, signaturePaint)
        }

        // QuoteVerse Branding watermark in subtle footer
        val brandPaint = TextPaint().apply {
            isAntiAlias = true
            color = preset.textColor.toInt()
            alpha = 90
            textSize = 28f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("QuoteVerse", width / 2f, height - 40f, brandPaint)

        return bitmap
    }

    /**
     * Saves the rendered bitmap to a cache file ready for uploading to Firebase Cloud Storage.
     */
    fun saveBitmapToTempFile(context: Context, bitmap: Bitmap): File {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, "quote_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        return file
    }
}
