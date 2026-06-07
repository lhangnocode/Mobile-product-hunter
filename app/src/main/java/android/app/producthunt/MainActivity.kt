package android.app.producthunt

import android.Manifest
import android.app.producthunt.data.local.LanguageMode
import android.app.producthunt.data.local.ThemeMode
import android.app.producthunt.data.local.db.entity.AgentConversationEntity
import android.app.producthunt.data.remote.dto.UserResponse
import android.app.producthunt.ui.components.UserAvatar
import android.app.producthunt.ui.components.appbar.MainNavBar
import android.app.producthunt.ui.components.appbar.ProductHunterChildTopAppBar
import android.app.producthunt.ui.components.appbar.ProductHunterMainTopAppBar
import android.app.producthunt.ui.navigation.AppNavGraph
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.navigation.baseRoute
import android.app.producthunt.core.state.UiState
import android.app.producthunt.core.notification.PriceAlertNotificationPayload
import android.app.producthunt.ui.i18n.AppStrings
import android.app.producthunt.ui.i18n.LocalAppStrings
import android.app.producthunt.ui.i18n.ProductHunterLocale
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import android.app.producthunt.ui.theme.PHIcons
import android.app.producthunt.ui.theme.PH_Primary
import android.app.producthunt.ui.viewmodel.AgentConversationHistoryViewModel
import android.app.producthunt.ui.viewmodel.AuthViewModel
import android.app.producthunt.ui.viewmodel.ThemeViewModel
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val pendingNotificationIntent = MutableStateFlow<Intent?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingNotificationIntent.value = intent
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        setContent {
            val navController = rememberNavController()
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current
            val themeMode by themeViewModel.themeMode.collectAsState()
            val languageMode by themeViewModel.languageMode.collectAsState()
            val priceAlertNotificationsEnabled by themeViewModel.priceAlertNotificationsEnabled.collectAsState()
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDarkTheme
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            AndroidAppProductHuntTheme(darkTheme = darkTheme) {
                ProductHunterLocale(languageMode = languageMode) locale@{
                val startupState by authViewModel.startupState.collectAsState()
                val currentUserState by authViewModel.currentUserState.collectAsState()
                val currentUser = (currentUserState as? UiState.Success)?.data

                LaunchedEffect(Unit) {
                    authViewModel.restoreSession()
                }

                if (startupState is UiState.Idle || startupState is UiState.Loading) {
                    StartupLoadingScreen()
                    return@locale
                }

                val isAuthenticated = (startupState as? UiState.Success)?.data == true
                // Default to Search as per design vision
                val startDestination = if (isAuthenticated) Route.SEARCH else Route.LOGIN
                val notificationIntent by pendingNotificationIntent.collectAsState()
                val agentHistoryViewModel: AgentConversationHistoryViewModel = hiltViewModel()
                val agentConversations by agentHistoryViewModel.conversations.collectAsState()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route?.baseRoute()
                val currentConversationId = backStackEntry?.arguments
                    ?.getString("conversationId")
                    ?.takeIf { it != "new" }
                val strings = LocalAppStrings.current
                val chrome = currentRoute.toChromeConfig(strings)
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val currentPostNotificationPermission =
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) == PackageManager.PERMISSION_GRANTED
                var hasPostNotificationPermission by remember {
                    mutableStateOf(currentPostNotificationPermission)
                }
                var hasRequestedPostNotificationPermission by remember {
                    mutableStateOf(false)
                }
                LaunchedEffect(currentPostNotificationPermission) {
                    hasPostNotificationPermission = currentPostNotificationPermission
                }
                val effectivePriceAlertNotificationsEnabled =
                    priceAlertNotificationsEnabled && hasPostNotificationPermission
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { isGranted ->
                    hasPostNotificationPermission = isGranted
                    if (isGranted) {
                        themeViewModel.setPriceAlertNotificationsEnabled(true)
                    }
                }
                LaunchedEffect(
                    isAuthenticated,
                    priceAlertNotificationsEnabled,
                    hasPostNotificationPermission,
                ) {
                    if (
                        isAuthenticated &&
                            priceAlertNotificationsEnabled &&
                            !hasPostNotificationPermission &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !hasRequestedPostNotificationPermission
                    ) {
                        hasRequestedPostNotificationPermission = true
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                val onPriceAlertNotificationsChange: (Boolean) -> Unit = { enabled ->
                    if (!enabled) {
                        themeViewModel.setPriceAlertNotificationsEnabled(false)
                    } else if (!hasPostNotificationPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        themeViewModel.setPriceAlertNotificationsEnabled(true)
                    }
                }
                LaunchedEffect(isAuthenticated, notificationIntent) {
                    if (!isAuthenticated) return@LaunchedEffect

                    val payload = PriceAlertNotificationPayload.fromIntent(notificationIntent)
                    if (payload != null) {
                        navController.navigate(payload.toProductDetailRoute()) {
                            launchSingleTop = true
                        }
                        pendingNotificationIntent.value = null
                    }
                }
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView)
                        .isAppearanceLightStatusBars = chrome.topBar == TopBarType.None && !darkTheme
                }

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
                                priceAlertNotificationsEnabled = effectivePriceAlertNotificationsEnabled,
                                currentUser = currentUser,
                                agentConversations = agentConversations,
                                onThemeChange = { themeViewModel.setThemeMode(it) },
                                onLanguageChange = { themeViewModel.setLanguageMode(it) },
                                onNotificationsEnabledChange = onPriceAlertNotificationsChange,
                                onNewSearch = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Route.SEARCH)
                                },
                                onConversationClick = { conversationId ->
                                    scope.launch { drawerState.close() }
                                    navController.navigate("${Route.SEARCH}?conversationId=$conversationId")
                                },
                                onDeleteConversation = { conversationId ->
                                    agentHistoryViewModel.deleteConversation(conversationId)
                                    if (currentRoute == Route.SEARCH && currentConversationId == conversationId) {
                                        navController.navigate(Route.SEARCH) {
                                            launchSingleTop = true
                                        }
                                    }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationIntent.value = intent
    }
}

