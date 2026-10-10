package ru.botva.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.botva.app.R
import ru.botva.app.audio.SoundManager
import ru.botva.app.data.Command
import ru.botva.app.data.FieldState
import ru.botva.app.data.Interpreter
import ru.botva.app.data.Lesson
import ru.botva.app.data.Task
import ru.botva.app.data.TaskType
import ru.botva.app.data.checkTask
import ru.botva.app.ui.components.FieldView
import ru.botva.app.ui.theme.AccentText
import ru.botva.app.ui.theme.BotvaColors
import ru.botva.app.ui.theme.Caveat
import ru.botva.app.ui.theme.glassPanel
import ru.botva.app.vm.GameViewModel

private const val HINT_PRICE = 15
private const val MAX_DEPTH = 3

private fun appendAt(program: List<Command>, path: List<Int>, cmd: Command): List<Command> {
    if (path.isEmpty()) return program + cmd
    val idx = path.first()
    if (idx !in program.indices) return program
    val node = program[idx]
    if (node !is Command.Repeat) return program
    val newBody = appendAt(node.body, path.drop(1), cmd)
    val newList = program.toMutableList()
    newList[idx] = node.copy(body = newBody)
    return newList
}

private fun removeAt(program: List<Command>, path: List<Int>): List<Command> {
    if (path.isEmpty()) return program
    if (path.size == 1) {
        val idx = path[0]
        if (idx !in program.indices) return program
        return program.filterIndexed { i, _ -> i != idx }
    }
    val idx = path.first()
    if (idx !in program.indices) return program
    val node = program[idx]
    if (node !is Command.Repeat) return program
    val newBody = removeAt(node.body, path.drop(1))
    val newList = program.toMutableList()
    newList[idx] = node.copy(body = newBody)
    return newList
}

private fun updateTimes(program: List<Command>, path: List<Int>, times: Int): List<Command> {
    if (path.isEmpty()) return program
    val idx = path.first()
    if (idx !in program.indices) return program
    val node = program[idx]
    if (node !is Command.Repeat) return program
    if (path.size == 1) {
        val newList = program.toMutableList()
        newList[idx] = node.copy(times = times)
        return newList
    }
    val newBody = updateTimes(node.body, path.drop(1), times)
    val newList = program.toMutableList()
    newList[idx] = node.copy(body = newBody)
    return newList
}

private fun getAt(program: List<Command>, path: List<Int>): Command? {
    if (path.isEmpty()) return null
    var current: List<Command> = program
    var node: Command? = null
    for ((depth, idx) in path.withIndex()) {
        if (idx !in current.indices) return null
        node = current[idx]
        if (depth < path.size - 1) {
            if (node !is Command.Repeat) return null
            current = node.body
        }
    }
    return node
}

@Composable
fun TaskScreen(
    lesson: Lesson,
    taskIndex: Int,
    vm: GameViewModel,
    onFinished: () -> Unit,
    onNextTask: () -> Unit,
    onBack: () -> Unit
) {
    val task = lesson.tasks.getOrNull(taskIndex)
    if (task == null) {
        onFinished()
        return
    }

    var attempts by remember(lesson.id, task.id) {
        mutableIntStateOf(vm.attemptsFor(lesson.id, task.id))
    }
    var showHint by remember(lesson.id, task.id) {
        mutableStateOf(vm.isHintRevealed(lesson.id, task.id))
    }
    var result by remember(lesson.id, task.id) { mutableStateOf<Result?>(null) }

    LaunchedEffect(lesson.id, taskIndex) {
        vm.setCurrentTask(lesson.id, taskIndex)
    }

    val isLast = taskIndex == lesson.tasks.lastIndex

    val updateResult: (Result?) -> Unit = { newResult ->
        if (newResult == Result.Wrong && result != Result.Wrong) {
            attempts++
            vm.addAttempt(lesson.id, task.id)
        }
        result = newResult
    }

    val advance: () -> Unit = {
        vm.finishTask(lesson.id, task.id)
        vm.addCoins(20)
        if (isLast) {
            val stars = starsForAttempts(attempts + 1)
            vm.completeLesson(lesson.id, stars)
            onFinished()
        } else {
            onNextTask()
        }
    }

    when (task.type) {
        TaskType.BUILD_PROGRAM -> BuildTaskContent(
            lessonId = lesson.id,
            task = task,
            vm = vm,
            attempts = attempts,
            showHint = showHint,
            result = result,
            onShowHint = { showHint = true },
            onResult = updateResult,
            onAdvance = advance,
            onBack = onBack,
            taskIndex = taskIndex,
            tasksCount = lesson.tasks.size
        )
        TaskType.CHOOSE_OUTPUT -> ChooseTaskContent(
            lessonId = lesson.id,
            task = task,
            vm = vm,
            attempts = attempts,
            showHint = showHint,
            result = result,
            onShowHint = { showHint = true },
            onResult = updateResult,
            onAdvance = advance,
            onBack = onBack,
            taskIndex = taskIndex,
            tasksCount = lesson.tasks.size
        )
    }
}

