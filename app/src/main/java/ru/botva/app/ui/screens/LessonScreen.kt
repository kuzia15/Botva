package ru.botva.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.botva.app.audio.SoundManager
import ru.botva.app.data.Command
import ru.botva.app.data.Direction
import ru.botva.app.data.FieldSpec
import ru.botva.app.data.Lesson
import ru.botva.app.data.RobotState
import ru.botva.app.ui.components.LessonAnimation
import ru.botva.app.ui.theme.AccentText
import ru.botva.app.ui.theme.BotvaColors
import ru.botva.app.ui.theme.Caveat
import ru.botva.app.ui.theme.glassPanel

@Composable
fun LessonScreen(
    lesson: Lesson,
    onToTask: () -> Unit,
    onBack: () -> Unit
) {
    val (demoSpec, demoProgram) = demoFor(lesson.id)

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Урок ${lesson.id}",
                fontSize = 16.sp,
                fontFamily = Caveat,
                color = BotvaColors.GreenDeep,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                lesson.title,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Caveat,
                color = BotvaColors.Terracotta
            )

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f)
                    .glassPanel(
                        shape = RoundedCornerShape(28.dp),
                        accent = true,
                        elevation = 2
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                LessonAnimation(
                    spec = demoSpec,
                    program = demoProgram
                )
            }

            Spacer(Modifier.height(20.dp))

            AccentText(
                text = lesson.theory,
                accentColor = BotvaColors.Terracotta,
                modifier = Modifier
                    .fillMaxWidth()
                    .glassPanel(elevation = 1)
                    .padding(16.dp),
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    fontFamily = Caveat,
                    color = BotvaColors.TextDark
                )
            )

            Spacer(Modifier.height(12.dp))

            AccentText(
                text = lesson.example,
                accentColor = BotvaColors.GreenDeep,
                modifier = Modifier
                    .fillMaxWidth()
                    .glassPanel(elevation = 0)
                    .padding(14.dp),
                style = TextStyle(
                    fontSize = 19.sp,
                    fontFamily = Caveat,
                    color = BotvaColors.TextMuted
                )
            )

            Spacer(Modifier.height(120.dp))
        }

        RoundButton(
            onClick = onBack,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            bg = BotvaColors.CreamDeep,
            iconColor = BotvaColors.TextDark,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        )

        RoundButton(
            onClick = onToTask,
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            bg = BotvaColors.Green,
            iconColor = BotvaColors.TextOnGreen,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }
}

private fun demoFor(lessonId: Int): Pair<FieldSpec, List<Command>> = when (lessonId) {

    1 -> FieldSpec(
        3, 3,
        RobotState(0, 0, Direction.RIGHT),
        listOf(2 to 2)
    ) to listOf(
        Command.Forward,
        Command.Forward,
        Command.TurnRight,
        Command.Forward,
        Command.Forward,
        Command.Harvest
    )

    2 -> FieldSpec(
        5, 1,
        RobotState(0, 0, Direction.RIGHT),
        listOf(1 to 0, 4 to 0)
    ) to listOf(
        Command.Forward,
        Command.Harvest,
        Command.Forward,
        Command.Forward,
        Command.Forward,
        Command.Harvest
    )

    3 -> FieldSpec(
        5, 1,
        RobotState(0, 0, Direction.RIGHT),
        listOf(1 to 0, 2 to 0, 3 to 0, 4 to 0)
    ) to listOf(
        Command.Repeat(4, listOf(Command.Forward, Command.Harvest))
    )

    4 -> FieldSpec(
        5, 1,
        RobotState(0, 0, Direction.RIGHT),
        listOf(2 to 0)
    ) to listOf(
        Command.Repeat(
            5,
            listOf(
                Command.IfHarvest(
                    then = listOf(Command.Harvest),
                    elseBranch = listOf(Command.Forward)
                )
            )
        )
    )

    5 -> FieldSpec(
        3, 3,
        RobotState(0, 0, Direction.RIGHT),
        listOf(0 to 1, 1 to 1, 2 to 2)
    ) to listOf(
        Command.Repeat(
            2,
            listOf(
                Command.TurnRight,
                Command.Forward,
                Command.IfHarvest(
                    then = listOf(Command.Harvest),
                    elseBranch = listOf()
                ),
                Command.TurnLeft,
                Command.Forward,
                Command.IfHarvest(
                    then = listOf(Command.Harvest),
                    elseBranch = listOf()
                )
            )
        )
    )

    else -> FieldSpec(3, 1, RobotState(0, 0, Direction.RIGHT), listOf(2 to 0)) to
        listOf(Command.Forward, Command.Forward, Command.Harvest)
}

@Composable
private fun RoundButton(
    onClick: () -> Unit,
    icon: ImageVector,
    bg: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .glassPanel(
                shape = CircleShape,
                accent = bg == BotvaColors.Green,
                elevation = 2
            )
            .clip(CircleShape)
            .clickable {
                SoundManager.tap()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(36.dp)
        )
    }
}
