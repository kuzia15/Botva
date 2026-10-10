package ru.botva.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class GameProgress(
    val coins: Int = 0,
    val xp: Int = 0,
    val stars: Map<Int, Int> = emptyMap(),
    val currentLessonId: Int = 1,
    val currentTaskIndex: Int = 0,
    val introSeen: Boolean = false,
    val revealedHints: Set<String> = emptySet(),
    val taskAttempts: Map<String, Int> = emptyMap()
)

class ProgressStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): GameProgress = GameProgress(
        coins = prefs.getInt(KEY_COINS, 0),
        xp = prefs.getInt(KEY_XP, 0),
        stars = decodeIntMap(prefs.getString(KEY_STARS, null)),
        currentLessonId = prefs.getInt(KEY_CURRENT_LESSON, 1),
        currentTaskIndex = prefs.getInt(KEY_CURRENT_TASK, 0),
        introSeen = prefs.getBoolean(KEY_INTRO_SEEN, false),
        revealedHints = prefs.getStringSet(KEY_REVEALED_HINTS, emptySet())?.toSet().orEmpty(),
        taskAttempts = decodeStringIntMap(prefs.getString(KEY_TASK_ATTEMPTS, null))
    )

    fun save(progress: GameProgress) {
        prefs.edit()
            .putInt(KEY_COINS, progress.coins)
            .putInt(KEY_XP, progress.xp)
            .putString(KEY_STARS, encodeIntMap(progress.stars))
            .putInt(KEY_CURRENT_LESSON, progress.currentLessonId)
            .putInt(KEY_CURRENT_TASK, progress.currentTaskIndex)
            .putBoolean(KEY_INTRO_SEEN, progress.introSeen)
            .putStringSet(KEY_REVEALED_HINTS, progress.revealedHints)
            .putString(KEY_TASK_ATTEMPTS, encodeStringIntMap(progress.taskAttempts))
            .commit()
    }

    fun loadProgram(lessonId: Int, taskId: Int): List<Command> {
        val raw = prefs.getString(programKey(lessonId, taskId), null) ?: return emptyList()
        return runCatching { decodeProgram(raw) }.getOrDefault(emptyList())
    }

    fun saveProgram(lessonId: Int, taskId: Int, program: List<Command>) {
        prefs.edit()
            .putString(programKey(lessonId, taskId), encodeProgram(program))
            .commit()
    }

    fun clearProgram(lessonId: Int, taskId: Int) {
        prefs.edit().remove(programKey(lessonId, taskId)).commit()
    }

    fun clearAll() {
        prefs.edit().clear().commit()
    }

    private fun programKey(lessonId: Int, taskId: Int) =
        "${KEY_PROGRAM_PREFIX}${lessonId}_$taskId"

    private fun encodeIntMap(map: Map<Int, Int>): String =
        map.entries
            .sortedBy { it.key }
            .joinToString(";") { "${it.key}:${it.value}" }

    private fun decodeIntMap(raw: String?): Map<Int, Int> =
        raw.orEmpty()
            .split(';')
            .mapNotNull { entry ->
                val parts = entry.split(':', limit = 2)
                val key = parts.getOrNull(0)?.toIntOrNull()
                val value = parts.getOrNull(1)?.toIntOrNull()
                if (key != null && value != null) key to value else null
            }
            .toMap()

    private fun encodeStringIntMap(map: Map<String, Int>): String =
        map.entries
            .sortedBy { it.key }
            .joinToString(";") { "${it.key}:${it.value}" }

    private fun decodeStringIntMap(raw: String?): Map<String, Int> =
        raw.orEmpty()
            .split(';')
            .mapNotNull { entry ->
                val parts = entry.split(':', limit = 2)
                val key = parts.getOrNull(0)?.takeIf { it.isNotBlank() }
                val value = parts.getOrNull(1)?.toIntOrNull()
                if (key != null && value != null) key to value else null
            }
            .toMap()

    private fun encodeProgram(program: List<Command>): String =
        commandsToJson(program).toString()

    private fun decodeProgram(raw: String): List<Command> =
        commandsFromJson(JSONArray(raw))

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
