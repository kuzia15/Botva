package ru.botva.app.data

enum class Direction { UP, RIGHT, DOWN, LEFT }

sealed class Command {
    data object Forward : Command()
    data object TurnLeft : Command()
    data object TurnRight : Command()
    data object Harvest : Command()
    data object Water : Command()
    data class Repeat(val times: Int, val body: List<Command>) : Command()
    data class IfHarvest(val then: List<Command>, val elseBranch: List<Command>) : Command()
}

data class CellState(
    val hasCrop: Boolean = false,
    val isWatered: Boolean = false,
    val isGrown: Boolean = false
)

data class RobotState(
    val x: Int,
    val y: Int,
    val direction: Direction = Direction.RIGHT
)

data class FieldState(
    val width: Int,
    val height: Int,
    val cells: List<List<CellState>>,
    val robot: RobotState,
    val coins: Int = 0,
    val message: String? = null
)

data class FieldSpec(
    val width: Int,
    val height: Int,
    val robotStart: RobotState,
    val cropPositions: List<Pair<Int, Int>>
) {
    fun toFieldState(): FieldState {
        val cells = List(height) { y ->
            List(width) { x ->
                CellState(hasCrop = cropPositions.contains(x to y))
            }
        }
        return FieldState(width, height, cells, robotStart)
    }
}

enum class TaskType { BUILD_PROGRAM, CHOOSE_OUTPUT }

data class OutputOption(val text: String, val isCorrect: Boolean)

data class Task(
    val id: Int,
    val type: TaskType,
    val spec: FieldSpec,
    val check: TaskCheck,
    val hint: String,
    val palette: List<Command> = emptyList(),
    val initialProgram: List<Command> = emptyList(),
    val outputQuestion: String = "",
    val outputOptions: List<OutputOption> = emptyList()
)

data class Lesson(
    val id: Int,
    val title: String,
    val subtitle: String,
    val theory: String,
    val example: String,
    val tasks: List<Task>
)