private enum class Result { Wrong, Right }

private fun starsForAttempts(attempts: Int): Int = when {
    attempts <= 1 -> 3
    attempts <= 2 -> 2
    else -> 1
}

private fun commandRes(cmd: Command): Int? = when (cmd) {
    Command.Forward -> R.drawable.icon_forward
    Command.TurnLeft -> R.drawable.icon_left
    Command.TurnRight -> R.drawable.icon_right
    Command.Harvest -> R.drawable.icon_harvest
    Command.Water -> null
    is Command.Repeat -> R.drawable.icon_repeat
    is Command.IfHarvest -> R.drawable.icon_if
}

private fun commandIcon(cmd: Command): String = when (cmd) {
    Command.Forward -> "▲"
    Command.TurnLeft -> "◀"
    Command.TurnRight -> "▶"
    Command.Harvest -> "🌾"
    Command.Water -> "💧"
    is Command.Repeat -> "↻"
    is Command.IfHarvest -> "?"
}

private fun commandLabel(cmd: Command): String = when (cmd) {
    Command.Forward -> "Вперёд"
    Command.TurnLeft -> "Влево"
    Command.TurnRight -> "Вправо"
    Command.Harvest -> "Собрать"
    Command.Water -> "Полить"
    is Command.Repeat -> "Повтори ${cmd.times}×"
    is Command.IfHarvest -> "Если урожай"
}

private fun paletteLabel(cmd: Command): String = when (cmd) {
    Command.Forward -> "Вперёд"
    Command.TurnLeft -> "Влево"
    Command.TurnRight -> "Вправо"
    Command.Harvest -> "Собрать"
    Command.Water -> "Полить"
    is Command.Repeat -> "Цикл"
    is Command.IfHarvest -> "Условие"
}

@Composable
private fun CommandIconView(cmd: Command, tint: Color, size: Dp) {
    val res = commandRes(cmd)
    if (res != null) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.size(size)
        )
    } else {
        Text(
            commandIcon(cmd),
            fontSize = (size.value * 0.75f).sp,
            color = tint
        )
    }
}

@Composable
private fun HintButton(
    vm: GameViewModel,
    lessonId: Int,
    taskId: Int,
    showHint: Boolean,
    hint: String,
    onShowHint: () -> Unit
) {
    if (!showHint) {
        var notEnough by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassPanel(elevation = 0)
                    .clickable {
                        SoundManager.tap()
                        if (vm.buyHint(lessonId, taskId, HINT_PRICE)) {
                            onShowHint()
                        } else {
                            notEnough = true
                        }
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Подсказка за $HINT_PRICE 🪙",
                    fontFamily = Caveat,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BotvaColors.Terracotta
                )
            }
            if (notEnough) {
                Text(
                    "Не хватает монет",
                    fontFamily = Caveat,
                    fontSize = 15.sp,
                    color = BotvaColors.Error
                )
            }
        }
    } else {
        HintBlock(hint)
    }
}

