package android.app.producthunt.ui.screens.main

import android.app.producthunt.ui.components.card.ProductGridCard
import android.app.producthunt.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class ProductDeal(
    val id: String,
    val title: String,
    val currentPrice: String,
    val originalPrice: String?,
    val discount: String?,
    val isWishlisted: Boolean = false
)

@Composable
fun TrendingScreen(
    navController: NavController = rememberNavController()
) {
    // Logic quản lý Tab giống ProfileScreen
    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Trending) }
    
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
                MainTab.Trending -> TrendingContent()
                MainTab.Wishlist -> WishlistContent()
                MainTab.Alerts -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Alerts Screen Content")
                    }
                }
                MainTab.Profile -> {
                    ProfileContent(navController = navController)
                }
            }
        }
    }
}

@Composable
fun TrendingContent() {
    val trendingDeals = listOf(
        ProductDeal("1", "Tablet Apple Ipad Air M2 11 Inch", "14.190.000 đ", "16.990.000 đ", "-16%"),
        ProductDeal("2", "Smartphone Honor X9d", "9.790.000 đ", "10.990.000 đ", "-11%", true),
        ProductDeal("3", "Smartphone Samsung Galaxy A36 5G", "7.340.000 đ", "8.140.000 đ", "-10%"),
        ProductDeal("4", "Tablet Samsung Galaxy Tab S10 Lite", "7.990.000 đ", "8.990.000 đ", "-11%"),
        ProductDeal("5", "Smartphone Apple iPhone 16 Plus", "24.990.000 đ", "26.990.000 đ", "-8%"),
        ProductDeal("6", "Smartphone Samsung Galaxy A56", "9.250.000 đ", "9.990.000 đ", "-8%"),
        ProductDeal("7", "Generic Honor X5b Plus", "2.240.000 đ", "2.990.000 đ", "-25%"),
        ProductDeal("8", "Tablet Lenovo Tab 10", "3.650.000 đ", "4.190.000 đ", "-13%")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PH_Background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = PH_Primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = PHIcons.Trending,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = PH_Primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "MARKET TRENDS",
                    style = MaterialTheme.typography.labelSmall,
                    color = PH_Primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "TRENDING DEALS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PH_OnBackground,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Sản phẩm đang giảm giá thật sự (đã kiểm tra lịch sử giá)",
                    style = MaterialTheme.typography.bodySmall,
                    color = PH_OnBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Product Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(trendingDeals) { deal ->
                ProductGridCard(
                    title = deal.title,
                    currentPrice = deal.currentPrice,
                    originalPrice = deal.originalPrice,
                    discount = deal.discount,
                    isWishlisted = deal.isWishlisted,
                    onProductClick = { /* Handle click */ },
                    onWishlistClick = { /* Handle wishlist */ }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrendingScreenPreview() {
    AndroidAppProductHuntTheme {
        TrendingScreen()
    }
}
