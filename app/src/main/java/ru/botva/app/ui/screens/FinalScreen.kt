package ru.botva.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.botva.app.audio.SoundManager
import ru.botva.app.ui.theme.BotvaColors
import ru.botva.app.ui.theme.Caveat
import ru.botva.app.ui.theme.glassPanel
import ru.botva.app.vm.GameViewModel

@Composable
fun FinalScreen(vm: GameViewModel, onRestart: () -> Unit) {
    LaunchedEffect(Unit) {
        SoundManager.win()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BotvaColors.BackgroundTop, BotvaColors.BackgroundBottom)
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏆", fontSize = 96.sp)

        Spacer(Modifier.height(16.dp))

        Text(
            "Модуль пройден",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Caveat,
            color = BotvaColors.Terracotta
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Алгоритмы, циклы и условия теперь твои.",
            fontSize = 20.sp,
            fontFamily = Caveat,
            textAlign = TextAlign.Center,
            color = BotvaColors.TextDark
        )

        Spacer(Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatRow("Уроков", "${vm.completedCount} / ${vm.totalLessons}")
            StatRow("Звёзд", "${vm.totalStars} / ${vm.maxStars}")
            StatRow("Монет", "${vm.coins} 🪙")
            StatRow("XP", "${vm.xp}")
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                SoundManager.tap()
                onRestart()
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BotvaColors.Green,
                contentColor = BotvaColors.TextOnGreen
            )
        ) {
            Text(
                "ПРОЙТИ ЗАНОВО",
                fontSize = 20.sp,
                fontFamily = Caveat,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(elevation = 0)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 20.sp,
            fontFamily = Caveat,
            color = BotvaColors.TextMuted
        )
        Text(
            value,
            fontSize = 22.sp,
            fontFamily = Caveat,
            fontWeight = FontWeight.Bold,
            color = BotvaColors.GreenDeep
        )
    }
}
