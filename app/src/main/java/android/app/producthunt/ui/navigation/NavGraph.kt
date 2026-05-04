package android.app.producthunt.ui.navigation

import android.app.producthunt.ui.screens.LoginScreen
import android.app.producthunt.ui.screens.SignupScreen
import android.app.producthunt.ui.screens.ProductDetailScreen
import android.app.producthunt.ui.screens.main.ProfileScreen
import android.app.producthunt.ui.screens.main.TrendingScreen
import android.app.producthunt.ui.screens.main.WishlistScreen
import android.app.producthunt.ui.screens.alerts.PriceAlertsScreen
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

const val ANIM_DURATION = 300

@Composable
fun AppNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        // Đặt LOGIN làm màn hình khởi đầu
        startDestination = Route.LOGIN,
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
        // Auth Flow
        composable(Route.LOGIN) { 
            LoginScreen(navController = navController) 
        }
        composable(Route.SIGNUP) { 
            SignupScreen(navController = navController) 
        }

        // Main Flow (Các màn hình này đều đã có Bottom Navigation bên trong)
        composable(Route.HOME) { 
            PriceAlertsScreen(navController = navController) 
        }
        composable(Route.ALERTS) { 
            PriceAlertsScreen(navController = navController) 
        }
        composable(Route.TRENDING) { 
            TrendingScreen(navController = navController) 
        }
        composable(Route.WISHLIST) { 
            WishlistScreen(navController = navController) 
        }
        composable(Route.PROFILE) {
            ProfileScreen(navController = navController)
        }

        // Detail Flow
        composable(Route.PRODUCT_DETAIL) { 
            ProductDetailScreen(navController = navController) 
        }
    }
}

fun String.baseRoute(): String =
    this.substringBefore("?")
        .substringBefore("/{")

fun NavDestination.baseRouteOrNull(): String? =
    this.route?.baseRoute()
