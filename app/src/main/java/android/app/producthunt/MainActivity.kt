package android.app.producthunt

import android.app.producthunt.data.local.ThemeMode
import android.app.producthunt.ui.components.appbar.MainNavBar
import android.app.producthunt.ui.components.appbar.ProductHunterChildTopAppBar
import android.app.producthunt.ui.components.appbar.ProductHunterMainTopAppBar
import android.app.producthunt.ui.navigation.AppNavGraph
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.navigation.baseRoute
import android.app.producthunt.domain.UiState
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import android.app.producthunt.ui.viewmodel.AuthViewModel
import android.app.producthunt.ui.viewmodel.ThemeViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current
            val themeMode by themeViewModel.themeMode.collectAsState()
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDarkTheme
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            AndroidAppProductHuntTheme(darkTheme = darkTheme) {
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
                val childScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (chrome.topBar == TopBarType.Child) {
                                Modifier.nestedScroll(childScrollBehavior.nestedScrollConnection)
                            } else {
                                Modifier
                            }
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            })
                        },
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        when (chrome.topBar) {
                            TopBarType.Main -> ProductHunterMainTopAppBar(
                                onSearchClick = { navController.navigate(Route.SEARCH) },
                                onProfileClick = { navController.navigate(Route.PROFILE) },
                            )
                            TopBarType.Child -> ProductHunterChildTopAppBar(
                                title = chrome.title,
                                subtitle = chrome.subtitle,
                                onBack = { navController.popBackStack() },
                                showSearchAction = chrome.showSearchAction,
                                showCalendarAction = chrome.showCalendarAction,
                                onSearchClick = { navController.navigate(Route.SEARCH) },
                                scrollBehavior = childScrollBehavior,
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
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
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
    Child,
}

private enum class FabType {
    None,
    HomeAlert,
}

private data class ChromeConfig(
    val topBar: TopBarType = TopBarType.None,
    val title: String = "",
    val subtitle: String? = null,
    val showBottomBar: Boolean = false,
    val fab: FabType = FabType.None,
    val showSearchAction: Boolean = false,
    val showCalendarAction: Boolean = false,
)

private fun String?.toChromeConfig(): ChromeConfig =
    when (this) {
        Route.HOME -> ChromeConfig(
            topBar = TopBarType.Main,
            title = "Trang chủ",
            showBottomBar = true,
            fab = FabType.HomeAlert,
        )
        Route.TRENDING -> ChromeConfig(
            topBar = TopBarType.Main,
            title = "Xu hướng",
            showBottomBar = true,
        )
        Route.WISHLIST -> ChromeConfig(
            topBar = TopBarType.Main,
            title = "Yêu thích",
            showBottomBar = true,
        )
        Route.ALERTS -> ChromeConfig(
            topBar = TopBarType.Main,
            title = "Thông báo giá",
            showBottomBar = true,
        )
        Route.PROFILE -> ChromeConfig(
            topBar = TopBarType.Child,
            title = "Cá nhân",
            subtitle = "Tài khoản và tùy chọn",
        )
        Route.SEARCH -> ChromeConfig(
            topBar = TopBarType.Child,
            title = "Tìm kiếm",
            subtitle = "So sánh giá tốt nhất",
        )
        Route.PRODUCT_DETAIL -> ChromeConfig(
            topBar = TopBarType.Child,
            title = "Chi tiết sản phẩm",
            subtitle = "Lịch sử giá và sàn bán",
            showSearchAction = true,
        )
        Route.SIGNUP -> ChromeConfig(
            topBar = TopBarType.Child,
            title = "Tạo tài khoản",
            subtitle = "Bắt đầu theo dõi giá",
        )
        Route.FORGOT_PASSWORD -> ChromeConfig(
            topBar = TopBarType.Child,
            title = "Quên mật khẩu",
            subtitle = "Nhận mã xác thực qua email",
        )
        Route.VERIFY_OTP -> ChromeConfig(
            topBar = TopBarType.Child,
            title = "Xác thực OTP",
            subtitle = "Nhập mã 6 chữ số",
        )
        Route.RESET_PASSWORD -> ChromeConfig(
            topBar = TopBarType.Child,
            title = "Đặt lại mật khẩu",
            subtitle = "Tạo mật khẩu mới",
        )
        else -> ChromeConfig()
    }
