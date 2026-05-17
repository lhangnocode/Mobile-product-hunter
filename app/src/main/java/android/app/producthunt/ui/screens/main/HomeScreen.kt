package android.app.producthunt.ui.screens.main

import android.app.producthunt.R
import android.app.producthunt.data.remote.dto.PriceAlertResponse
import android.app.producthunt.data.remote.dto.ProductResponse
import android.app.producthunt.data.remote.dto.TrendingDealResponse
import android.app.producthunt.data.remote.dto.WishlistResponse
import android.app.producthunt.domain.UiState
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.navigation.navigateToTopLevelDestination
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import android.app.producthunt.ui.theme.PH_Primary
import android.app.producthunt.ui.viewmodel.PriceAlertViewModel
import android.app.producthunt.ui.viewmodel.ProductViewModel
import android.app.producthunt.ui.viewmodel.TrendingViewModel
import android.app.producthunt.ui.viewmodel.WishlistViewModel
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage

private val HomeOrangeDark = Color(0xFF7A2A12)

private data class HomeDeal(
    val id: String? = null,
    val title: String,
    val currentPrice: String,
    val originalPrice: String,
    val discount: String,
    val platforms: List<String>,
    val imageUrl: String? = null,
    val icon: ImageVector? = null,
)

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    trendingViewModel: TrendingViewModel = hiltViewModel(),
    wishlistViewModel: WishlistViewModel = hiltViewModel(),
    priceAlertViewModel: PriceAlertViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel(),
) {
    val trendingState by trendingViewModel.trendingState.collectAsState()
    val wishlistState by wishlistViewModel.wishlistState.collectAsState()
    val alertsState by priceAlertViewModel.alertsState.collectAsState()
    val productsState by productViewModel.productsState.collectAsState()

    val hotDeals = remember(trendingState) {
        trendingState.toHomeDeals()
    }
    val wishlist = remember(wishlistState) {
        wishlistState.toWishlistPreview()
    }
    val alerts = remember(alertsState) {
        alertsState.toAlertPreview()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item { 
            SearchPanel(onClick = { 
                navController.navigate(Route.SEARCH) 
            }) 
        }
        item { 
            PopularKeywordsSection(onKeywordClick = { keyword ->
                navController.navigate("${Route.SEARCH}?q=$keyword")
            }) 
        }
        item { BannerSection() }
        item {
            HeroDealSection(
                title = "Sản phẩm Trending",
                subtitle = "Các deal hot nhất trong ngày",
                deals = hotDeals.take(5),
                onViewMore = { navController.navigateToTopLevelDestination(Route.TRENDING) },
                onDealClick = { deal ->
                    deal.id?.let { 
                        val encodedUrl = deal.imageUrl?.let { url -> Uri.encode(url) } ?: ""
                        navController.navigate("${Route.PRODUCT_DETAIL}/$it?imageUrl=$encodedUrl") 
                    }
                }
            )
        }
        item {
            ProductListSection(
                productsState = productsState,
                onProductClick = { product ->
                    val encodedUrl = product.mainImageUrl?.let { url -> Uri.encode(url) } ?: ""
                    navController.navigate("${Route.PRODUCT_DETAIL}/${product.id}?imageUrl=$encodedUrl")
                }
            )
        }
        item {
            CompactPreviewSection(
                title = "Danh sách theo dõi",
                viewMoreText = "Xem tất cả",
                items = wishlist.take(3),
                onViewMore = { navController.navigateToTopLevelDestination(Route.WISHLIST) },
                onDealClick = { deal ->
                    deal.id?.let { 
                        val encodedUrl = deal.imageUrl?.let { url -> Uri.encode(url) } ?: ""
                        navController.navigate("${Route.PRODUCT_DETAIL}/$it?imageUrl=$encodedUrl") 
                    }
                }
            )
        }
        item {
            AlertPreviewSection(
                alerts = alerts.take(3),
                onViewMore = { navController.navigateToTopLevelDestination(Route.ALERTS) },
                onDealClick = { deal ->
                    deal.id?.let { 
                        val encodedUrl = deal.imageUrl?.let { url -> Uri.encode(url) } ?: ""
                        navController.navigate("${Route.PRODUCT_DETAIL}/$it?imageUrl=$encodedUrl") 
                    }
                }
            )
        }
    }
}

