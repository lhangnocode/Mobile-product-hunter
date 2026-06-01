package android.app.producthunt.ui.navigation

import android.app.producthunt.ui.screens.*
import android.app.producthunt.ui.screens.main.*
import android.app.producthunt.ui.viewmodel.AuthViewModel
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

const val ANIM_DURATION = 300

val TopLevelRoutes = listOf(
    Route.FEED,
    Route.SEARCH,
    Route.WISHLIST,
    Route.ALERTS,
)

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
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
            AuthGateScreen(navController = navController, viewModel = authViewModel)
        }

        composable(Route.LOGIN) {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }

        composable(Route.FEED) {
            // Re-using HomeScreen as FeedScreen for now, will refactor later
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
            // SearchScreen will be the Chat-based interface
            SearchScreen(navController = navController, initialQuery = query, authViewModel = authViewModel)
        }

        composable(Route.WISHLIST) {
            WishlistScreen(navController = navController)
        }

        composable(Route.ALERTS) {
            PriceAlertsScreen(
                onNavigateToHunt = { navController.navigate(Route.FEED) },
                onNavigateToDeals = { navController.navigate(Route.FEED) }, // Adjusting to Feed
                onNavigateToSaved = { navController.navigate(Route.WISHLIST) },
                onProductSelected = { productId, imageUrl ->
                    val encodedImage = imageUrl?.let {
                        java.net.URLEncoder.encode(it, java.nio.charset.StandardCharsets.UTF_8.toString())
                    }
                    val route = buildString {
                        append("${Route.PRODUCT_DETAIL}/$productId")
                        if (!encodedImage.isNullOrBlank()) append("?imageUrl=$encodedImage")
                    }
                    navController.navigate(route)
                },
            )
        }

        composable(Route.PROFILE) {
            ProfileScreen(navController = navController, viewModel = authViewModel)
        }

        composable(Route.SEARCH_HISTORY) {
            // Placeholder for Search History Screen
            SearchHistoryScreen(navController = navController)
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
            SignupScreen(navController = navController, viewModel = authViewModel)
        }

        composable(Route.FORGOT_PASSWORD) {
            ForgotPasswordScreen(viewModel = authViewModel)
        }

        composable(
            route = "${Route.RESET_PASSWORD}?token={token}",
            arguments = listOf(
                navArgument("token") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://producthunt.example.com/reset-password?token={token}"
                }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            ResetPasswordScreen(navController = navController, token = token, viewModel = authViewModel)
        }

        composable(Route.APP_INFORMATION) {
            AppInformationScreen(modifier = Modifier.fillMaxSize())
        }

        composable(Route.AGENT_MANAGEMENT) {
            AgentManagementScreen()
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
