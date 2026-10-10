package ru.botva.app.vm

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.json.JSONArray
import org.json.JSONObject
import ru.botva.app.data.Command
import ru.botva.app.data.Lesson
import ru.botva.app.data.Lessons

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val lessons: List<Lesson> = Lessons.all

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .also(::migrateProgressIfNeeded)

    private fun migrateProgressIfNeeded(saved: SharedPreferences) {
        if (saved.getInt(KEY_CONTENT_VERSION, 1) >= CONTENT_VERSION) return

        val editor = saved.edit().putInt(KEY_CONTENT_VERSION, CONTENT_VERSION)
        if (!saved.contains(KEY_CURRENT_LESSON)) {
            editor.commit()
            return
        }

        fun newTaskKey(oldKey: String): String? {
            val parts = oldKey.split('-', limit = 2)
            val oldId = parts.getOrNull(0)?.toIntOrNull() ?: return null
            val taskId = parts.getOrNull(1)?.toIntOrNull() ?: return null
            return if (oldId in 2..6) "${oldId - 1}-$taskId" else null
        }

        val shiftedStars = saved.getString(KEY_STARS, "").orEmpty()
            .split(';').mapNotNull { entry ->
                val parts = entry.split(':', limit = 2)
                val oldId = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                val value = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                if (oldId in 2..6) "${oldId - 1}:$value" else null
            }.joinToString(";")
        editor.putString(KEY_STARS, shiftedStars)

        val shiftedHints = saved.getStringSet(KEY_REVEALED_HINTS, emptySet()).orEmpty()
            .mapNotNull(::newTaskKey).toSet()
        editor.putStringSet(KEY_REVEALED_HINTS, shiftedHints)

        val shiftedAttempts = saved.getString(KEY_TASK_ATTEMPTS, "").orEmpty()
            .split(';').mapNotNull { entry ->
                val parts = entry.split(':', limit = 2)
                newTaskKey(parts[0])?.let { "$it:${parts.getOrNull(1).orEmpty()}" }
            }.joinToString(";")
        editor.putString(KEY_TASK_ATTEMPTS, shiftedAttempts)

        val oldPrograms = saved.all.filterKeys { it.startsWith(KEY_PROGRAM_PREFIX) }
        oldPrograms.keys.forEach { editor.remove(it) }
        oldPrograms.forEach { (key, value) ->
            val parts = key.removePrefix(KEY_PROGRAM_PREFIX).split('_', limit = 2)
            val oldId = parts.getOrNull(0)?.toIntOrNull() ?: return@forEach
            val taskId = parts.getOrNull(1)?.toIntOrNull() ?: return@forEach
            if (oldId in 2..5 && !(oldId == 3 && taskId == 2) && value is String) {
                editor.putString("${KEY_PROGRAM_PREFIX}${oldId - 1}_$taskId", value)
            }
        }

        val oldLessonId = saved.getInt(KEY_CURRENT_LESSON, 1)
        editor.putInt(KEY_CURRENT_LESSON, (oldLessonId - 1).coerceIn(1, 5))
        if (oldLessonId == 1) editor.putInt(KEY_CURRENT_TASK, 0)
        editor.putBoolean(KEY_INTRO_SEEN, false)
        editor.commit()
    }

    var coins by mutableStateOf(prefs.getInt(KEY_COINS, 0).coerceAtLeast(0))
        private set

    var xp by mutableStateOf(prefs.getInt(KEY_XP, 0).coerceAtLeast(0))
        private set

    var stars by mutableStateOf<Map<Int, Int>>(loadStars())
        private set

    var currentLessonId by mutableStateOf(
        prefs.getInt(KEY_CURRENT_LESSON, 1).coerceIn(1, lessons.size.coerceAtLeast(1))
    )
        private set

    var currentTaskIndex by mutableStateOf(prefs.getInt(KEY_CURRENT_TASK, 0).coerceAtLeast(0))
        private set

    var introSeen by mutableStateOf(prefs.getBoolean(KEY_INTRO_SEEN, false))
        private set

    private var revealedHints: Set<String> =
        prefs.getStringSet(KEY_REVEALED_HINTS, emptySet())?.toSet().orEmpty()

    private var taskAttempts: Map<String, Int> = loadAttempts()

    init {
        currentTaskIndex = currentTaskIndex.coerceIn(
            0,
            lessonById(currentLessonId).tasks.lastIndex.coerceAtLeast(0)
        )
        persist()
    }

    val completedCount: Int
        get() = stars.count { it.value > 0 }

    val totalLessons: Int
        get() = lessons.size

    val totalStars: Int
        get() = stars.values.sum()

    val maxStars: Int
        get() = lessons.size * 3

    fun lessonById(id: Int): Lesson =
        lessons.first { it.id == id }

    fun isLessonUnlocked(id: Int): Boolean =
        id == 1 || (stars[id - 1] ?: 0) > 0

    fun setCurrentLesson(id: Int) {
        if (!isLessonUnlocked(id)) return
        if (currentLessonId != id) currentTaskIndex = 0
        currentLessonId = id
        persist()
    }

    fun setCurrentTask(lessonId: Int, taskIndex: Int) {
        if (!isLessonUnlocked(lessonId)) return
        val maxIndex = lessonById(lessonId).tasks.lastIndex.coerceAtLeast(0)
        currentLessonId = lessonId
        currentTaskIndex = taskIndex.coerceIn(0, maxIndex)
        persist()
    }

    fun markIntroSeen() {
        if (introSeen) return
        introSeen = true
        persist()
    }

    fun addCoins(amount: Int) {
        if (amount <= 0) return
        coins += amount
        persist()
    }

    fun spendCoins(amount: Int): Boolean {
        if (amount <= 0) return true
        if (coins < amount) return false
        coins -= amount
        persist()
        return true
    }

    fun isHintRevealed(lessonId: Int, taskId: Int): Boolean =
        taskKey(lessonId, taskId) in revealedHints

    fun buyHint(lessonId: Int, taskId: Int, price: Int): Boolean {
        val key = taskKey(lessonId, taskId)
        if (key in revealedHints) return true
        if (price > 0 && coins < price) return false

        if (price > 0) coins -= price
        revealedHints = revealedHints + key
        persist()
        return true
    }

    fun attemptsFor(lessonId: Int, taskId: Int): Int =
        taskAttempts[taskKey(lessonId, taskId)] ?: 0

    fun addAttempt(lessonId: Int, taskId: Int) {
        val key = taskKey(lessonId, taskId)
        taskAttempts = taskAttempts + (key to ((taskAttempts[key] ?: 0) + 1))
        persist()
    }

    fun programFor(lessonId: Int, taskId: Int): List<Command> {
        val raw = prefs.getString(programKey(lessonId, taskId), null) ?: return emptyList()
        return runCatching { commandsFromJson(JSONArray(raw)) }.getOrDefault(emptyList())
    }

    fun saveProgram(lessonId: Int, taskId: Int, program: List<Command>) {
        prefs.edit()
            .putString(programKey(lessonId, taskId), commandsToJson(program).toString())
            .apply()
    }

    fun finishTask(lessonId: Int, taskId: Int) {
        prefs.edit().remove(programKey(lessonId, taskId)).apply()

        val key = taskKey(lessonId, taskId)
        if (key in taskAttempts) {
            taskAttempts = taskAttempts - key
            persist()
        }
    }

    fun completeLesson(lessonId: Int, starsCount: Int) {
        val clamped = starsCount.coerceIn(1, 3)
        val prev = stars[lessonId] ?: 0
        if (clamped > prev) {
            stars = stars + (lessonId to clamped)
        }

        xp += 30 * clamped

        currentLessonId = (lessonId + 1).coerceAtMost(totalLessons)
        currentTaskIndex = 0
        persist()
    }

    fun resetAll() {
        prefs.edit().clear().putInt(KEY_CONTENT_VERSION, CONTENT_VERSION).commit()
        coins = 0
        xp = 0
        stars = emptyMap()
        currentLessonId = 1
        currentTaskIndex = 0
        introSeen = false
        revealedHints = emptySet()
        taskAttempts = emptyMap()
    }

    private fun persist() {
        prefs.edit()
            .putInt(KEY_CONTENT_VERSION, CONTENT_VERSION)
            .putInt(KEY_COINS, coins)
            .putInt(KEY_XP, xp)
            .putString(KEY_STARS, encodeStars(stars))
            .putInt(KEY_CURRENT_LESSON, currentLessonId)
            .putInt(KEY_CURRENT_TASK, currentTaskIndex)
            .putBoolean(KEY_INTRO_SEEN, introSeen)
            .putStringSet(KEY_REVEALED_HINTS, revealedHints)
            .putString(KEY_TASK_ATTEMPTS, encodeAttempts(taskAttempts))
            .commit()
    }

    private fun loadStars(): Map<Int, Int> =
        prefs.getString(KEY_STARS, null)
            .orEmpty()
            .split(';')
            .mapNotNull { entry ->
                val parts = entry.split(':', limit = 2)
                val id = parts.getOrNull(0)?.toIntOrNull()
                val value = parts.getOrNull(1)?.toIntOrNull()
                if (id != null && value != null && lessons.any { it.id == id }) {
                    id to value.coerceIn(0, 3)
                } else {
                    null
                }
            }
            .toMap()

    private fun encodeStars(value: Map<Int, Int>): String =
        value.entries
            .sortedBy { it.key }
            .joinToString(";") { "${it.key}:${it.value}" }

    private fun loadAttempts(): Map<String, Int> =
        prefs.getString(KEY_TASK_ATTEMPTS, null)
            .orEmpty()
            .split(';')
            .mapNotNull { entry ->
                val parts = entry.split(':', limit = 2)
                val key = parts.getOrNull(0)?.takeIf { it.isNotBlank() }
                val value = parts.getOrNull(1)?.toIntOrNull()
                if (key != null && value != null) key to value.coerceAtLeast(0) else null
            }
            .toMap()

    private fun encodeAttempts(value: Map<String, Int>): String =
        value.entries
            .sortedBy { it.key }
            .joinToString(";") { "${it.key}:${it.value}" }

    private fun taskKey(lessonId: Int, taskId: Int) = "$lessonId-$taskId"

    private fun programKey(lessonId: Int, taskId: Int) =
        "${KEY_PROGRAM_PREFIX}${lessonId}_$taskId"

    private fun commandsToJson(commands: List<Command>): JSONArray = JSONArray().apply {
        commands.forEach { put(commandToJson(it)) }
    }

    private fun commandToJson(command: Command): JSONObject = when (command) {
        Command.Forward -> JSONObject().put("type", "forward")
        Command.TurnLeft -> JSONObject().put("type", "left")
        Command.TurnRight -> JSONObject().put("type", "right")
        Command.Harvest -> JSONObject().put("type", "harvest")
        Command.Water -> JSONObject().put("type", "water")
        is Command.Repeat -> JSONObject()
            .put("type", "repeat")
            .put("times", command.times)
            .put("body", commandsToJson(command.body))
        is Command.IfHarvest -> JSONObject()
            .put("type", "ifHarvest")
            .put("then", commandsToJson(command.then))
            .put("else", commandsToJson(command.elseBranch))
    }

    private fun commandsFromJson(array: JSONArray): List<Command> = buildList {
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            commandFromJson(obj)?.let(::add)
        }
    }

    private fun commandFromJson(obj: JSONObject): Command? = when (obj.optString("type")) {
        "forward" -> Command.Forward
        "left" -> Command.TurnLeft
        "right" -> Command.TurnRight
        "harvest" -> Command.Harvest
        "water" -> Command.Water
        "repeat" -> Command.Repeat(
            times = obj.optInt("times", 1).coerceIn(1, 99),
            body = commandsFromJson(obj.optJSONArray("body") ?: JSONArray())
        )
        "ifHarvest" -> Command.IfHarvest(
            then = commandsFromJson(obj.optJSONArray("then") ?: JSONArray()),
            elseBranch = commandsFromJson(obj.optJSONArray("else") ?: JSONArray())
        )
        else -> null
    }

    private companion object {
        const val CONTENT_VERSION = 2
        const val KEY_CONTENT_VERSION = "content_version"
        const val PREFS_NAME = "botva_progress_v1"
        const val KEY_COINS = "coins"
        const val KEY_XP = "xp"
        const val KEY_STARS = "stars"
        const val KEY_CURRENT_LESSON = "current_lesson"
        const val KEY_CURRENT_TASK = "current_task"
        const val KEY_INTRO_SEEN = "intro_seen"
        const val KEY_REVEALED_HINTS = "revealed_hints"
        const val KEY_TASK_ATTEMPTS = "task_attempts"
        const val KEY_PROGRAM_PREFIX = "program_"
    }
}
