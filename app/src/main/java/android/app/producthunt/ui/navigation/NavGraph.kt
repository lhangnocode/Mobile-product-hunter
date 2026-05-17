package android.app.producthunt.ui.navigation

import android.app.producthunt.ui.screens.*
import android.app.producthunt.ui.screens.main.*
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument

const val ANIM_DURATION = 300

val TopLevelRoutes = listOf(
    Route.HOME,
    Route.TRENDING,
    Route.WISHLIST,
    Route.ALERTS,
)

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
            val slideDirection = forwardSlideDirection(
                fromRoute = initialState.destination.route,
                toRoute = targetState.destination.route,
            )
            slideIntoContainer(
                slideDirection,
                animationSpec = tween(ANIM_DURATION)
            )
        },
        exitTransition = {
            val slideDirection = forwardSlideDirection(
                fromRoute = initialState.destination.route,
                toRoute = targetState.destination.route,
            )
            slideOutOfContainer(
                slideDirection,
                animationSpec = tween(ANIM_DURATION)
            )
        },
        popEnterTransition = {
            val slideDirection = popSlideDirection(
                fromRoute = initialState.destination.route,
                toRoute = targetState.destination.route,
            )
            slideIntoContainer(
                slideDirection,
                animationSpec = tween(ANIM_DURATION)
            )
        },
        popExitTransition = {
            val slideDirection = popSlideDirection(
                fromRoute = initialState.destination.route,
                toRoute = targetState.destination.route,
            )
            slideOutOfContainer(
                slideDirection,
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

        composable(
            route = "${Route.SEARCH}?q={q}",
            arguments = listOf(navArgument("q") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("q")
            SearchScreen(navController = navController, initialQuery = query)
        }

        composable(Route.TRENDING) {
            TrendingScreen(navController = navController)
        }

        composable(Route.WISHLIST) {
            WishlistScreen(navController = navController)
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
            route = "${Route.PRODUCT_DETAIL}/{productId}?imageUrl={imageUrl}",
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType },
                navArgument("imageUrl") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            val imageUrl = backStackEntry.arguments?.getString("imageUrl")
            ProductDetailScreen(navController = navController, productId = productId, imageUrl = imageUrl)
        }

        composable(Route.SIGNUP) {
            SignupScreen(navController = navController)
        }

        composable(Route.FORGOT_PASSWORD) {
            ForgotPasswordScreen(navController = navController)
        }

        composable("${Route.VERIFY_OTP}/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyOtpScreen(navController = navController, email = email)
        }

        composable("${Route.RESET_PASSWORD}/{email}/{otp}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val otp = backStackEntry.arguments?.getString("otp") ?: ""
            ResetPasswordScreen(navController = navController, email = email, otp = otp)
        }
    }
}

fun String.baseRoute(): String =
    this.substringBefore("?")
        .substringBefore("/{")

fun NavDestination.baseRouteOrNull(): String? =
    this.route?.baseRoute()

private fun forwardSlideDirection(
    fromRoute: String?,
    toRoute: String?,
): AnimatedContentTransitionScope.SlideDirection {
    val fromIndex = topLevelRouteIndex(fromRoute)
    val toIndex = topLevelRouteIndex(toRoute)
    return when {
        fromIndex != null && toIndex != null && toIndex < fromIndex ->
            AnimatedContentTransitionScope.SlideDirection.Right
        else -> AnimatedContentTransitionScope.SlideDirection.Left
    }
}

private fun popSlideDirection(
    fromRoute: String?,
    toRoute: String?,
): AnimatedContentTransitionScope.SlideDirection {
    val fromIndex = topLevelRouteIndex(fromRoute)
    val toIndex = topLevelRouteIndex(toRoute)
    return when {
        fromIndex != null && toIndex != null && toIndex > fromIndex ->
            AnimatedContentTransitionScope.SlideDirection.Left
        else -> AnimatedContentTransitionScope.SlideDirection.Right
    }
}

private fun topLevelRouteIndex(route: String?): Int? {
    val baseRoute = route?.baseRoute() ?: return null
    return TopLevelRoutes.indexOf(baseRoute).takeIf { it >= 0 }
}
