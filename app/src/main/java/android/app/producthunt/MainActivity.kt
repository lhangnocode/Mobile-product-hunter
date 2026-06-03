package android.app.producthunt

import android.app.producthunt.data.local.LanguageMode
import android.app.producthunt.data.local.ThemeMode
import android.app.producthunt.data.remote.dto.UserResponse
import android.app.producthunt.ui.components.UserAvatar
import android.app.producthunt.ui.components.appbar.MainNavBar
import android.app.producthunt.ui.components.appbar.ProductHunterChildTopAppBar
import android.app.producthunt.ui.components.appbar.ProductHunterMainTopAppBar
import android.app.producthunt.ui.navigation.AppNavGraph
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.navigation.baseRoute
import android.app.producthunt.core.state.UiState
import android.app.producthunt.ui.i18n.AppStrings
import android.app.producthunt.ui.i18n.LocalAppStrings
import android.app.producthunt.ui.i18n.ProductHunterLocale
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
            val languageMode by themeViewModel.languageMode.collectAsState()
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDarkTheme
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            AndroidAppProductHuntTheme(darkTheme = darkTheme) {
                ProductHunterLocale(languageMode = languageMode) {
                val strings = LocalAppStrings.current
                val startupState by authViewModel.startupState.collectAsState()
                val currentUserState by authViewModel.currentUserState.collectAsState()
                val currentUser = (currentUserState as? UiState.Success)?.data

                LaunchedEffect(Unit) {
                    authViewModel.restoreSession()
                }

                if (startupState is UiState.Idle || startupState is UiState.Loading) {
                    StartupLoadingScreen()
                    return@ProductHunterLocale
                }

                val isAuthenticated = (startupState as? UiState.Success)?.data == true
                // Default to Search as per design vision
                val startDestination = if (isAuthenticated) Route.SEARCH else Route.LOGIN
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route?.baseRoute()
                val chrome = currentRoute.toChromeConfig(strings)
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
                                languageMode = languageMode,
                                currentUser = currentUser,
                                onThemeChange = { themeViewModel.setThemeMode(it) },
                                onLanguageChange = { themeViewModel.setLanguageMode(it) },
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
                                },
                                onAgentManagement = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Route.AGENT_MANAGEMENT)
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
                                    userName = currentUser?.fullName,
                                    userEmail = currentUser?.email,
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
                            authViewModel = authViewModel,
                            modifier = Modifier.padding(innerPadding),
                            startDestination = startDestination,
                        )
                    }
                }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(
    themeMode: ThemeMode,
    languageMode: LanguageMode,
    currentUser: UserResponse?,
    onThemeChange: (ThemeMode) -> Unit,
    onLanguageChange: (LanguageMode) -> Unit,
    onNewSearch: () -> Unit,
    onHistoryClick: () -> Unit,
    onManageAccount: () -> Unit,
    onAppInformation: () -> Unit,
    onAgentManagement: () -> Unit,
) {
    val strings = LocalAppStrings.current
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
            label = { Text(strings.newSearch, fontWeight = FontWeight.Medium) },
            selected = false,
            onClick = onNewSearch,
            icon = { CustomIcon(PHIcons.Add, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(strings.history, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(8.dp), color = Color.Gray)

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

        Text("Agent", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(8.dp), color = Color.Gray)

        NavigationDrawerItem(
            label = { Text(strings.aiAgent) },
            selected = false,
            onClick = onAgentManagement,
            icon = { CustomIcon(Icons.Default.AutoAwesome, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

        Text(strings.appearance, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(8.dp), color = Color.Gray)

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
            Text(strings.darkMode, modifier = Modifier.weight(1f))
            Switch(
                checked = themeMode == ThemeMode.DARK,
                onCheckedChange = { onThemeChange(if (it) ThemeMode.DARK else ThemeMode.LIGHT) }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            CustomIcon(Icons.Default.Language, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(strings.language, modifier = Modifier.weight(1f))
            SingleChoiceSegmentedButtonRow {
                LanguageMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = languageMode == mode,
                        onClick = { onLanguageChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = LanguageMode.entries.size,
                        ),
                    ) {
                        Text(if (mode == LanguageMode.ENGLISH) "EN" else "VI")
                    }
                }
            }
        }

        NavigationDrawerItem(
            label = { Text(strings.appInformation) },
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
                UserAvatar(
                    name = currentUser?.fullName,
                    email = currentUser?.email,
                    size = 40.dp,
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        currentUser?.fullName?.takeIf { it.isNotBlank() } ?: currentUser?.email ?: strings.productHunterUser,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        currentUser?.plan?.takeIf { it.isNotBlank() } ?: strings.freePlan,
                        style = MaterialTheme.typography.labelSmall,
                        color = PH_Primary,
                    )
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

private fun String?.toChromeConfig(strings: AppStrings): ChromeConfig =
    when (this) {
        Route.FEED -> ChromeConfig(
            topBar = TopBarType.Main,
            title = strings.feed,
            showBottomBar = true,
        )
        Route.SEARCH -> ChromeConfig(
            topBar = TopBarType.Main,
            title = strings.search,
            showBottomBar = true,
        )
        Route.WISHLIST -> ChromeConfig(
            topBar = TopBarType.Main,
            title = strings.wishlist,
            showBottomBar = true,
        )
        Route.ALERTS -> ChromeConfig(
            topBar = TopBarType.Main,
            title = strings.priceAlerts,
            showBottomBar = true,
        )
        Route.PROFILE -> ChromeConfig(
            topBar = TopBarType.Child,
            title = strings.profile,
        )
        Route.SEARCH_HISTORY -> ChromeConfig(
            topBar = TopBarType.Child,
            title = strings.history,
        )
        Route.PRODUCT_DETAIL -> ChromeConfig(
            topBar = TopBarType.Child,
            title = strings.details,
        )
        Route.APP_INFORMATION -> ChromeConfig(
            topBar = TopBarType.Child,
            title = strings.appInformation,
        )
        Route.AGENT_MANAGEMENT -> ChromeConfig(
            topBar = TopBarType.Child,
            title = strings.aiAgent,
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
