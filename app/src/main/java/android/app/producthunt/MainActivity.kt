package android.app.producthunt

import android.app.producthunt.ui.components.appbar.BackTopBar
import android.app.producthunt.ui.components.appbar.MainNavBar
import android.app.producthunt.ui.navigation.AppNavGraph
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.navigation.baseRoute
import android.app.producthunt.ui.viewmodel.AuthViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dagger.hilt.android.AndroidEntryPoint
import android.app.producthunt.domain.UiState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current

            AndroidAppProductHuntTheme {
                val startupState by authViewModel.startupState.collectAsState()

                LaunchedEffect(Unit) {
                    authViewModel.restoreSession()
                }

                if (startupState is UiState.Idle || startupState is UiState.Loading) {
                    StartupLoadingScreen()
                    return@AndroidAppProductHuntTheme
                }

                val isAuthenticated = (startupState as? UiState.Success)?.data == true
                val startDestination = if (isAuthenticated) Route.HOME else Route.LOGIN
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route?.baseRoute()
                val chrome = currentRoute.toChromeConfig()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            })
                        },
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        when (chrome.topBar) {
                            TopBarType.Main -> ProductHunterTopBar()
                            TopBarType.Back -> BackTopBar(
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .padding(vertical = 8.dp),
                                onBack = { navController.popBackStack() },
                            )
                            TopBarType.None -> Unit
                        }
                    },
                    bottomBar = {
                        if (chrome.showBottomBar) {
                            MainNavBar(navController = navController)
                        }
                    },
                    floatingActionButton = {
                        when (chrome.fab) {
                            FabType.HomeAlert -> FloatingActionButton(
                                onClick = { navController.navigate(Route.ALERTS) },
                                containerColor = Color(0xFFFF8A50),
                                contentColor = Color.Black,
                                shape = CircleShape,
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = "Alerts")
                            }
                            else -> Unit
                        }
                    },
                ) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        startDestination = startDestination,
                    )
                }
            }
        }
    }
}

@Composable
private fun StartupLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

private enum class TopBarType {
    None,
    Main,
    Back,
}

private enum class FabType {
    None,
    HomeAlert,
}

private data class ChromeConfig(
    val topBar: TopBarType = TopBarType.None,
    val showBottomBar: Boolean = false,
    val fab: FabType = FabType.None,
)

private fun String?.toChromeConfig(): ChromeConfig =
    when (this) {
        Route.HOME -> ChromeConfig(
            topBar = TopBarType.Main,
            showBottomBar = true,
            fab = FabType.HomeAlert,
        )
        Route.TRENDING,
        Route.WISHLIST,
        Route.ALERTS,
        Route.PROFILE -> ChromeConfig(
            topBar = TopBarType.Main,
            showBottomBar = true,
        )
        Route.PRODUCT_DETAIL -> ChromeConfig(
            topBar = TopBarType.None, // Changed to None for Cinematic effect
            fab = FabType.None,
        )
        Route.SIGNUP,
        Route.FORGOT_PASSWORD,
        Route.VERIFY_OTP,
        Route.RESET_PASSWORD -> ChromeConfig(topBar = TopBarType.None)
        else -> ChromeConfig()
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductHunterTopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.product_logo),
                    contentDescription = "ProductHunter",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "ProductHunter",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        },
        actions = {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(36.dp)
                    .clip(CircleShape),
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}