@Composable
private fun BuildTaskContent(
    lessonId: Int,
    task: Task,
    vm: GameViewModel,
    attempts: Int,
    showHint: Boolean,
    result: Result?,
    onShowHint: () -> Unit,
    onResult: (Result?) -> Unit,
    onAdvance: () -> Unit,
    onBack: () -> Unit,
    taskIndex: Int,
    tasksCount: Int
) {
    var program by remember(lessonId, task.id) {
        mutableStateOf(vm.programFor(lessonId, task.id))
    }
    var activePath by remember(lessonId, task.id) { mutableStateOf<List<Int>>(emptyList()) }

    LaunchedEffect(program) {
        vm.saveProgram(lessonId, task.id, program)
    }

    var history by remember { mutableStateOf(listOf(task.spec.toFieldState())) }
    var index by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }

    var showRepeatDialog by remember { mutableStateOf(false) }
    var editingRepeatPath by remember { mutableStateOf<List<Int>?>(null) }

    val shake = remember { Animatable(0f) }
    LaunchedEffect(result) {
        if (result == Result.Wrong) {
            repeat(3) {
                shake.animateTo(-14f, tween(40))
                shake.animateTo(14f, tween(40))
            }
            shake.animateTo(0f, tween(40))
        }
    }

    LaunchedEffect(history, running) {
        if (!running) return@LaunchedEffect
        index = 0
        while (index < history.lastIndex) {
            delay(300)
            index++
            val prev = history[index - 1]
            val curr = history[index]
            if (curr.robot.x != prev.robot.x || curr.robot.y != prev.robot.y) {
                SoundManager.step()
            }
            if (curr.coins > prev.coins) {
                SoundManager.harvest()
            }
        }
        running = false
        val correct = checkTask(task.check, program, task.spec)
        if (correct) SoundManager.success() else SoundManager.error()
        onResult(if (correct) Result.Right else Result.Wrong)
    }

    val current: FieldState = history.getOrNull(index) ?: task.spec.toFieldState()

    if (showRepeatDialog) {
        RepeatNumberDialog(
            initial = editingRepeatPath?.let { (getAt(program, it) as? Command.Repeat)?.times } ?: 3,
            onPick = { n ->
                SoundManager.tap()
                if (editingRepeatPath != null) {
                    program = updateTimes(program, editingRepeatPath!!, n)
                } else {
                    program = appendAt(program, activePath, Command.Repeat(n, emptyList()))
                    activePath = activePath + (program.lastIndex)
                }
                showRepeatDialog = false
                editingRepeatPath = null
            },
            onDismiss = {
                showRepeatDialog = false
                editingRepeatPath = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BotvaColors.BackgroundTop, BotvaColors.BackgroundBottom)
                )
            )
            .offset(x = shake.value.dp)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassPanel(elevation = 0)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
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
                        modifier = Modifier.size(23.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    "Задание ${taskIndex + 1} из $tasksCount",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    fontFamily = Caveat,
                    color = BotvaColors.GreenDeep
                )
            }
            Text(
                "🪙 ${vm.coins}  ⭐ ${vm.totalStars}",
                fontSize = 15.sp,
                fontFamily = Caveat,
                color = BotvaColors.Terracotta
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FieldView(
                state = current,
                isMoving = running,
                modifier = Modifier.fillMaxWidth(0.65f)
            )
        }

        Breadcrumbs(
            program = program,
            activePath = activePath,
            onNavigate = { activePath = it }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .glassPanel(
                    shape = RoundedCornerShape(24.dp),
                    elevation = 1
                )
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            if (program.isEmpty() && activePath.isEmpty()) {
                Text(
                    "Пусто. Тапни по команде внизу.",
                    fontSize = 15.sp,
                    fontFamily = Caveat,
                    color = BotvaColors.TextMuted
                )
            } else {
                ProgramTree(
                    items = program,
                    basePath = emptyList(),
                    activePath = activePath,
                    enabled = !running && result == null,
                    depth = 0,
                    onRepeatTap = { path ->
                        activePath = if (activePath == path) path.dropLast(1) else path
                    },
                    onRepeatTimesTap = { path ->
                        editingRepeatPath = path
                        showRepeatDialog = true
                    },
                    onRemove = { path ->
                        if (path.size == 1 && path[0] == activePath.firstOrNull()) {
                            activePath = emptyList()
                        }
                        program = removeAt(program, path)
                    }
                )
            }
        }

        Text(
            "Команды",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Caveat,
            color = BotvaColors.TextMuted
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(task.palette) { cmd ->
                PaletteIcon(
                    cmd = cmd,
                    enabled = !running && result == null,
                    onClick = {
                        SoundManager.tap()
                        if (cmd is Command.Repeat) {
                            editingRepeatPath = null
                            showRepeatDialog = true
                        } else {
                            program = appendAt(program, activePath, cmd)
                        }
                    }
                )
            }
        }

        if (result == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (program.isEmpty()) return@Button
                        SoundManager.tap()
                        history = Interpreter.run(program, task.spec.toFieldState())
                        index = 0
                        running = true
                    },
                    enabled = !running && program.isNotEmpty(),
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BotvaColors.Green,
                        contentColor = BotvaColors.TextOnGreen
                    )
                ) {
                    Text("ЗАПУСТИТЬ", fontFamily = Caveat, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        SoundManager.tap()
                        program = emptyList()
                        activePath = emptyList()
                        history = listOf(task.spec.toFieldState())
                        index = 0
                    },
                    enabled = !running,
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("СБРОС", fontFamily = Caveat, fontSize = 16.sp, color = BotvaColors.TextDark)
                }
            }

            HintButton(
                vm = vm,
                lessonId = lessonId,
                taskId = task.id,
                showHint = showHint,
                hint = task.hint,
                onShowHint = onShowHint
            )
        } else {
            ResultBlock(
                result = result!!,
                attempts = attempts,
                onRetry = {
                    SoundManager.tap()
                    program = emptyList()
                    activePath = emptyList()
                    history = listOf(task.spec.toFieldState())
                    index = 0
                    onResult(null)
                },
                onNext = {
                    SoundManager.tap()
                    onAdvance()
                }
            )
        }
    }
}

