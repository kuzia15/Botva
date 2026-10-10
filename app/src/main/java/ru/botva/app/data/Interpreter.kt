package ru.botva.app.data

object Interpreter {

    private const val MAX_STEPS = 300

    fun run(program: List<Command>, start: FieldState): List<FieldState> {
        val history = mutableListOf(start)
        var current = start
        var steps = 0

        fun exec(cmds: List<Command>) {
            for (cmd in cmds) {
                if (steps++ > MAX_STEPS) {
                    current = current.copy(message = "Программа слишком длинная")
                    history.add(current)
                    return
                }
                current = when (cmd) {
                    Command.Forward   -> moveForward(current)
                    Command.TurnLeft  -> turn(current, -1)
                    Command.TurnRight -> turn(current, +1)
                    Command.Harvest   -> harvest(current)
                    Command.Water     -> water(current)
                    is Command.Repeat -> {
                        repeat(cmd.times) { exec(cmd.body) }
                        current
                    }
                    is Command.IfHarvest -> {
                        val cell = current.cells[current.robot.y][current.robot.x]
                        if (cell.hasCrop) exec(cmd.then) else exec(cmd.elseBranch)
                        current
                    }
                }
                history.add(current)
            }
        }

        exec(program)
        return history
    }

    private fun moveForward(s: FieldState): FieldState {
        val (dx, dy) = when (s.robot.direction) {
            Direction.UP    -> 0 to -1
            Direction.DOWN  -> 0 to 1
            Direction.LEFT  -> -1 to 0
            Direction.RIGHT -> 1 to 0
        }
        val nx = (s.robot.x + dx).coerceIn(0, s.width - 1)
        val ny = (s.robot.y + dy).coerceIn(0, s.height - 1)
        return s.copy(robot = s.robot.copy(x = nx, y = ny), message = null)
    }

    private fun turn(s: FieldState, delta: Int): FieldState {
        val order = Direction.entries
        val i = order.indexOf(s.robot.direction)
        val ni = (i + delta + order.size) % order.size
        return s.copy(robot = s.robot.copy(direction = order[ni]), message = null)
    }

    private fun harvest(s: FieldState): FieldState {
        val cell = s.cells[s.robot.y][s.robot.x]
        if (!cell.hasCrop) return s.copy(message = "Тут нечего собирать")
        val newCells = s.cells.mapIndexed { y, row ->
            row.mapIndexed { x, c ->
                if (x == s.robot.x && y == s.robot.y) c.copy(hasCrop = false, isGrown = false)
                else c
            }
        }
        return s.copy(cells = newCells, coins = s.coins + 10, message = "+10 монет")
    }

    private fun water(s: FieldState): FieldState {
        val newCells = s.cells.mapIndexed { y, row ->
            row.mapIndexed { x, c ->
                if (x == s.robot.x && y == s.robot.y) c.copy(isWatered = true, isGrown = true)
                else c
            }
        }
        return s.copy(cells = newCells, message = "Полито")
    }
}
