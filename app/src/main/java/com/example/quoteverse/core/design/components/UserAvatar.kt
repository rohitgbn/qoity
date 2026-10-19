package com.example.quoteverse.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.quoteverse.theme.BrandPrimaryDark
import com.example.quoteverse.theme.BrandPrimaryLight

@Composable
fun UserAvatar(
    imageUrl: String?,
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Box(
        modifier = clickableModifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(BrandPrimaryLight, BrandPrimaryDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            val initial = displayName.trim().firstOrNull()?.uppercase() ?: "Q"
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42f).sp
            )
        }
    }
}