@Composable
private fun Breadcrumbs(
    program: List<Command>,
    activePath: List<Int>,
    onNavigate: (List<Int>) -> Unit
) {
    val crumbs = remember(program, activePath) {
        val list = mutableListOf<Pair<List<Int>, String>>()
        list.add(emptyList<Int>() to "Программа")
        var p = emptyList<Int>()
        activePath.forEach { i ->
            p = p + i
            val cmd = getAt(program, p) as? Command.Repeat
            if (cmd != null) {
                list.add(p to "Повтори ${cmd.times}×")
            }
        }
        list
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        crumbs.forEachIndexed { i, (path, name) ->
            Text(
                name,
                fontFamily = Caveat,
                fontSize = 16.sp,
                fontWeight = if (path == activePath) FontWeight.Bold else FontWeight.Normal,
                color = if (path == activePath) BotvaColors.Terracotta else BotvaColors.TextMuted,
                modifier = Modifier
                    .clickable {
                        SoundManager.tap()
                        onNavigate(path)
                    }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
            if (i < crumbs.lastIndex) {
                Text(
                    "›",
                    fontFamily = Caveat,
                    fontSize = 16.sp,
                    color = BotvaColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun ProgramTree(
    items: List<Command>,
    basePath: List<Int>,
    activePath: List<Int>,
    enabled: Boolean,
    depth: Int,
    onRepeatTap: (List<Int>) -> Unit,
    onRepeatTimesTap: (List<Int>) -> Unit,
    onRemove: (List<Int>) -> Unit
) {
    val isActiveContainer = basePath == activePath

    Column {
        items.forEachIndexed { i, cmd ->
            val path = basePath + i

            CommandTreeRow(
                cmd = cmd,
                path = path,
                depth = depth,
                isActive = path == activePath,
                enabled = enabled,
                onRepeatTap = onRepeatTap,
                onRepeatTimesTap = onRepeatTimesTap,
                onRemove = onRemove
            )

            if (cmd is Command.Repeat) {
                ProgramTree(
                    items = cmd.body,
                    basePath = path,
                    activePath = activePath,
                    enabled = enabled,
                    depth = depth + 1,
                    onRepeatTap = onRepeatTap,
                    onRepeatTimesTap = onRepeatTimesTap,
                    onRemove = onRemove
                )
            }
        }

        if (isActiveContainer) {
            InsertionMarker(depth = depth)
        }
    }
}

@Composable
private fun CommandTreeRow(
    cmd: Command,
    path: List<Int>,
    depth: Int,
    isActive: Boolean,
    enabled: Boolean,
    onRepeatTap: (List<Int>) -> Unit,
    onRepeatTimesTap: (List<Int>) -> Unit,
    onRemove: (List<Int>) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 20).dp)
            .background(if (isActive) BotvaColors.GreenLight else BotvaColors.Cream)
            .clickable(enabled = enabled) {
                SoundManager.tap()
                if (cmd is Command.Repeat) {
                    onRepeatTap(path)
                }
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommandIconView(cmd = cmd, tint = BotvaColors.GreenDeep, size = 22.dp)
        Spacer(Modifier.size(8.dp))
        Text(
            commandLabel(cmd),
            fontFamily = Caveat,
            fontSize = 16.sp,
            color = BotvaColors.TextDark,
            modifier = Modifier.weight(1f)
        )
        if (cmd is Command.Repeat) {
            Box(
                modifier = Modifier
                    .clickable(enabled = enabled) {
                        SoundManager.tap()
                        onRepeatTimesTap(path)
                    }
                    .padding(4.dp)
            ) {
                Text(
                    "изм.",
                    fontFamily = Caveat,
                    fontSize = 13.sp,
                    color = BotvaColors.Terracotta
                )
            }
        }
        Box(
            modifier = Modifier
                .clickable(enabled = enabled) {
                    SoundManager.tap()
                    onRemove(path)
                }
                .padding(4.dp)
        ) {
            Text("×", fontSize = 18.sp, color = BotvaColors.TextMuted)
        }
    }
}

@Composable
private fun InsertionMarker(depth: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 20).dp + 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("▼", fontSize = 12.sp, color = BotvaColors.Terracotta)
        Spacer(Modifier.size(6.dp))
        Text(
            "сюда падают команды",
            fontFamily = Caveat,
            fontSize = 13.sp,
            color = BotvaColors.Terracotta
        )
    }
}

@Composable
private fun RepeatNumberDialog(
    initial: Int,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var times by remember { mutableIntStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Сколько раз повторить?",
                fontFamily = Caveat,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = BotvaColors.Terracotta
            )
        },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..5).forEach { n ->
                    val active = times == n
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (active) BotvaColors.Terracotta else BotvaColors.GreenLight)
                            .clickable {
                                SoundManager.tap()
                                times = n
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            n.toString(),
                            fontFamily = Caveat,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) BotvaColors.TextOnAccent else BotvaColors.TextDark
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onPick(times) }) {
                Text(
                    "Готово",
                    fontFamily = Caveat,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BotvaColors.GreenDeep
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", fontFamily = Caveat, fontSize = 18.sp, color = BotvaColors.TextMuted)
            }
        },
        containerColor = BotvaColors.Cream
    )
}

