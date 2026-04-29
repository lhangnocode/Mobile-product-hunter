package android.app.producthunt.ui.navigation

import android.app.producthunt.ui.screens.main.ProfileScreen
import android.app.producthunt.ui.screens.ProductDetailScreen
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

const val ANIM_DURATION = 300

@Composable
fun AppNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        // Chạy thẳng vào màn hình Profile để kiểm tra giao diện bạn yêu cầu
        startDestination = Route.PROFILE,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(ANIM_DURATION)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(ANIM_DURATION)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(ANIM_DURATION)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(ANIM_DURATION)
            )
        }
    ) {
        composable(Route.PROFILE) {
            ProfileScreen(navController = navController)
        }

        composable(Route.PRODUCT_DETAIL) { 
            ProductDetailScreen(navController = navController) 
        }

        // Tạm thời để trống các route khác để tránh lỗi build
        composable(Route.LOGIN) { }
        composable(Route.SIGNUP) { }
        composable(Route.HOME) { }
        composable(Route.TRENDING) { }
        composable(Route.WISHLIST) { }
        composable(Route.ALERTS) { }
    }
}

fun String.baseRoute(): String =
    this.substringBefore("?")
        .substringBefore("/{")

fun NavDestination.baseRouteOrNull(): String? =
    this.route?.baseRoute()
