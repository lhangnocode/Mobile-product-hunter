package android.app.producthunt.ui.navigation

import android.app.producthunt.ui.screens.LoginScreen
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
        // Đặt PRODUCT_DETAIL làm startDestination để chạy app là thấy ngay
        startDestination = Route.PRODUCT_DETAIL,
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
        composable(Route.LOGIN) { LoginScreen(navController = navController) }
        composable(Route.PRODUCT_DETAIL) { ProductDetailScreen(navController = navController) }
        
        composable(Route.HOME) { ProductDetailScreen(navController = navController) }
        composable(Route.SIGNUP) { }
        composable(Route.FORGOT_PASSWORD) { }
        composable(Route.VERIFY_OTP + "?email={email}", arguments = listOf(navArgument("email") {
            type = NavType.StringType;
            defaultValue = "";
        })) { }
        composable(
            Route.RESET_PASSWORD + "?email={email}&otp={otp}", arguments = listOf(
            navArgument("email") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("otp") {
                type = NavType.StringType
                defaultValue = ""
            }
        )) { }
    }
}

fun String.baseRoute(): String =
    this.substringBefore("?")
        .substringBefore("/{")

fun NavDestination.baseRouteOrNull(): String? =
    this.route?.baseRoute()