@Composable
private fun PaletteIcon(cmd: Command, enabled: Boolean, onClick: () -> Unit) {
    val bg = if (enabled) BotvaColors.Green else BotvaColors.GreenLight
    Column(
        modifier = Modifier
            .width(66.dp)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            CommandIconView(cmd = cmd, tint = BotvaColors.TextOnGreen, size = 32.dp)
        }
        Text(
            text = paletteLabel(cmd),
            fontFamily = Caveat,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) BotvaColors.TextDark else BotvaColors.TextMuted,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun ChooseTaskContent(
    lessonId: Int,
    task: Task,
    vm: GameViewModel,
    attempts: Int,
    showHint: Boolean,
    result: Result?,
    onShowHint: () -> Unit,
    onResult: (Result?) -> Unit,
    onAdvance: () -> Unit,
    onBack: () -> Unit,
    taskIndex: Int,
    tasksCount: Int
) {
    var selected by remember { mutableStateOf<Int?>(null) }

    val shake = remember { Animatable(0f) }
    LaunchedEffect(result) {
        if (result == Result.Wrong) {
            repeat(3) {
                shake.animateTo(-14f, tween(40))
                shake.animateTo(14f, tween(40))
            }
            shake.animateTo(0f, tween(40))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BotvaColors.BackgroundTop, BotvaColors.BackgroundBottom)
                )
            )
            .offset(x = shake.value.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassPanel(elevation = 0)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
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
                        modifier = Modifier.size(23.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    "Задание ${taskIndex + 1} из $tasksCount",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    fontFamily = Caveat,
                    color = BotvaColors.GreenDeep
                )
            }
            Text(
                "🪙 ${vm.coins}  ⭐ ${vm.totalStars}",
                fontSize = 15.sp,
                fontFamily = Caveat,
                color = BotvaColors.Terracotta
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassPanel(elevation = 1)
                .padding(16.dp)
        ) {
            Text(
                task.outputQuestion,
                fontSize = 20.sp,
                fontFamily = Caveat,
                fontWeight = FontWeight.Bold,
                color = BotvaColors.TextDark
            )
        }

        task.outputOptions.forEachIndexed { i, option ->
            val isSelected = selected == i
            val textColor = when {
                result == Result.Right && option.isCorrect -> BotvaColors.TextOnGreen
                result == Result.Wrong && isSelected -> BotvaColors.TextOnAccent
                else -> BotvaColors.TextDark
            }
            val optionModifier = when {
                result == Result.Right && option.isCorrect -> Modifier
                    .fillMaxWidth()
                    .background(BotvaColors.Green, RoundedCornerShape(18.dp))
                result == Result.Wrong && isSelected -> Modifier
                    .fillMaxWidth()
                    .background(BotvaColors.Error, RoundedCornerShape(18.dp))
                else -> Modifier
                    .fillMaxWidth()
                    .glassPanel(
                        shape = RoundedCornerShape(18.dp),
                        accent = result == null && isSelected,
                        elevation = 0
                    )
            }
            Box(
                modifier = optionModifier
                    .clickable(enabled = result == null) {
                        SoundManager.tap()
                        selected = i
                    }
                    .padding(16.dp)
            ) {
                Text(
                    option.text,
                    fontSize = 18.sp,
                    fontFamily = Caveat,
                    color = textColor
                )
            }
        }

        if (result == null) {
            Button(
                onClick = {
                    val idx = selected ?: return@Button
                    SoundManager.tap()
                    val correct = task.outputOptions[idx].isCorrect
                    onResult(if (correct) Result.Right else Result.Wrong)
                },
                enabled = selected != null,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BotvaColors.Green,
                    contentColor = BotvaColors.TextOnGreen
                )
            ) {
                Text("ПРОВЕРИТЬ", fontFamily = Caveat, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            HintButton(
                vm = vm,
                lessonId = lessonId,
                taskId = task.id,
                showHint = showHint,
                hint = task.hint,
                onShowHint = onShowHint
            )
        } else {
            ResultBlock(
                result = result!!,
                attempts = attempts,
                onRetry = {
                    SoundManager.tap()
                    selected = null
                    onResult(null)
                },
                onNext = {
                    SoundManager.tap()
                    onAdvance()
                }
            )
        }
    }
}

