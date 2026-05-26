package android.app.producthunt.ui.components.appbar

import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.navigation.TopLevelRoutes
import android.app.producthunt.ui.navigation.baseRouteOrNull
import android.app.producthunt.ui.navigation.navigateToTopLevelDestination
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val matchRoutes: Set<String> = setOf(route)
)

@Composable
fun MainNavBar(
    navController: NavController,
    showOnRoutes: Set<String> = TopLevelRoutes.toSet(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    MainNavBarContent(
        isVisible = currentDestination.isInRoutes(showOnRoutes),
        isSelected = { matchRoutes -> currentDestination.isInRoutes(matchRoutes) },
        onItemClick = { route ->
            navController.navigateToTopLevelDestination(route)
        }
    )
}

@Composable
private fun MainNavBarContent(
    isVisible: Boolean,
    isSelected: (Set<String>) -> Boolean,
    onItemClick: (String) -> Unit
) {
    val items = remember {
        listOf(
            BottomNavItem(Route.FEED, "Feed", Icons.Default.RssFeed),
            BottomNavItem(Route.SEARCH, "Search", Icons.Default.ChatBubbleOutline),
            BottomNavItem(Route.WISHLIST, "Wishlist", Icons.Default.FavoriteBorder),
        )
    }

    val duration = 220
    val colorScheme = MaterialTheme.colorScheme

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(duration)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(duration))
    ) {
        NavigationBar(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface
        ) {
            items.forEach { item ->
                val selected = isSelected(item.matchRoutes)
                NavigationBarItem(
                    selected = selected,
                    onClick = { onItemClick(item.route) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorScheme.primary,
                        selectedTextColor = colorScheme.primary,
                        indicatorColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        unselectedIconColor = colorScheme.onSurfaceVariant,
                        unselectedTextColor = colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

private fun NavDestination?.isInRoutes(routes: Set<String>): Boolean {
    if (this == null) return false
    return hierarchy.any { it.baseRouteOrNull() != null && it.baseRouteOrNull() in routes }
}

@Preview(showBackground = true)
@Composable
fun MainNavBarPreview() {
    AndroidAppProductHuntTheme {
        MainNavBarContent(
            isVisible = true,
            isSelected = { it.contains(Route.FEED) },
            onItemClick = {}
        )
    }
}
