package ru.botva.app.data

sealed class TaskCheck {
    data object AllCropsHarvested : TaskCheck()
    data class ReachCell(val x: Int, val y: Int) : TaskCheck()
    data class ProgramContains(val predicate: (List<Command>) -> Boolean) : TaskCheck()
    data class Combined(val checks: List<TaskCheck>) : TaskCheck()
}

fun checkTask(check: TaskCheck, program: List<Command>, spec: FieldSpec): Boolean {
    val history = Interpreter.run(program, spec.toFieldState())
    val final = history.last()
    return when (check) {
        TaskCheck.AllCropsHarvested ->
            final.cells.flatten().none { it.hasCrop }

        is TaskCheck.ReachCell ->
            final.robot.x == check.x && final.robot.y == check.y

        is TaskCheck.ProgramContains ->
            check.predicate(program)

        is TaskCheck.Combined ->
            check.checks.all { checkTask(it, program, spec) }
    }
}
