package ru.botva.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.botva.app.audio.SoundManager
import ru.botva.app.ui.theme.BotvaColors
import ru.botva.app.ui.theme.Caveat
import ru.botva.app.ui.theme.glassPanel
import ru.botva.app.vm.GameViewModel

@Composable
fun MapScreen(vm: GameViewModel, onBack: () -> Unit, onLessonClick: (Int) -> Unit) {
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassPanel(elevation = 0)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                SoundManager.tap()
                                onBack()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад в меню",
                            tint = BotvaColors.TextDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Прогресс",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Caveat,
                        color = BotvaColors.Terracotta
                    )
                }
                Text(
                    "🪙 ${vm.coins}   ⭐ ${vm.totalStars}/${vm.maxStars}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Caveat,
                    color = BotvaColors.GreenDeep
                )
            }

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                vm.lessons.forEachIndexed { index, lesson ->
                    val unlocked = vm.isLessonUnlocked(lesson.id)
                    val stars = vm.stars[lesson.id] ?: 0

                    LessonNode(
                        lessonId = lesson.id,
                        unlocked = unlocked,
                        stars = stars,
                        current = lesson.id == vm.currentLessonId,
                        onClick = { onLessonClick(lesson.id) }
                    )

                    if (index < vm.lessons.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = size.width / 2
                                drawLine(
                                    color = BotvaColors.BorderAccent,
                                    start = Offset(cx, 0f),
                                    end = Offset(cx, size.height),
                                    strokeWidth = 5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonNode(
    lessonId: Int,
    unlocked: Boolean,
    stars: Int,
    current: Boolean,
    onClick: () -> Unit
) {
    val size = if (current) 110.dp else 88.dp
    val textColor = when {
        !unlocked -> BotvaColors.TextMuted
        current -> BotvaColors.GreenDeep
        else -> BotvaColors.TextDark
    }

    Box(
        modifier = Modifier
            .size(size)
            .glassPanel(
                shape = CircleShape,
                accent = current && unlocked,
                elevation = if (current) 2 else 1
            )
            .clip(CircleShape)
            .clickable(enabled = unlocked) {
                SoundManager.tap()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!unlocked) {
                Text("🔒", fontSize = 28.sp)
            } else {
                Text(
                    lessonId.toString(),
                    fontSize = if (current) 48.sp else 40.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Caveat,
                    color = textColor
                )
                if (stars > 0) {
                    Text("⭐".repeat(stars), fontSize = 12.sp)
                }
            }
        }
    }
}
