package android.app.producthunt.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.app.producthunt.ui.components.card.ProductMiniCard
import android.app.producthunt.ui.theme.*

sealed class MainTab(val route: String, val title: String, val icon: ImageVector) {
    object Home : MainTab("home", "Home", PHIcons.Home)
    object Trending : MainTab("trending", "Trending", PHIcons.Trending)
    object Wishlist : MainTab("wishlist", "Wishlist", PHIcons.Wishlist)
    object Alerts : MainTab("alerts", "Alerts", PHIcons.Notifications)
    object Profile : MainTab("profile", "Profile", PHIcons.Profile)
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Home) }
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
                tonalElevation = 8.dp
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
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                MainTab.Home -> PriceAlertsScreen()
                MainTab.Trending -> TrendingScreen()
                MainTab.Wishlist -> WishlistScreen()
                MainTab.Alerts -> {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Text("Alerts Screen Placeholder")
                     }
                }
                MainTab.Profile -> {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Text("Profile Screen Placeholder")
                     }
                }
            }
        }
    }
}

@Composable
fun PriceAlertsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PH_Background),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = PHIcons.Menu, contentDescription = null, tint = PH_Primary)
                Text(
                    text = "ProductHunter",
                    style = MaterialTheme.typography.titleLarge,
                    color = PH_Primary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = Color.LightGray
                ) {
                    // Avatar placeholder
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Header and Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Price",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = PH_OnBackground
                    )
                    Text(
                        text = "Alerts",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = PH_OnBackground
                    )
                }

                Button(
                    onClick = { /* Add New Alert */ },
                    colors = ButtonDefaults.buttonColors(containerColor = PH_Primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(imageVector = PHIcons.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Add New\nAlert", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Manage your precision tracking",
                style = MaterialTheme.typography.bodyMedium,
                color = PH_OnBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Master Notifications
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PH_Status_Error_Bg.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = PHIcons.Notifications, contentDescription = null, tint = PH_Price_Target)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Master Notifications",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PH_Price_Target)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Section Label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(PH_Price_Target, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ACTIVE PRECISION TRACKING",
                    style = MaterialTheme.typography.labelSmall,
                    color = PH_OnBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Active Alerts
        items(listOf(
            Triple("Sony WH-1000XM5 Headphones", "$348.00", "$299.00"),
            Triple("Apple Watch Series 9", "$389.00", "$350.00")
        )) { (title, current, target) ->
            ProductMiniCard(
                title = title,
                currentPrice = current,
                targetPrice = target,
                progress = 0.7f,
                statusText = "↘ 14% away from target price"
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))

            // Upcoming Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color.LightGray, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "UPCOMING ALERTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = PH_OnBackground.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Empty State
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PH_Status_Error_Bg.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PH_Status_Error_Bg)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Icon(
                            imageVector = PHIcons.History,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp),
                            tint = Color.LightGray
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Scheduled Drops",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You don't have any seasonal or recurring alerts set up. Track historical sales to anticipate the next big dip.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PH_OnBackground.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Browse Price History Trends",
                        style = MaterialTheme.typography.titleSmall,
                        color = PH_Price_Target,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AndroidAppProductHuntTheme {
        MainScreen()
    }
}
