package android.app.producthunt.model

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val matchRoutes: Set<String> = setOf(route)
)