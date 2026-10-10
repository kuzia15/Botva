package ru.botva.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import ru.botva.app.audio.SoundManager
import ru.botva.app.navigation.AppNav
import ru.botva.app.ui.theme.BotvaColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SoundManager.init(applicationContext)

        setContent {
            val colors = darkColorScheme(
                primary = BotvaColors.Green,
                onPrimary = BotvaColors.TextOnGreen,
                secondary = BotvaColors.Terracotta,
                onSecondary = BotvaColors.TextOnAccent,
                background = BotvaColors.Cream,
                onBackground = BotvaColors.TextDark,
                surface = BotvaColors.CreamDeep,
                onSurface = BotvaColors.TextDark,
                surfaceVariant = BotvaColors.GreenLight,
                onSurfaceVariant = BotvaColors.TextMuted,
                error = BotvaColors.Error
            )

            MaterialTheme(colorScheme = colors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BotvaColors.Cream
                ) {
                    AppNav()
                }
            }
        }

        window.decorView.post { enterFullscreen() }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.post { enterFullscreen() }
        }
    }

    private fun enterFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
