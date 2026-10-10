package ru.botva.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.botva.app.R
import ru.botva.app.audio.SoundManager
import ru.botva.app.ui.theme.BotvaColors
import ru.botva.app.ui.theme.Caveat

@Composable
fun StartScreen(onStart: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        BotvaColors.BackgroundTop,
                        BotvaColors.BackgroundBottom
                    )
                )
            )
            .padding(22.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(BotvaColors.GlassSoft)
                    .padding(
                        horizontal = 18.dp,
                        vertical = 8.dp
                    )
            ) {
                Text(
                    text = "ОБУЧАЮЩАЯ ИГРА",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = BotvaColors.GreenDeep
                )
            }

            Spacer(Modifier.height(102.dp))

            Box(
                modifier = Modifier
                    .size(175.dp)
                    .shadow(
                        elevation = 17.dp,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                BotvaColors.AccentStrong,
                                BotvaColors.GlassSoft
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(R.drawable.robot_idle),
                    contentDescription = "Робот-помощник",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(140.dp)
                        .rotate(90f)
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Ботва",
                fontSize = 64.sp,
                lineHeight = 64.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Caveat,
                color = BotvaColors.GreenDeep
            )

            Text(
                text = "Учись программировать играя",
                fontSize = 12.sp,
                color = BotvaColors.GreenDeep,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                FeatureCard(
                    number = "1",
                    title = "Изучай",
                    modifier = Modifier.weight(1f)
                )

                FeatureCard(
                    number = "2",
                    title = "Решай",
                    modifier = Modifier.weight(1f)
                )

                FeatureCard(
                    number = "3",
                    title = "Играй",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(BotvaColors.Green)
                    .clickable {
                        SoundManager.tap()
                        onStart()
                    },
                contentAlignment = Alignment.Center
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                BotvaColors.TextOnGreen.copy(
                                    alpha = 0.15f
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = BotvaColors.TextOnGreen,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = "НАЧАТЬ ОБУЧЕНИЕ",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        color = BotvaColors.TextOnGreen
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    number: String,
    title: String,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(BotvaColors.GlassSoft)
            .padding(
                vertical = 14.dp,
                horizontal = 6.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(BotvaColors.Green),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = number,
                color = BotvaColors.TextOnGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BotvaColors.GreenDeep,
            textAlign = TextAlign.Center
        )
    }
}