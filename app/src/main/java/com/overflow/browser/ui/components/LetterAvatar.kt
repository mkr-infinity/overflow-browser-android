package com.overflow.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AvatarPalette = listOf(
    Color(0xFF006A6A),
    Color(0xFF9C4236),
    Color(0xFF3F5F90),
    Color(0xFF6A5D00),
    Color(0xFF5B3F7A),
    Color(0xFF2E6B4F)
)

@Composable
fun LetterAvatar(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    background: Color? = null
) {
    val color = background
        ?: AvatarPalette[(text.hashCode() and Int.MAX_VALUE) % AvatarPalette.size]
    Box(
        modifier = modifier
            .size(size)
            .background(color.copy(alpha = 0.16f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "O",
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.4f).sp
        )
    }
}
