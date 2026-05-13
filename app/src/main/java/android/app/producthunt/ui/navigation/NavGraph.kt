package android.app.producthunt.ui.navigation

import android.app.producthunt.ui.screens.AuthGateScreen
import android.app.producthunt.ui.screens.LoginScreen
import android.app.producthunt.ui.screens.SignupScreen
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
import androidx.navigation.NavType
import androidx.navigation.navArgument

const val ANIM_DURATION = 300

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Route.AUTH_GATE,
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
        composable(Route.AUTH_GATE) {
            AuthGateScreen(navController = navController)
        }

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

        composable(
            route = "${Route.PRODUCT_DETAIL}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductDetailScreen(navController = navController, productId = productId)
        }

        composable(Route.SIGNUP) {
            SignupScreen(navController = navController)
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