@Composable
fun AppDrawerContent(
    themeMode: ThemeMode,
    languageMode: LanguageMode,
    priceAlertNotificationsEnabled: Boolean,
    currentUser: UserResponse?,
    agentConversations: List<AgentConversationEntity>,
    onThemeChange: (ThemeMode) -> Unit,
    onLanguageChange: (LanguageMode) -> Unit,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onNewSearch: () -> Unit,
    onConversationClick: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onManageAccount: () -> Unit,
    onAppInformation: () -> Unit,
    onAgentManagement: () -> Unit,
) {
    val strings = LocalAppStrings.current
    var pendingDeleteConversation by remember { mutableStateOf<AgentConversationEntity?>(null) }

    pendingDeleteConversation?.let { conversation ->
        AlertDialog(
            onDismissRequest = { pendingDeleteConversation = null },
            title = { Text("Delete conversation?") },
            text = {
                Text(
                    conversation.title
                        ?: "This agent conversation and its messages will be removed.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteConversation = null
                        onDeleteConversation(conversation.id)
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteConversation = null }) {
                    Text("Cancel")
                }
            },
        )
    }

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

        if (agentConversations.isEmpty()) {
            EmptyHistoryPlaceholder(
                title = strings.historyEmptyTitle,
                description = strings.historyEmptyDescription,
            )
        }

        agentConversations.take(6).forEach { conversation ->
            AgentHistoryDrawerItem(
                conversation = conversation,
                onClick = { onConversationClick(conversation.id) },
                onDeleteClick = { pendingDeleteConversation = conversation },
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

        Text("Agent", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(8.dp), color = Color.Gray)

        NavigationDrawerItem(
            label = { Text("AI Agent") },
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
                .clickable { onNotificationsEnabledChange(!priceAlertNotificationsEnabled) }
                .padding(12.dp)
        ) {
            CustomIcon(
                if (priceAlertNotificationsEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                contentDescription = null,
            )
            Spacer(Modifier.width(12.dp))
            Text(strings.alerts, modifier = Modifier.weight(1f))
            Switch(
                checked = priceAlertNotificationsEnabled,
                onCheckedChange = onNotificationsEnabledChange,
            )
        }

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
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomIcon(Icons.Default.Language, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.language,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        modifier = Modifier.weight(1f),
                        selected = languageMode == LanguageMode.VIETNAMESE,
                        onClick = { onLanguageChange(LanguageMode.VIETNAMESE) },
                        label = { Text(strings.vietnameseShort, maxLines = 1) },
                    )
                    FilterChip(
                        modifier = Modifier.weight(1f),
                        selected = languageMode == LanguageMode.ENGLISH,
                        onClick = { onLanguageChange(LanguageMode.ENGLISH) },
                        label = { Text(strings.englishShort, maxLines = 1) },
                    )
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
                        currentUser?.fullName?.takeIf { it.isNotBlank() } ?: currentUser?.email ?: "Product Hunter User",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
private fun EmptyHistoryPlaceholder(
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CustomIcon(
            PHIcons.History,
            contentDescription = null,
            tint = Color.Gray,
            size = 18.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
            )
        }
    }
}

@Composable
private fun AgentHistoryDrawerItem(
    conversation: AgentConversationEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(start = 4.dp, end = 2.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomIcon(PHIcons.History, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                text = conversation.title ?: "New agent chat",
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete conversation",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                modifier = Modifier.size(18.dp),
            )
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
        Route.FORGOT_PASSWORD -> ChromeConfig(
            topBar = TopBarType.Child,
            title = strings.forgotPassword,
        )
        else -> ChromeConfig()
    }

private fun PriceAlertNotificationPayload.toProductDetailRoute(): String =
    buildString {
        append("${Route.PRODUCT_DETAIL}/$productId")
        val params = buildList {
            if (!imageUrl.isNullOrBlank()) add("imageUrl=${Uri.encode(imageUrl)}")
            if (!productName.isNullOrBlank()) add("productName=${Uri.encode(productName)}")
            if (!platformProductId.isNullOrBlank()) add("platformProductId=$platformProductId")
        }
        if (params.isNotEmpty()) {
            append("?")
            append(params.joinToString("&"))
        }
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
