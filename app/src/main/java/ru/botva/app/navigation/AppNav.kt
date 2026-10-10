package ru.botva.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.botva.app.ui.screens.FinalScreen
import ru.botva.app.ui.screens.IntroScreen
import ru.botva.app.ui.screens.LessonScreen
import ru.botva.app.ui.screens.MapScreen
import ru.botva.app.ui.screens.StartScreen
import ru.botva.app.ui.screens.TaskScreen
import ru.botva.app.vm.GameViewModel

object Routes {
    const val START = "start"
    const val INTRO = "intro"
    const val MAP = "map"
    const val LESSON = "lesson/{lessonId}"
    const val TASK = "task/{lessonId}/{taskIndex}"
    const val FINAL = "final"

    fun lesson(id: Int) = "lesson/$id"
    fun task(lessonId: Int, taskIndex: Int) = "task/$lessonId/$taskIndex"
}

@Composable
fun AppNav(
    navController: NavHostController = rememberNavController(),
    vm: GameViewModel = viewModel()
) {
    val goToMenu = {
        navController.navigate(Routes.START) {
            popUpTo(0)
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = Routes.START) {

        composable(Routes.START) {
            StartScreen(
                onStart = {
                    val destination = if (vm.introSeen) Routes.MAP else Routes.INTRO
                    navController.navigate(destination)
                }
            )
        }

        composable(Routes.INTRO) {
            BackHandler(onBack = goToMenu)
            IntroScreen(
                onContinue = {
                    vm.markIntroSeen()
                    navController.navigate(Routes.MAP) {
                        popUpTo(Routes.START) { inclusive = true }
                    }
                },
                onBack = goToMenu
            )
        }

        composable(Routes.MAP) {
            BackHandler(onBack = goToMenu)
            MapScreen(
                vm = vm,
                onBack = goToMenu,
                onLessonClick = { id ->
                    vm.setCurrentLesson(id)
                    navController.navigate(Routes.lesson(id))
                }
            )
        }

        composable(Routes.LESSON) { backStack ->
            BackHandler { navController.popBackStack() }
            val id = backStack.arguments?.getString("lessonId")?.toIntOrNull() ?: 1
            LessonScreen(
                lesson = vm.lessonById(id),
                onToTask = {
                    val taskIndex = if (vm.currentLessonId == id) vm.currentTaskIndex else 0
                    navController.navigate(Routes.task(id, taskIndex))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TASK) { backStack ->
            BackHandler { navController.popBackStack() }
            val id = backStack.arguments?.getString("lessonId")?.toIntOrNull() ?: 1
            val taskIndex = backStack.arguments?.getString("taskIndex")?.toIntOrNull() ?: 0
            val lesson = vm.lessonById(id)

            TaskScreen(
                lesson = lesson,
                taskIndex = taskIndex,
                vm = vm,
                onFinished = {
                    val next = id + 1
                    if (next <= vm.totalLessons) {
                        navController.navigate(Routes.lesson(next)) {
                            popUpTo(Routes.MAP)
                        }
                    } else {
                        navController.navigate(Routes.FINAL) {
                            popUpTo(Routes.MAP)
                        }
                    }
                },
                onNextTask = {
                    vm.setCurrentTask(id, taskIndex + 1)
                    navController.navigate(Routes.task(id, taskIndex + 1)) {
                        popUpTo(Routes.LESSON)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FINAL) {
            BackHandler(onBack = goToMenu)
            FinalScreen(
                vm = vm,
                onRestart = {
                    vm.resetAll()
                    navController.navigate(Routes.START) { popUpTo(0) }
                }
            )
        }
    }
}
