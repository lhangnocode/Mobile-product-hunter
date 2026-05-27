package android.app.producthunt

import android.app.producthunt.data.local.ThemeMode
import android.app.producthunt.ui.components.appbar.MainNavBar
import android.app.producthunt.ui.components.appbar.ProductHunterChildTopAppBar
import android.app.producthunt.ui.components.appbar.ProductHunterMainTopAppBar
import android.app.producthunt.ui.navigation.AppNavGraph
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.navigation.baseRoute
import android.app.producthunt.domain.UiState
import android.app.producthunt.ui.navigation.navigateToTopLevelDestination
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import android.app.producthunt.ui.theme.PHIcons
import android.app.producthunt.ui.theme.PH_Primary
import android.app.producthunt.ui.viewmodel.AuthViewModel
import android.app.producthunt.ui.viewmodel.ThemeViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
                // Default to Search as per design vision
                val startDestination = if (isAuthenticated) Route.SEARCH else Route.LOGIN
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route?.baseRoute()
                val chrome = currentRoute.toChromeConfig()
                val childScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(320.dp),
                            drawerContainerColor = MaterialTheme.colorScheme.surface,
                            drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                        ) {
                            AppDrawerContent(
                                themeMode = themeMode,
                                onThemeChange = { themeViewModel.setThemeMode(it) },
                                onNewSearch = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Route.SEARCH)
                                },
                                onHistoryClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Route.SEARCH_HISTORY)
                                },
                                onManageAccount = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Route.PROFILE)
                                },
                                onAppInformation = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Route.APP_INFORMATION)
                                }
                            )
                        }
                    },
                    gesturesEnabled = chrome.showBottomBar
                ) {
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
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    onProfileClick = { navController.navigate(Route.PROFILE) },
                                )
                                TopBarType.Child -> ProductHunterChildTopAppBar(
                                    title = chrome.title,
                                    subtitle = chrome.subtitle,
                                    onBack = { navController.popBackStack() },
                                    showSearchAction = chrome.showSearchAction,
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
                        }
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
}

@Composable
fun AppDrawerContent(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onNewSearch: () -> Unit,
    onHistoryClick: () -> Unit,
    onManageAccount: () -> Unit,
    onAppInformation: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Product Hunter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = PH_Primary
            )
        }

        NavigationDrawerItem(
            label = { Text("New Search", fontWeight = FontWeight.Medium) },
            selected = false,
            onClick = onNewSearch,
            icon = { CustomIcon(PHIcons.Add, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text("History", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(8.dp), color = Color.Gray)

        val recentHistory = listOf("Compare Samsung Tab S10 vs iPad Mini", "Best laptop for AI under 20M", "iPhone 15 vs Galaxy S24")
        recentHistory.forEach { history ->
            NavigationDrawerItem(
                label = { Text(history, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                selected = false,
                onClick = onHistoryClick,
                icon = { CustomIcon(PHIcons.History, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

        Text("Appearance", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(8.dp), color = Color.Gray)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onThemeChange(if (themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK) }
                .padding(12.dp)
        ) {
            CustomIcon(if (themeMode == ThemeMode.DARK) Icons.Default.DarkMode else Icons.Default.LightMode, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Dark Mode", modifier = Modifier.weight(1f))
            Switch(
                checked = themeMode == ThemeMode.DARK,
                onCheckedChange = { onThemeChange(if (it) ThemeMode.DARK else ThemeMode.LIGHT) }
            )
        }

        NavigationDrawerItem(
            label = { Text("App Information") },
            selected = false,
            onClick = { onAppInformation() },
            icon = { CustomIcon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onManageAccount),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Nguyễn Văn Thắng", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Free Plan", style = MaterialTheme.typography.labelSmall, color = PH_Primary)
                }
                CustomIcon(Icons.Default.Settings, contentDescription = null, size = 18.dp)
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Version 1.0.0 Product Hunter",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp)
        )
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

private data class ChromeConfig(
    val topBar: TopBarType = TopBarType.None,
    val title: String = "",
    val subtitle: String? = null,
    val showBottomBar: Boolean = false,
    val showSearchAction: Boolean = false,
)

private fun String?.toChromeConfig(): ChromeConfig =
    when (this) {
        Route.FEED -> ChromeConfig(
            topBar = TopBarType.Main,
            title = "Feed",
            showBottomBar = true,
        )
        Route.SEARCH -> ChromeConfig(
            topBar = TopBarType.Main,
            title = "Search",
            showBottomBar = true,
        )
        Route.WISHLIST -> ChromeConfig(
            topBar = TopBarType.Main,
            title = "Wishlist",
            showBottomBar = true,
        )
        Route.PROFILE -> ChromeConfig(
            topBar = TopBarType.Child,
            title = "Profile",
        )
        Route.SEARCH_HISTORY -> ChromeConfig(
            topBar = TopBarType.Child,
            title = "History",
        )
        Route.PRODUCT_DETAIL -> ChromeConfig(
            topBar = TopBarType.Child,
            title = "Details",
        )
        Route.APP_INFORMATION -> ChromeConfig(
            topBar = TopBarType.Child,
            title = "App Information",
        )
        else -> ChromeConfig()
    }

@Composable
private fun CustomIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp? = null,
    tint: Color = LocalContentColor.current
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = if (size != null) modifier.size(size) else modifier,
        tint = tint
    )
}
