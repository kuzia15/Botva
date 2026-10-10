package ru.botva.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterpreterTest {

    @Test
    fun forwardMovesRobotRight() {
        val spec = FieldSpec(5, 1, RobotState(0, 0, Direction.RIGHT), emptyList())
        val history = Interpreter.run(listOf(Command.Forward, Command.Forward), spec.toFieldState())
        val final = history.last()
        assertEquals(2, final.robot.x)
        assertEquals(0, final.robot.y)
    }

    @Test
    fun harvestCollectsCropAndAddsCoins() {
        val spec = FieldSpec(3, 1, RobotState(0, 0, Direction.RIGHT), listOf(2 to 0))
        val program = listOf(Command.Forward, Command.Forward, Command.Harvest)
        val history = Interpreter.run(program, spec.toFieldState())
        val final = history.last()
        assertEquals(10, final.coins)
        assertTrue(final.cells.flatten().none { it.hasCrop })
    }

    @Test
    fun repeatExecutesBodyMultipleTimes() {
        val spec = FieldSpec(5, 1, RobotState(0, 0, Direction.RIGHT), listOf(3 to 0))
        val program = listOf(
            Command.Repeat(3, listOf(Command.Forward)),
            Command.Harvest
        )
        val history = Interpreter.run(program, spec.toFieldState())
        val final = history.last()
        assertEquals(3, final.robot.x)
        assertTrue(final.cells.flatten().none { it.hasCrop })
    }

    @Test
    fun ifHarvestBranchesCorrectly() {
        val spec = FieldSpec(3, 1, RobotState(0, 0, Direction.RIGHT), listOf(1 to 0))
        val program = listOf(
            Command.Forward,
            Command.IfHarvest(
                then = listOf(Command.Harvest),
                elseBranch = listOf(Command.Water)
            )
        )
        val history = Interpreter.run(program, spec.toFieldState())
        val final = history.last()
        assertEquals(10, final.coins)
        assertFalse(final.cells[0][1].hasCrop)
    }

    @Test
    fun checkTaskAllCropsHarvested() {
        val spec = FieldSpec(5, 1, RobotState(0, 0, Direction.RIGHT), listOf(2 to 0, 4 to 0))
        val program = listOf(
            Command.Repeat(2, listOf(Command.Forward, Command.Forward, Command.Harvest))
        )
        assertTrue(checkTask(TaskCheck.AllCropsHarvested, program, spec))
    }

    @Test
    fun checkTaskFailsWhenCropNotHarvested() {
        val spec = FieldSpec(5, 1, RobotState(0, 0, Direction.RIGHT), listOf(2 to 0, 4 to 0))
        val program = listOf(Command.Forward, Command.Forward, Command.Harvest)
        assertFalse(checkTask(TaskCheck.AllCropsHarvested, program, spec))
    }
}
