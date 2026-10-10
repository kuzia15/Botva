package ru.botva.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun Modifier.glassPanel(
    shape: Shape = RoundedCornerShape(22.dp),
    accent: Boolean = false,
    elevation: Int = 3
): Modifier {
    val colors = if (accent) {
        listOf(BotvaColors.AccentStrong, BotvaColors.GlassStrong)
    } else {
        listOf(BotvaColors.GlassHighlight, BotvaColors.GlassStrong)
    }
    val stroke = if (accent) BotvaColors.BorderAccent else BotvaColors.Border

    return this
        .shadow(elevation.dp, shape = shape, clip = false)
        .background(Brush.linearGradient(colors), shape)
        .border(1.dp, stroke, shape)
}
