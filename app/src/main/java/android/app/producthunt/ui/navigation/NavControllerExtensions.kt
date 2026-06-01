package android.app.producthunt.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

fun NavController.navigateToTopLevelDestination(route: String) {
    navigate(route) {
        launchSingleTop = true
        popUpTo(graph.findStartDestination().id) {
            saveState = false
        }
    }
}