@Composable
private fun SearchPanel(onClick: () -> Unit) {
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
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(27.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(14.dp))
            Text(
                text = "Tìm kiếm hơn 10,000+ sản phẩm...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun PopularKeywordsSection(onKeywordClick: (String) -> Unit) {
    val keywords = listOf("iPhone 15", "Laptop Gaming", "Nike Air", "Bàn phím cơ", "Smart Watch", "AirPods")
    Column {
        Text(
            text = "Từ khóa phổ biến",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            keywords.forEach { keyword ->
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = keyword,
                        modifier = Modifier
                            .clickable { onKeywordClick(keyword) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerSection() {
    val banners = listOf(
        R.drawable.banner1,
        R.drawable.banner2,
        R.drawable.banner3,
        R.drawable.banner4
    )
    val pagerState = rememberPagerState(pageCount = { banners.size })

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 12.dp
        ) { page ->
            Image(
                painter = painterResource(id = banners[page]),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .height(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(banners.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
private fun ProductListSection(
    productsState: UiState<List<ProductResponse>>,
    onProductClick: (ProductResponse) -> Unit
) {
    SectionHeader(
        title = "Sản phẩm dành cho bạn",
        action = "Xem tất cả",
        onAction = { }
    )
    Spacer(Modifier.height(16.dp))

    when (productsState) {
        is UiState.Loading -> {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PH_Primary)
            }
        }
        is UiState.Success -> {
            val products = productsState.data.take(10)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                products.chunked(2).forEach { rowProducts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowProducts.forEach { product ->
                            Box(modifier = Modifier.weight(1f)) {
                                ProductGridItem(product, onClick = { onProductClick(product) })
                            }
                        }
                        if (rowProducts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        is UiState.Error -> {
            Text(
                text = "Lỗi tải sản phẩm: ${productsState.message}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(24.dp)
            )
        }
        else -> Unit
    }
}

@Composable
private fun ProductGridItem(product: ProductResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (product.mainImageUrl != null) {
                    AsyncImage(
                        model = product.mainImageUrl,
                        contentDescription = product.productName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.LightGray)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = product.brand ?: "Hàng chính hãng",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = product.productName,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp,
                modifier = Modifier.heightIn(min = 36.dp)
            )
        }
    }
}

@Composable
private fun HeroDealSection(
    title: String,
    subtitle: String,
    deals: List<HomeDeal>,
    onViewMore: () -> Unit,
    onDealClick: (HomeDeal) -> Unit,
) {
    SectionHeader(title = title, action = "Xem thêm", onAction = onViewMore)
    Spacer(Modifier.height(4.dp))
    Text(
        text = subtitle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(16.dp))
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(deals) { deal ->
            FeaturedDealCard(deal, onClick = { onDealClick(deal) })
        }
    }
}

@Composable
private fun FeaturedDealCard(deal: HomeDeal, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(200.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (deal.imageUrl != null) {
                    AsyncImage(
                        model = deal.imageUrl,
                        contentDescription = deal.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = deal.icon ?: Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(60.dp)
                    )
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart),
                ) {
                    Text(
                        text = "HOT",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Column(Modifier.padding(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    deal.platforms.take(2).forEach { platform ->
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = platform.uppercase(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = deal.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        if (deal.originalPrice.isNotEmpty()) {
                            Text(
                                text = deal.originalPrice,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                textDecoration = TextDecoration.LineThrough,
                            )
                        }
                        Text(
                            text = deal.currentPrice,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
    onDealClick: (HomeDeal) -> Unit,
) {
    SectionHeader(title = title, action = viewMoreText, onAction = onViewMore)
    Spacer(Modifier.height(14.dp))
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(items) { deal ->
            MiniDealCard(deal = deal, modifier = Modifier.width(160.dp), onClick = { onDealClick(deal) })
        }
    }
}

@Composable
private fun AlertPreviewSection(
    alerts: List<HomeDeal>,
    onViewMore: () -> Unit,
    onDealClick: (HomeDeal) -> Unit,
) {
    SectionHeader(title = "Thông báo giảm giá", action = "Xem tất cả", onAction = onViewMore)
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
                    .clickable(onClick = { onDealClick(alert) })
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
                    if (alert.imageUrl != null) {
                        AsyncImage(model = alert.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                    } else {
                        Icon(alert.icon ?: Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
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
private fun MiniDealCard(deal: HomeDeal, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.heightIn(min = 200.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                if (deal.imageUrl != null) {
                    AsyncImage(model = deal.imageUrl, contentDescription = null, contentScale = ContentScale.Fit)
                } else {
                    Icon(deal.icon ?: Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(60.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = deal.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(10.dp))
            PriceProgress(discount = deal.discount)
            Spacer(Modifier.height(14.dp))
            if (deal.originalPrice.isNotEmpty()) {
                Text(
                    text = deal.originalPrice,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    textDecoration = TextDecoration.LineThrough,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = deal.currentPrice,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
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
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = action.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            modifier = Modifier.clickable(onClick = onAction),
        )
    }
}

private fun UiState<List<TrendingDealResponse>>.toHomeDeals(): List<HomeDeal> =
    (this as? UiState.Success)
        ?.data
        ?.mapIndexed { index, deal ->
            HomeDeal(
                id = deal.id,
                title = deal.productName,
                currentPrice = deal.currentPrice.toVnd(),
                originalPrice = deal.originalPrice?.toVnd() ?: "",
                discount = deal.discountPercent?.let { "-${it.toInt()}%" } ?: "-12%",
                platforms = listOf(platformName(deal.platformId)),
                imageUrl = deal.mainImageUrl,
                icon = dealIcon(index),
            )
        }
        ?: sampleHotDeals

private fun UiState<List<WishlistResponse>>.toWishlistPreview(): List<HomeDeal> =
    (this as? UiState.Success)
        ?.data
        ?.mapIndexed { index, item ->
            HomeDeal(
                id = item.productId,
                title = item.product?.productName ?: item.productId,
                currentPrice = "Theo dõi",
                originalPrice = "",
                discount = "-${15 + index * 5}%",
                platforms = listOf("Lưu"),
                imageUrl = item.product?.mainImageUrl,
                icon = dealIcon(index),
            )
        }
        ?: sampleWishlist

private fun UiState<List<PriceAlertResponse>>.toAlertPreview(): List<HomeDeal> =
    (this as? UiState.Success)
        ?.data
        ?.mapIndexed { index, alert ->
            HomeDeal(
                id = alert.productId,
                title = alert.product?.productName ?: alert.productId,
                currentPrice = alert.targetPrice.toVnd(),
                originalPrice = "Giá mục tiêu",
                discount = "-${10 + index * 4}%",
                platforms = listOf("Báo giá"),
                imageUrl = alert.product?.mainImageUrl,
                icon = dealIcon(index + 1),
            )
        }
        ?: sampleAlerts

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
    HomeDeal(null, "Apple iPad Pro M2 (11-inch, 256GB)", "18.500.000 đ", "21.900.000 đ", "-17%", listOf("Shopee", "Lazada"), null, Icons.Default.Smartphone),
    HomeDeal(null, "Sony WH-1000XM5 Headphones", "7.500.000 đ", "8.900.000 đ", "-13%", listOf("Shopee", "Tiki"), null, Icons.Default.LocalOffer),
    HomeDeal(null, "Samsung Galaxy Watch 6 Classic", "5.200.000 đ", "7.500.000 đ", "-30%", listOf("Lazada"), null, Icons.Default.Watch),
)

private val sampleWishlist = listOf(
    HomeDeal(null, "Nike Air Zoom Pegasus 40", "2.500.000 đ", "3.200.000 đ", "-25%", listOf("Shopee"), null, Icons.Default.LocalOffer),
    HomeDeal(null, "Premium Minimalist Watch", "1.200.000 đ", "2.100.000 đ", "-40%", listOf("Tiki"), null, Icons.Default.Watch),
)

private val sampleAlerts = listOf(
    HomeDeal(null, "Sony WH-1000XM5", "6.900.000 đ target", "7.500.000 đ now", "-14%", listOf("Alert"), null, Icons.Default.LocalOffer),
)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AndroidAppProductHuntTheme {
        HomeScreen(navController = rememberNavController())
    }
}