@Composable
private fun HintBlock(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(accent = true, elevation = 1)
            .padding(14.dp)
    ) {
        AccentText(
            text = text,
            accentColor = BotvaColors.GreenDeep,
            style = TextStyle(
                fontSize = 17.sp,
                fontFamily = Caveat,
                color = BotvaColors.TextDark
            )
        )
    }
}

@Composable
private fun StarReveal(count: Int) {
    var visible by remember { mutableIntStateOf(0) }
    LaunchedEffect(count) {
        visible = 0
        repeat(count) {
            delay(200)
            visible++
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(count) {
            if (it < visible) {
                Text("⭐", fontSize = 32.sp)
            } else {
                Spacer(Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun ResultBlock(
    result: Result,
    attempts: Int,
    onRetry: () -> Unit,
    onNext: () -> Unit
) {
    val resultModifier = if (result == Result.Right) {
        Modifier
            .fillMaxWidth()
            .glassPanel(accent = true, elevation = 1)
    } else {
        Modifier
            .fillMaxWidth()
            .background(BotvaColors.Error, RoundedCornerShape(20.dp))
    }

    Column(
        modifier = resultModifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (result == Result.Right) {
            val stars = starsForAttempts(attempts + 1)
            Text(
                "Верно!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Caveat,
                color = BotvaColors.TextOnGreen
            )
            StarReveal(stars)
            Text(
                "+${stars * 30} XP и монеты",
                fontSize = 18.sp,
                fontFamily = Caveat,
                color = BotvaColors.TextOnGreen
            )
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BotvaColors.TextOnGreen,
                    contentColor = BotvaColors.GreenDeep
                )
            ) {
                Text("ДАЛЬШЕ", fontFamily = Caveat, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                "Не вышло",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Caveat,
                color = BotvaColors.TextOnAccent
            )
            Text(
                "Попробуй ещё раз.",
                fontSize = 17.sp,
                fontFamily = Caveat,
                color = BotvaColors.TextOnAccent
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BotvaColors.Cream,
                    contentColor = BotvaColors.TerracottaDeep
                )
            ) {
                Text("ПОВТОРИТЬ", fontFamily = Caveat, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
