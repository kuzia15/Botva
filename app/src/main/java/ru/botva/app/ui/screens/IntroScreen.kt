package ru.botva.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.botva.app.R
import ru.botva.app.audio.SoundManager
import ru.botva.app.ui.theme.BotvaColors
import ru.botva.app.ui.theme.Caveat
import ru.botva.app.ui.theme.glassPanel

@Composable
fun IntroScreen(onContinue: () -> Unit, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BotvaColors.BackgroundTop, BotvaColors.BackgroundBottom)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Цель игры",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Caveat,
                color = BotvaColors.Terracotta
            )

            IntroBlock(
                imageRes = R.drawable.intro_book,
                text = "Помогите роботу собрать все морковки! Составляйте для него программу из команд, чтобы он добрался до урожая.",
                variant = BlockVariant.Light
            )
            IntroBlock(
                imageRes = R.drawable.intro_blocks,
                text = "Задания это сборка программы из мелких команд.",
                variant = BlockVariant.Green
            )
            IntroBlock(
                imageRes = R.drawable.intro_coin,
                text = "Монетки тратятся на подсказки. Застрял, робот поможет, но не бесплатно.",
                variant = BlockVariant.Light
            )

            Spacer(Modifier.height(100.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
                .size(64.dp)
                .clip(CircleShape)
                .glassPanel(shape = CircleShape, elevation = 0)
                .clickable {
                    SoundManager.tap()
                    onBack()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = BotvaColors.TextDark,
                modifier = Modifier.size(34.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(BotvaColors.Green)
                .clickable {
                    SoundManager.tap()
                    onContinue()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = BotvaColors.TextOnGreen,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

private enum class BlockVariant { Light, Green }

@Composable
private fun IntroBlock(imageRes: Int, text: String, variant: BlockVariant) {
    val textColor = when (variant) {
        BlockVariant.Light -> BotvaColors.TextDark
        BlockVariant.Green -> BotvaColors.TextOnGreen
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(accent = variant == BlockVariant.Green, elevation = 1)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(140.dp)
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text,
            fontSize = 17.sp,
            fontFamily = Caveat,
            color = textColor,
            lineHeight = 24.sp
        )
    }
}
