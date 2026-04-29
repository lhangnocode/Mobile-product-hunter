package android.app.producthunt.ui.screens.main

import android.app.producthunt.R
import android.app.producthunt.ui.theme.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// Đã xóa định nghĩa MainTab ở đây vì nó đã có trong MainScreen.kt cùng package

@Composable
fun ProfileScreen(navController: NavController) {
    // State quản lý tab, mặc định chọn Profile
    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Profile) }
    
    val tabs = listOf(
        MainTab.Home,
        MainTab.Trending,
        MainTab.Wishlist,
        MainTab.Alerts,
        MainTab.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = PH_Surface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PH_Primary,
                            selectedTextColor = PH_Primary,
                            unselectedIconColor = PH_OnSurface.copy(alpha = 0.4f),
                            unselectedTextColor = PH_OnSurface.copy(alpha = 0.4f),
                            indicatorColor = PH_Primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        },
        containerColor = PH_Background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                MainTab.Home -> PriceAlertsScreen()
                MainTab.Trending -> TrendingScreen()
                MainTab.Wishlist -> WishlistScreen()
                MainTab.Alerts -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Alerts Screen Content")
                    }
                }
                MainTab.Profile -> {
                    ProfileContent(navController)
                }
            }
        }
    }
}

@Composable
fun ProfileContent(navController: NavController) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileTopBar()
        ProfileHeaderCard()

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsGroup(title = "NOTIFICATIONS") {
                SettingsToggleItem(
                    icon = Icons.Default.NotificationsActive,
                    iconColor = Color(0xFFFFCCBC),
                    contentColor = Color(0xFFFF5722),
                    title = "Price alerts",
                    description = "Notify when saved items drop in price",
                    initialValue = true
                )
                SettingsToggleItem(
                    icon = Icons.Default.LocalOffer,
                    iconColor = Color(0xFFE8EAF6),
                    contentColor = Color(0xFF3F51B5),
                    title = "Exclusive deals",
                    description = "Daily curated offers for Elite members",
                    initialValue = false
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsGroup(title = "PREFERENCES") {
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    iconColor = Color(0xFFECEFF1),
                    contentColor = Color(0xFF455A64),
                    title = "Dark mode",
                    description = "Enable high contrast dark appearance",
                    initialValue = false
                )
                SettingsNavigationItem(
                    icon = Icons.Default.Language,
                    iconColor = Color(0xFFECEFF1),
                    contentColor = Color(0xFF455A64),
                    title = "Region & Language",
                    description = "United States (English)"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsGroup(title = "SUPPORT") {
                SettingsNavigationItem(
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFFF3E5F5),
                    contentColor = Color(0xFF9C27B0),
                    title = "About ProductHunter"
                )
                SettingsNavigationItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    iconColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFE53935),
                    title = "Sign Out",
                    textColor = Color(0xFFE53935)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Version 2.4.1 (Build 8902)\n© 2024 ProductHunter Inc.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
fun ProfileTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ProductHunter",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = PH_Primary
        )
        
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFBDBDBD))
                .align(Alignment.CenterEnd)
        )
    }
}

@Composable
fun ProfileHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(110.dp).clip(CircleShape).border(4.dp, Color.White, CircleShape).background(Color(0xFFEEEEEE))
                )
                Surface(shape = CircleShape, color = PH_Primary, modifier = Modifier.size(26.dp).border(2.dp, Color.White, CircleShape)) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Alex Thompson", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(text = "alex.hunter@example.com", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileBadge(icon = Icons.Default.Key, text = "Elite Hunter", color = PH_Primary, bgColor = Color(0xFFFFF1EB))
                ProfileBadge(icon = Icons.Default.Sync, text = "Synced", color = Color(0xFF455A64), bgColor = Color(0xFFECEFF1))
            }
        }
    }
}

@Composable
fun ProfileBadge(icon: ImageVector, text: String, color: Color, bgColor: Color) {
    Surface(shape = RoundedCornerShape(12.dp), color = bgColor) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray.copy(alpha = 0.8f), modifier = Modifier.padding(start = 8.dp, bottom = 12.dp))
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)) {
            Column { content() }
        }
    }
}

@Composable
fun SettingsToggleItem(icon: ImageVector, iconColor: Color, contentColor: Color, title: String, description: String, initialValue: Boolean) {
    var checked by remember { mutableStateOf(initialValue) }
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(12.dp), color = iconColor, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp)) }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = { checked = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PH_Primary))
    }
}

@Composable
fun SettingsNavigationItem(icon: ImageVector, iconColor: Color, contentColor: Color, title: String, description: String? = null, textColor: Color = Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth().clickable { }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(12.dp), color = iconColor, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp)) }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = textColor)
            if (description != null) { Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    AndroidAppProductHuntTheme { ProfileScreen(rememberNavController()) }
}
