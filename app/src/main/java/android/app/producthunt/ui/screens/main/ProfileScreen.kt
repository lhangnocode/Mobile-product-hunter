package android.app.producthunt.ui.screens.main

import android.app.producthunt.R
import android.app.producthunt.data.local.ThemeMode
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.viewmodel.AuthViewModel
import android.app.producthunt.ui.viewmodel.ThemeViewModel
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val themeMode by themeViewModel.themeMode.collectAsState()

    ProfileContent(
        navController = navController,
        isDarkMode = themeMode == ThemeMode.DARK,
        onDarkModeChange = themeViewModel::setDarkMode,
        onLogout = {
            viewModel.logout()
            navController.navigate(Route.LOGIN) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        },
    )
}

@Composable
fun ProfileContent(
    navController: NavController,
    isDarkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = PHSpacing.ScreenVertical),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.padding(horizontal = PHSpacing.ScreenHorizontal)) {
                SectionTitle("ACCOUNT")
                ProfileHeaderCard()
            }

            Spacer(modifier = Modifier.height(PHSpacing.SectionGap))

            Column(modifier = Modifier.padding(horizontal = PHSpacing.ScreenHorizontal)) {
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

                Spacer(modifier = Modifier.height(PHSpacing.SectionGap))

                SettingsGroup(title = "PREFERENCES") {
                    SettingsToggleItem(
                        icon = Icons.Default.DarkMode,
                        iconColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = "Dark mode",
                        description = "Enable high contrast dark appearance",
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeChange,
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.Language,
                        iconColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = "Region & Language",
                        description = "United States (English)"
                    )
                }

                Spacer(modifier = Modifier.height(PHSpacing.SectionGap))

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
                            textColor = Color(0xFFE53935),
                            onClick = onLogout,
                    )
                }
            }

            Spacer(modifier = Modifier.height(PHSpacing.SectionGap))
            Text(
                text = "Version 2.4.1 (Build 8902)\n© 2024 ProductHunter Inc.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = PHSpacing.BottomNavPadding)
            )
        }
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PHSpacing.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(72.dp).clip(CircleShape).border(3.dp, MaterialTheme.colorScheme.surface, CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp).border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Alex Thompson", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "alex.hunter@example.com", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
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
        SectionTitle(title)
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)) {
            Column { content() }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
    )
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    iconColor: Color,
    contentColor: Color,
    title: String,
    description: String,
    initialValue: Boolean = false,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    var localChecked by remember { mutableStateOf(initialValue) }
    val isChecked = checked ?: localChecked
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(12.dp), color = iconColor, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp)) }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { enabled ->
                if (checked == null) localChecked = enabled
                onCheckedChange?.invoke(enabled)
            },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
fun SettingsNavigationItem(icon: ImageVector, iconColor: Color, contentColor: Color, title: String, description: String? = null, textColor: Color = Color.Unspecified, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(12.dp), color = iconColor, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp)) }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = if (textColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else textColor)
            if (description != null) { Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    AndroidAppProductHuntTheme { ProfileScreen(rememberNavController()) }
}
