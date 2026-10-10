package ru.botva.app.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

object BotvaColors {
    val Cream          = Color(0xFF0F1211)
    val CreamDeep      = Color(0xFF171B19)

    val Green          = Color(0xFF6D9F72)
    val GreenDeep      = Color(0xFF9FCC9F)
    val GreenLight     = Color(0xFF26342A)

    val Terracotta     = Color(0xFFC98A64)
    val TerracottaDeep = Color(0xFFE6A179)

    val TextDark       = Color(0xFFF2F4F2)
    val TextMuted      = Color(0xFF9AA39C)
    val TextOnGreen    = Color(0xFFF7FBF7)
    val TextOnAccent   = Color(0xFFFFF7F2)

    val Error          = Color(0xFFE06A5F)
    val Locked         = Color(0xFF343A36)

    val Border         = Color(0x26FFFFFF)
    val BorderAccent   = Color(0x667FC35D)
    val BackgroundTop    = Color(0xFF0A0E0C)
    val BackgroundBottom = Color(0xFF101713)
    val GlassStrong      = Color(0xF018201C)
    val GlassSoft        = Color(0xA8121715)
    val GlassHighlight   = Color(0x14FFFFFF)
    val AccentStrong     = Color(0x554A8C55)

    val NavyMuted      = TextMuted
    val LeafLight      = Green
    val LeafBright     = Green
    val LeafDeep       = GreenDeep
    val SandWarm       = Terracotta
    val SandDeep       = TerracottaDeep
    val SandLight      = Cream
    val Coral          = Error
    val Violet         = Terracotta
    val NavyMid        = CreamDeep
    val NavyLight      = GreenLight
    val NavyBorder     = TextDark
    val TextLight      = TextDark
}

fun accent(text: String, accentColor: Color, baseBold: Boolean = false): AnnotatedString =
    buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            val start = text.indexOf("**", i)
            if (start == -1) {
                append(text.substring(i))
                break
            }
            append(text.substring(i, start))
            val end = text.indexOf("**", start + 2)
            if (end == -1) {
                append(text.substring(start))
                break
            }
            withStyle(
                SpanStyle(
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(text.substring(start + 2, end))
            }
            i = end + 2
        }
    }

@Composable
fun AccentText(
    text: String,
    accentColor: Color = BotvaColors.SandWarm,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    bold: Boolean = false
) {
    Text(
        text = accent(text, accentColor),
        modifier = modifier,
        style = if (bold) style.copy(fontWeight = FontWeight.SemiBold) else style,
        color = color
    )
}
