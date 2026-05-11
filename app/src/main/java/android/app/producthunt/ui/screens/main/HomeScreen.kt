package android.app.producthunt.ui.screens.main

import android.app.producthunt.R
import android.app.producthunt.data.remote.dto.PriceAlertResponse
import android.app.producthunt.data.remote.dto.TrendingDealResponse
import android.app.producthunt.data.remote.dto.WishlistResponse
import android.app.producthunt.domain.UiState
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.navigation.navigateToTopLevelDestination
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import android.app.producthunt.ui.theme.PH_Primary
import android.app.producthunt.ui.viewmodel.PriceAlertViewModel
import android.app.producthunt.ui.viewmodel.TrendingViewModel
import android.app.producthunt.ui.viewmodel.WishlistViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

private val HomeOrangeDark = Color(0xFF7A2A12)

private data class HomeDeal(
    val title: String,
    val currentPrice: String,
    val originalPrice: String,
    val discount: String,
    val platforms: List<String>,
    val icon: ImageVector,
)

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    trendingViewModel: TrendingViewModel = hiltViewModel(),
    wishlistViewModel: WishlistViewModel = hiltViewModel(),
    priceAlertViewModel: PriceAlertViewModel = hiltViewModel(),
) {
    val trendingState by trendingViewModel.trendingState.collectAsState()
    val wishlistState by wishlistViewModel.wishlistState.collectAsState()
    val alertsState by priceAlertViewModel.alertsState.collectAsState()

    val hotDeals = remember(trendingState) {
        trendingState.toHomeDeals().ifEmpty { sampleHotDeals }
    }
    val wishlist = remember(wishlistState) {
        wishlistState.toWishlistPreview().ifEmpty { sampleWishlist }
    }
    val alerts = remember(alertsState) {
        alertsState.toAlertPreview().ifEmpty { sampleAlerts }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item { SearchPanel() }
        item {
            CategorySection(
                onViewMore = { navController.navigateToTopLevelDestination(Route.TRENDING) },
            )
        }
        item {
            HeroDealSection(
                title = "Today’s Hot Deals",
                subtitle = "Pulled from trending price signals",
                deals = hotDeals.take(5),
                onViewMore = { navController.navigateToTopLevelDestination(Route.TRENDING) },
            )
        }
        item {
            CompactPreviewSection(
                title = "Recently Viewed",
                viewMoreText = "View more",
                items = sampleRecentlyViewed,
                onViewMore = { navController.navigateToTopLevelDestination(Route.TRENDING) },
            )
        }
        item {
            CompactPreviewSection(
                title = "Wishlist Preview",
                viewMoreText = "View wishlist",
                items = wishlist.take(3),
                onViewMore = { navController.navigateToTopLevelDestination(Route.WISHLIST) },
            )
        }
        item {
            AlertPreviewSection(
                alerts = alerts.take(3),
                onViewMore = { navController.navigateToTopLevelDestination(Route.ALERTS) },
            )
        }
        item {
            PriceDropGridSection(
                deals = sampleDiscountedDeals.take(4),
                onViewMore = { navController.navigateToTopLevelDestination(Route.TRENDING) },
            )
        }
        item { InsightCard() }
        item { SmartAlertCard(onClick = { navController.navigateToTopLevelDestination(Route.ALERTS) }) }
    }
}

@Composable
private fun SearchPanel() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            )
            .padding(horizontal = 24.dp, vertical = 26.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(18.dp))
            Text(
                text = "Search 10,000+ products...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun CategorySection(onViewMore: () -> Unit) {
    SectionHeader(
        title = "Featured Categories",
        action = "View more",
        onAction = onViewMore,
    )
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf("Electronics", "Fashion", "Home", "Beauty", "Gaming").forEachIndexed { index, category ->
            val selected = index == 0
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { }
                    .padding(horizontal = 24.dp, vertical = 13.dp),
            ) {
                Text(
                    text = category,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun HeroDealSection(
    title: String,
    subtitle: String,
    deals: List<HomeDeal>,
    onViewMore: () -> Unit,
) {
    SectionHeader(title = title, action = "View more", onAction = onViewMore)
    Spacer(Modifier.height(6.dp))
    Text(
        text = subtitle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(16.dp))
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(deals) { deal ->
            FeaturedDealCard(deal)
        }
    }
}

@Composable
private fun FeaturedDealCard(deal: HomeDeal) {
    Card(
        modifier = Modifier.width(316.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF363C40), Color(0xFF101010)),
                        ),
                    ),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                ) {
                    Text(
                        text = "FLASH DEAL",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    )
                }
                Icon(
                    imageVector = deal.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.Center),
                )
            }
            Column(Modifier.padding(24.dp)) {
                PlatformBadges(deal.platforms)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = deal.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 23.sp,
                    lineHeight = 28.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Best price tracked today. High demand expected.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = deal.originalPrice,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textDecoration = TextDecoration.LineThrough,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = deal.currentPrice,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactPreviewSection(
    title: String,
    viewMoreText: String,
    items: List<HomeDeal>,
    onViewMore: () -> Unit,
) {
    SectionHeader(title = title, action = viewMoreText, onAction = onViewMore)
    Spacer(Modifier.height(14.dp))
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(items) { deal ->
            MiniDealCard(deal = deal, modifier = Modifier.width(190.dp))
        }
    }
}

