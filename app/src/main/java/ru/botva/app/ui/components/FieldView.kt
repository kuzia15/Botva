package ru.botva.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.botva.app.R
import ru.botva.app.data.Direction
import ru.botva.app.data.FieldState
import ru.botva.app.ui.theme.BotvaColors
import ru.botva.app.ui.theme.Caveat

private val ColorFieldBg     = Color(0xFF151917)
private val ColorFieldBorder = Color(0xFF343B36)

@Composable
fun FieldView(
    state: FieldState,
    modifier: Modifier = Modifier,
    isMoving: Boolean = false
) {
    val w = state.width
    val h = state.height

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(w.toFloat() / h.toFloat())
    ) {
        val cellW = this.maxWidth / w
        val cellH = this.maxHeight / h

        val pulse = rememberInfiniteTransition(label = "pulse")
        val pulseScale by pulse.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        val bob = rememberInfiniteTransition(label = "bob")
        val bobOffset by bob.animateFloat(
            initialValue = -2f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(180, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bobOffset"
        )

        var prevCoins by remember { mutableIntStateOf(state.coins) }
        var coinKey by remember { mutableIntStateOf(0) }
        var coinAmount by remember { mutableIntStateOf(0) }
        var coinCellX by remember { mutableIntStateOf(0) }
        var coinCellY by remember { mutableIntStateOf(0) }

        LaunchedEffect(state.coins) {
            if (state.coins > prevCoins) {
                coinAmount = state.coins - prevCoins
                coinCellX = state.robot.x
                coinCellY = state.robot.y
                coinKey++
                prevCoins = state.coins
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = ColorFieldBg, size = size)
            for (x in 0..w) {
                drawLine(
                    color = ColorFieldBorder,
                    start = Offset(x * size.width / w, 0f),
                    end = Offset(x * size.width / w, size.height),
                    strokeWidth = 2f
                )
            }
            for (y in 0..h) {
                drawLine(
                    color = ColorFieldBorder,
                    start = Offset(0f, y * size.height / h),
                    end = Offset(size.width, y * size.height / h),
                    strokeWidth = 2f
                )
            }
        }

        for (y in 0 until h) {
            for (x in 0 until w) {
                val cell = state.cells[y][x]
                val res = if (cell.hasCrop) R.drawable.crop_grown else R.drawable.crop_empty
                val scale = if (cell.hasCrop) pulseScale else 1f
                Image(
                    painter = painterResource(res),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .offset(x = cellW * x, y = cellH * y)
                        .size(cellW, cellH)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }
        }

        val robotX by animateFloatAsState(
            targetValue = cellW.value * state.robot.x,
            animationSpec = tween(250),
            label = "robotX"
        )
        val robotY by animateFloatAsState(
            targetValue = cellH.value * state.robot.y,
            animationSpec = tween(250),
            label = "robotY"
        )
        val robotRot by animateFloatAsState(
            targetValue = rotationFor(state.robot.direction),
            animationSpec = tween(200),
            label = "robotRot"
        )

        val extraBob = if (isMoving) bobOffset else 0f

        Image(
            painter = painterResource(R.drawable.robot_idle),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset(x = robotX.dp, y = (robotY + extraBob).dp)
                .size(cellW, cellH)
                .graphicsLayer { rotationZ = robotRot }
        )

        if (coinKey > 0) {
            CoinFloat(
                key = coinKey,
                amount = coinAmount,
                x = cellW * coinCellX,
                y = cellH * coinCellY,
                cellH = cellH
            )
        }
    }
}

@Composable
private fun CoinFloat(key: Int, amount: Int, x: Dp, y: Dp, cellH: Dp) {
    val anim = remember(key) { Animatable(0f) }
    LaunchedEffect(key) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(800))
    }
    if (anim.value < 1f) {
        Text(
            "+$amount",
            color = BotvaColors.Terracotta,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Caveat,
            modifier = Modifier
                .offset(x = x, y = y - cellH * anim.value)
                .graphicsLayer { alpha = 1f - anim.value }
        )
    }
}

private fun rotationFor(direction: Direction): Float = when (direction) {
    Direction.UP -> 0f
    Direction.RIGHT -> 90f
    Direction.DOWN -> 180f
    Direction.LEFT -> 270f
}
