package android.app.producthunt.ui.navigation

import android.app.producthunt.ui.screens.LoginScreen
import android.app.producthunt.ui.screens.main.ProductDetailScreen
import android.app.producthunt.ui.screens.main.PriceAlertsScreen
import android.app.producthunt.ui.screens.main.HomeScreen
import android.app.producthunt.ui.screens.main.ProfileScreen
import android.app.producthunt.ui.screens.main.TrendingScreen
import android.app.producthunt.ui.screens.main.WishlistScreen
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

const val ANIM_DURATION = 300

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Route.LOGIN,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
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
        composable(Route.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(Route.HOME) {
            HomeScreen(navController = navController)
        }

        composable(Route.TRENDING) {
            TrendingScreen()
        }

        composable(Route.WISHLIST) {
            WishlistScreen()
        }

        composable(Route.ALERTS) {
            PriceAlertsScreen(
                onNavigateToHunt = { navController.navigate(Route.HOME) },
                onNavigateToDeals = { navController.navigate(Route.TRENDING) },
                onNavigateToSaved = { navController.navigate(Route.WISHLIST) },
            )
        }

        composable(Route.PROFILE) {
            ProfileScreen(navController = navController)
        }

        composable(Route.PRODUCT_DETAIL) {
            ProductDetailScreen(navController = navController)
        }

        composable(Route.SIGNUP) {
            Text("Sign up screen")
        }

        composable(Route.FORGOT_PASSWORD) {
            Text("Forgot password screen")
        }

        composable(Route.VERIFY_OTP) {
            Text("Verify OTP screen")
        }

        composable(Route.RESET_PASSWORD) {
            Text("Reset password screen")
        }
    }
}

fun String.baseRoute(): String =
    this.substringBefore("?")
        .substringBefore("/{")

fun NavDestination.baseRouteOrNull(): String? =
    this.route?.baseRoute()
