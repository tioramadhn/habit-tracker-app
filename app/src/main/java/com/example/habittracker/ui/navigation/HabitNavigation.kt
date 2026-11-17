package com.example.habittracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habittracker.ui.detail.HabitDetailScreen
import com.example.habittracker.ui.editor.HabitEditorScreen
import com.example.habittracker.ui.home.HomeScreen
import com.example.habittracker.ui.stats.StatsScreen

object Routes {
    const val TODAY = "today"
    const val STATS = "stats"
    const val DETAIL = "habit/{habitId}"
    const val EDITOR = "editor?habitId={habitId}"

    fun detail(habitId: Long) = "habit/$habitId"
    fun editor(habitId: Long = 0L) = "editor?habitId=$habitId"
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.TODAY, "Hari ini", Icons.Default.CheckCircle),
    TopLevelDestination(Routes.STATS, "Statistik", Icons.Default.DateRange),
)

@Composable
fun HabitTrackerNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bottomBar: @Composable () -> Unit = {
        NavigationBar {
            topLevelDestinations.forEach { destination ->
                NavigationBarItem(
                    selected = currentRoute == destination.route,
                    onClick = {
                        if (currentRoute != destination.route) {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(destination.icon, contentDescription = null) },
                    label = { Text(destination.label) },
                )
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.TODAY) {
        composable(Routes.TODAY) {
            HomeScreen(
                onHabitClick = { navController.navigate(Routes.detail(it)) },
                onAddHabit = { navController.navigate(Routes.editor()) },
                bottomBar = bottomBar,
            )
        }

        composable(Routes.STATS) {
            StatsScreen(
                onHabitClick = { navController.navigate(Routes.detail(it)) },
                bottomBar = bottomBar,
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType }),
        ) { entry ->
            val habitId = entry.arguments?.getLong("habitId") ?: 0L
            HabitDetailScreen(
                habitId = habitId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.editor(it)) },
            )
        }

        composable(
            route = Routes.EDITOR,
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
            ),
        ) { entry ->
            val habitId = entry.arguments?.getLong("habitId") ?: 0L
            HabitEditorScreen(
                habitId = habitId,
                onClose = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }
}
