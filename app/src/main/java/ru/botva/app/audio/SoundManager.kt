package ru.botva.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import ru.botva.app.R

object SoundManager {

    private var pool: SoundPool? = null
    private var loaded = false

    private var tap = 0
    private var step = 0
    private var harvest = 0
    private var error = 0
    private var success = 0
    private var win = 0

    fun init(context: Context) {
        if (pool != null) return

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val p = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attrs)
            .build()

        p.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) loaded = true
        }

        tap     = p.load(context, R.raw.sfx_tap, 1)
        step    = p.load(context, R.raw.sfx_step, 1)
        harvest = p.load(context, R.raw.sfx_harvest, 1)
        error   = p.load(context, R.raw.sfx_error, 1)
        success = p.load(context, R.raw.sfx_success, 1)
        win     = p.load(context, R.raw.sfx_win, 1)

        pool = p
    }

    private fun play(id: Int, volume: Float = 1f) {
        if (!loaded || id == 0) return
        pool?.play(id, volume, volume, 1, 0, 1f)
    }

    fun tap()     = play(tap, 0.6f)
    fun step()    = play(step, 0.8f)
    fun harvest() = play(harvest, 1f)
    fun error()   = play(error, 1f)
    fun success() = play(success, 1f)
    fun win()     = play(win, 1f)
}
