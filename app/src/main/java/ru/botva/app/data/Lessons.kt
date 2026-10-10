package ru.botva.app.data

object Lessons {

    val all: List<Lesson> = listOf(

        Lesson(
            id = 1,
            title = "Вводный урок",
            subtitle = "Начнём с главного: как управлять роботом и собирать морковки.",
            theory = "Наша **цель** — помочь роботу собрать все морковки! Для этого составь **программу** — команды, которые робот выполнит по порядку. **Вперёд** — шаг на одну клетку. **Влево** и **вправо** — повернуться на месте. **Собрать** — взять морковку в клетке, на которой стоит робот.",
            example = "Пример программы: вперёд → вперёд → вправо → вперёд → вперёд → собрать.",
            tasks = listOf(
                Task(
                    id = 1,
                    type = TaskType.BUILD_PROGRAM,
                    spec = FieldSpec(
                        width = 3, height = 3,
                        robotStart = RobotState(0, 0, Direction.RIGHT),
                        cropPositions = listOf(2 to 2)
                    ),
                    check = TaskCheck.AllCropsHarvested,
                    hint = "Нужно добраться до морковки: два шага **вперёд**, поворот **вправо**, ещё два шага **вперёд** и **собрать**.",
                    palette = listOf(
                        Command.Forward, Command.TurnLeft,
                        Command.TurnRight, Command.Harvest
                    )
                ),
                Task(
                    id = 2,
                    type = TaskType.CHOOSE_OUTPUT,
                    spec = FieldSpec(
                        width = 3, height = 3,
                        robotStart = RobotState(0, 0, Direction.RIGHT),
                        cropPositions = listOf(1 to 1)
                    ),
                    check = TaskCheck.AllCropsHarvested,
                    hint = "Поворот меняет направление, но робот остаётся на той же клетке. Представь, что поворачиваешь направо, глядя вправо.",
                    outputQuestion = "Робот смотрит вправо. Как повернуть его, чтобы он смотрел вниз?",
                    outputOptions = listOf(
                        OutputOption("Повернуть вправо", true),
                        OutputOption("Повернуть влево", false),
                        OutputOption("Шагнуть вперёд", false)
                    )
                )
            )
        ),

        Lesson(
            id = 2,
            title = "Сбор урожая",
            subtitle = "Дойти мало. Надо ещё собрать то, что под роботом.",
            theory = "Команда **собрать** работает только на клетке с грядкой. Если грядки нет, робот скажет, что собирать нечего.",
            example = "Две вперёд, собрать, вперёд, вперёд, собрать.",
            tasks = listOf(
                Task(
                    id = 1,
                    type = TaskType.BUILD_PROGRAM,
                    spec = FieldSpec(5, 1, RobotState(0, 0, Direction.RIGHT), listOf(1 to 0, 4 to 0)),
                    check = TaskCheck.AllCropsHarvested,
                    hint = "Первая грядка близко, вторая далеко. Порядок: шаг, собрал, три шага, собрал.",
                    palette = listOf(Command.Forward, Command.Harvest, Command.TurnRight)
                ),
                Task(
                    id = 2,
                    type = TaskType.BUILD_PROGRAM,
                    spec = FieldSpec(4, 1, RobotState(0, 0, Direction.RIGHT), listOf(1 to 0, 3 to 0)),
                    check = TaskCheck.AllCropsHarvested,
                    hint = "Сначала шагни **вперёд** и **собери** первую грядку. Потом ещё два шага и собери вторую.",
                    palette = listOf(Command.Forward, Command.Harvest)
                )
            )
        ),

        Lesson(
            id = 3,
            title = "Цикл",
            subtitle = "Одинаковые команды можно повторять. Это цикл.",
            theory = "**Цикл** это повтор. Пишешь команду один раз, а робот делает её столько раз, сколько скажешь.",
            example = "Повтори 4 раза: вперёд, собрать.",
            tasks = listOf(
                Task(
                    id = 1,
                    type = TaskType.CHOOSE_OUTPUT,
                    spec = FieldSpec(5, 1, RobotState(0, 0, Direction.RIGHT), listOf(1 to 0, 2 to 0, 3 to 0, 4 to 0)),
                    check = TaskCheck.AllCropsHarvested,
                    hint = "В каждом повторении робот делает шаг и собирает одну морковку. Посчитай три повторения.",
                    outputQuestion = "Робот 3 раза повторит команды «Вперёд» и «Собрать». На каждой следующей клетке есть морковка. Сколько морковок он соберёт?",
                    outputOptions = listOf(
                        OutputOption("3 морковки", true),
                        OutputOption("6 морковок", false),
                        OutputOption("1 морковку", false)
                    )
                ),
                Task(
                    id = 2,
                    type = TaskType.BUILD_PROGRAM,
                    spec = FieldSpec(5, 1, RobotState(0, 0, Direction.RIGHT), listOf(1 to 0, 2 to 0, 3 to 0, 4 to 0)),
                    check = TaskCheck.Combined(
                        listOf(
                            TaskCheck.AllCropsHarvested,
                            TaskCheck.ProgramContains { program ->
                                program.any { it is Command.Repeat }
                            }
                        )
                    ),
                    hint = "Собери всё через **цикл**. Без цикла программа будет длинной.",
                    palette = listOf(
                        Command.Repeat(0, emptyList()),
                        Command.Forward,
                        Command.Harvest
                    )
                )
            )
        ),

        Lesson(
            id = 4,
            title = "Условие",
            subtitle = "Робот сам разберётся, где урожай. Ты только задаёшь правило.",
            theory = "**Условие** проверяет клетку под роботом. Урожай есть: собери. Пусто: иди дальше.",
            example = "Попробуй на ячейке с морковками и без выполнить условие (кнопка ?), и посмотри",
            tasks = listOf(
                Task(
                    id = 1,
                    type = TaskType.BUILD_PROGRAM,
                    spec = FieldSpec(5, 1, RobotState(0, 0, Direction.RIGHT), listOf(2 to 0)),
                    check = TaskCheck.Combined(
                        listOf(
                            TaskCheck.AllCropsHarvested,
                            TaskCheck.ProgramContains { containsIfHarvest(it) }
                        )
                    ),
                    hint = "Используй **условие**. Робот сам пойдёт, найдёт грядку, соберёт.",
                    palette = listOf(
                        Command.IfHarvest(
                            then = listOf(Command.Harvest),
                            elseBranch = listOf(Command.Forward)
                        ),
                        Command.Forward,
                        Command.Harvest
                    )
                ),
                Task(
                    id = 2,
                    type = TaskType.BUILD_PROGRAM,
                    spec = FieldSpec(5, 1, RobotState(0, 0, Direction.RIGHT), listOf(1 to 0, 3 to 0)),
                    check = TaskCheck.Combined(
                        listOf(
                            TaskCheck.AllCropsHarvested,
                            TaskCheck.ProgramContains { containsIfHarvest(it) }
                        )
                    ),
                    hint = "Две грядки на пути. Условие проверит каждую клетку.",
                    palette = listOf(
                        Command.IfHarvest(
                            then = listOf(Command.Harvest),
                            elseBranch = listOf(Command.Forward)
                        ),
                        Command.Forward,
                        Command.Harvest
                    )
                )
            )
        ),

        Lesson(
            id = 5,
            title = "Всё вместе",
            subtitle = "Проверь каждую клетку на пути. Условие подскажет, когда собирать.",
            theory = """
                Первая грядка находится рядом с роботом, остальные дальше по пути.
                **Повторяй** шаги и используй **условие**: робот проверит каждую клетку и соберёт урожай там, где он есть.
            """.trimIndent(),
            example = "Повтори 2 раза: вправо, вперёд, если урожай: собрать, влево, вперёд, если урожай: собрать.",
            tasks = listOf(
                Task(
                    id = 1,
                    type = TaskType.BUILD_PROGRAM,
                    spec = FieldSpec(
                        width = 3, height = 3,
                        robotStart = RobotState(0, 0, Direction.RIGHT),
                        cropPositions = listOf(0 to 1, 1 to 1, 2 to 2)
                    ),
                    check = TaskCheck.Combined(
                        listOf(
                            TaskCheck.AllCropsHarvested,
                            TaskCheck.ProgramContains { containsIfHarvest(it) }
                        )
                    ),
                    hint = "**Повтори 2 раза**: вправо, вперёд, если урожай — собрать; влево, вперёд, если урожай — собрать.",
                    palette = listOf(
                        Command.Repeat(0, emptyList()),
                        Command.Harvest,
                        Command.Forward,
                        Command.TurnLeft,
                        Command.TurnRight,
                        Command.IfHarvest(
                            then = listOf(Command.Harvest),
                            elseBranch = listOf()
                        )
                    )
                ),
                Task(
                    id = 2,
                    type = TaskType.BUILD_PROGRAM,
                    spec = FieldSpec(
                        width = 5, height = 5,
                        robotStart = RobotState(0, 0, Direction.RIGHT),
                        cropPositions = listOf(0 to 1, 2 to 2, 4 to 4)
                    ),
                    check = TaskCheck.Combined(
                        listOf(
                            TaskCheck.AllCropsHarvested,
                            TaskCheck.ProgramContains { containsIfHarvest(it) }
                        )
                    ),
                    hint = "Первая грядка в клетке ниже робота. **Повтори 4 раза**: вправо, вперёд, условие; влево, вперёд, условие.",
                    palette = listOf(
                        Command.Repeat(0, emptyList()),
                        Command.Harvest,
                        Command.Forward,
                        Command.TurnLeft,
                        Command.TurnRight,
                        Command.IfHarvest(
                            then = listOf(Command.Harvest),
                            elseBranch = listOf()
                        )
                    )
                )
            )
        )
    )

    private fun containsIfHarvest(program: List<Command>): Boolean {
        return program.any { cmd ->
            when (cmd) {
                is Command.IfHarvest -> true
                is Command.Repeat -> containsIfHarvest(cmd.body)
                else -> false
            }
        }
    }
}