@Composable
private fun AlertPreviewSection(
    alerts: List<HomeDeal>,
    onViewMore: () -> Unit,
) {
    SectionHeader(title = "Alerts Nearing Target", action = "View alerts", onAction = onViewMore)
    Spacer(Modifier.height(14.dp))
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        alerts.forEach { alert ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onViewMore)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(alert.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = alert.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    PriceProgress(discount = alert.discount)
                }
                Text(
                    text = alert.currentPrice,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun PriceDropGridSection(
    deals: List<HomeDeal>,
    onViewMore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Recently Heavily Discounted",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Real-time alerts for the biggest value shifts",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.clickable(onClick = onViewMore),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("View more", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
    Spacer(Modifier.height(18.dp))
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        deals.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowItems.forEach { deal ->
                    MiniDealCard(deal = deal, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MiniDealCard(deal: HomeDeal, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.heightIn(min = 240.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(deal.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(72.dp))
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    deal.platforms.take(2).forEach {
                        Surface(color = HomeOrangeDark, shape = RoundedCornerShape(5.dp)) {
                            Text(
                                text = it.uppercase(),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = deal.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(10.dp))
            PriceProgress(discount = deal.discount)
            Spacer(Modifier.height(14.dp))
            Text(
                text = deal.originalPrice,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                textDecoration = TextDecoration.LineThrough,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = deal.currentPrice,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PriceProgress(discount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = HomeOrangeDark, shape = RoundedCornerShape(5.dp)) {
            Text(
                text = discount,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outlineVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(discount.toDiscountProgress())
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun InsightCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(30.dp)) {
                Text(
                    text = "Price History\nIntelligence",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Track every price movement and buy only when the signal is green. Our AI predicts future drops.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth(0.86f),
                )
                Spacer(Modifier.height(24.dp))
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(22.dp)) {
                    Text(
                        text = "Explore Insights",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 13.dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(150.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), Color.Transparent),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun SmartAlertCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(42.dp))
            Spacer(Modifier.height(14.dp))
            Text("Set Smart Alerts", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Get notified the second your wishlist items hit your target price.",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = action.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            modifier = Modifier.clickable(onClick = onAction),
        )
    }
}

@Composable
private fun PlatformBadges(platforms: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        platforms.take(3).forEach { platform ->
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = platform.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
    }
}

private fun UiState<List<TrendingDealResponse>>.toHomeDeals(): List<HomeDeal> =
    (this as? UiState.Success)
        ?.data
        ?.take(5)
        ?.mapIndexed { index, deal ->
            HomeDeal(
                title = deal.productName,
                currentPrice = deal.currentPrice.toVnd(),
                originalPrice = deal.originalPrice?.toVnd() ?: "",
                discount = deal.discountPercent?.let { "-${it.toInt()}%" } ?: "-12%",
                platforms = listOf(platformName(deal.platformId)),
                icon = dealIcon(index),
            )
        }
        .orEmpty()

private fun UiState<List<WishlistResponse>>.toWishlistPreview(): List<HomeDeal> =
    (this as? UiState.Success)
        ?.data
        ?.take(3)
        ?.mapIndexed { index, item ->
            HomeDeal(
                title = item.product?.productName ?: item.productId,
                currentPrice = "Tracking",
                originalPrice = "Wishlist",
                discount = "-${10 + index * 5}%",
                platforms = listOf("Saved"),
                icon = dealIcon(index),
            )
        }
        .orEmpty()

private fun UiState<List<PriceAlertResponse>>.toAlertPreview(): List<HomeDeal> =
    (this as? UiState.Success)
        ?.data
        ?.take(3)
        ?.mapIndexed { index, alert ->
            HomeDeal(
                title = alert.product?.productName ?: alert.productId,
                currentPrice = alert.targetPrice.toVnd(),
                originalPrice = "Target",
                discount = "-${8 + index * 4}%",
                platforms = listOf("Alert"),
                icon = dealIcon(index + 1),
            )
        }
        .orEmpty()

private fun Double.toVnd(): String = "%,.0f đ".format(this)

private fun String.toDiscountProgress(): Float =
    filter { it.isDigit() }
        .toFloatOrNull()
        ?.div(50f)
        ?.coerceIn(0.18f, 1f)
        ?: 0.35f

private fun platformName(platformId: Int?): String =
    when (platformId) {
        1 -> "Shopee"
        2 -> "Lazada"
        3 -> "Tiki"
        else -> "Deal"
    }

private fun dealIcon(index: Int): ImageVector =
    when (index % 4) {
        0 -> Icons.Default.Smartphone
        1 -> Icons.Default.LaptopMac
        2 -> Icons.Default.Watch
        else -> Icons.Default.LocalOffer
    }

private val sampleHotDeals = listOf(
    HomeDeal("Apple iPad Pro M2 (11-inch, 256GB)", "$749", "$899", "-17%", listOf("Shopee", "Lazada"), Icons.Default.Smartphone),
    HomeDeal("Sony WH-1000XM5 Headphones", "$348", "$399", "-13%", listOf("Shopee", "Tiki"), Icons.Default.LocalOffer),
    HomeDeal("Samsung Galaxy Watch 6 Classic", "$229", "$329", "-30%", listOf("Lazada"), Icons.Default.Watch),
)

private val sampleRecentlyViewed = listOf(
    HomeDeal("iPhone 15 Pro Max", "$999", "$1,099", "-9%", listOf("Shopee"), Icons.Default.Smartphone),
    HomeDeal("MacBook Air M3", "$1,049", "$1,199", "-13%", listOf("Tiki"), Icons.Default.LaptopMac),
    HomeDeal("Garmin Venu 3", "$349", "$449", "-22%", listOf("Lazada"), Icons.Default.Watch),
)

private val sampleWishlist = listOf(
    HomeDeal("Nike Air Zoom Pegasus 40", "$97.50", "$130", "-25%", listOf("Shopee", "Lazada"), Icons.Default.LocalOffer),
    HomeDeal("Premium Minimalist Quartz Watch", "$72", "$120", "-40%", listOf("Tiki"), Icons.Default.Watch),
    HomeDeal("Dell XPS 13 Plus 9320", "$1,249", "$1,499", "-15%", listOf("Shopee"), Icons.Default.LaptopMac),
)

private val sampleAlerts = listOf(
    HomeDeal("Sony WH-1000XM5 Headphones", "$299 target", "$348 now", "-14%", listOf("Alert"), Icons.Default.LocalOffer),
    HomeDeal("Apple Watch Series 9", "$350 target", "$389 now", "-10%", listOf("Alert"), Icons.Default.Watch),
    HomeDeal("MacBook Air M3", "$999 target", "$1,049 now", "-5%", listOf("Alert"), Icons.Default.LaptopMac),
)

private val sampleDiscountedDeals = listOf(
    HomeDeal("Dell XPS 13 Plus 9320 (Intel i7, 16GB)", "$1,249", "$1,499", "-15%", listOf("Shopee", "Lazada"), Icons.Default.LaptopMac),
    HomeDeal("Premium Minimalist Quartz Watch", "$72", "$120", "-40%", listOf("Tiki"), Icons.Default.Watch),
    HomeDeal("Nike Air Zoom Pegasus 40", "$97.50", "$130", "-25%", listOf("Shopee", "Lazada"), Icons.Default.LocalOffer),
    HomeDeal("Fujifilm Instax Mini 12", "$68", "$85", "-20%", listOf("Tiki", "Lazada"), Icons.Default.Smartphone),
)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AndroidAppProductHuntTheme {
        HomeScreen(navController = rememberNavController())
    }
}
