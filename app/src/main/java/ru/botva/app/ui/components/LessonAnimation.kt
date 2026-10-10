package ru.botva.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import ru.botva.app.data.Command
import ru.botva.app.data.FieldSpec
import ru.botva.app.data.Interpreter

@Composable
fun LessonAnimation(
    spec: FieldSpec,
    program: List<Command>,
    modifier: Modifier = Modifier,
    stepDelayMs: Long = 420L,
    loopDelayMs: Long = 900L
) {
    val history = remember(spec, program) {
        Interpreter.run(program, spec.toFieldState())
    }
    var index by remember(history) { mutableIntStateOf(0) }

    LaunchedEffect(history) {
        while (true) {
            index = 0
            while (index < history.lastIndex) {
                delay(stepDelayMs)
                index++
            }
            delay(loopDelayMs)
        }
    }

    val current = history.getOrNull(index) ?: history.first()

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        FieldView(
            state = current,
            isMoving = index < history.lastIndex,
            modifier = Modifier.fillMaxWidth(0.7f)
        )
    }
}
