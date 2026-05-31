package android.app.producthunt.ui.screens.main

import android.app.producthunt.R
import android.app.producthunt.data.remote.dto.PlatformListingDto
import android.app.producthunt.data.remote.dto.PriceAnalysisResponse
import android.app.producthunt.data.remote.dto.PriceRecordResponse
import android.app.producthunt.core.state.UiState
import android.app.producthunt.ui.theme.*
import android.app.producthunt.ui.viewmodel.ProductDetailViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import android.content.Intent
import android.net.Uri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String?,
    imageUrl: String? = null,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val listingsState by viewModel.listingsState.collectAsState()
    val historyState by viewModel.historyState.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()
    val isWishlisted by viewModel.isWishlisted.collectAsState()
    val hasPriceAlert by viewModel.hasPriceAlert.collectAsState()
    val priceAlertState by viewModel.priceAlertState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPriceAlertDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        productId?.let { viewModel.loadProductDetails(it) }
    }

    LaunchedEffect(priceAlertState) {
        when (val state = priceAlertState) {
            is UiState.Success -> {
                showPriceAlertDialog = false
                snackbarHostState.showSnackbar("Đã đặt cảnh báo giá")
                viewModel.resetPriceAlertState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetPriceAlertState()
            }
            else -> Unit
        }
    }

    val scrollState = rememberScrollState()
    val currentListings = (listingsState as? UiState.Success)?.data.orEmpty()
    val currentProductTitle = currentListings.firstOrNull()?.rawName ?: "sản phẩm này"
    val currentProductPrice = currentListings
        .mapNotNull { it.currentPrice.toDoubleOrNull() }
        .minOrNull()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        when (val state = listingsState) {
            is UiState.Loading, is UiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PH_Primary)
                }
            }
            is UiState.Success -> {
                val listings = state.data
                if (listings.isEmpty()) {
                    EmptyState(navController)
                } else {
                    val bestListing = listings.bestPricedListing()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        ProductHeaderCinematic(bestListing, imageUrl)

                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                            Text(
                                text = bestListing.rawName ?: "Sản phẩm tìm thấy",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = 36.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            ProductMetaChips(
                                inStock = listings.any { it.inStock },
                                platformCount = listings.size,
                            )
                            
                            Spacer(modifier = Modifier.height(28.dp))

                            // Price Analysis Section
                            if (analysisState is UiState.Success) {
                                PriceAnalysisCards((analysisState as UiState.Success).data)
                            } else if (analysisState is UiState.Loading) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp), color = PH_Primary)
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                "Giá tại các sàn TMĐT",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            MarketComparisonSection(listings)

                            Spacer(modifier = Modifier.height(40.dp))

                            Text(
                                "Lịch sử biến động giá",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PH_Primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            when (val history = historyState) {
                                is UiState.Success -> ModernPriceChart(history.data)
                                is UiState.Loading -> Box(Modifier.height(260.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = PH_Primary, modifier = Modifier.size(32.dp))
                                }
                                else -> Box(
                                    modifier = Modifier
                                        .height(260.dp)
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Đang tổng hợp dữ liệu giá...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(150.dp))
                        }
                    }
                }
            }
            is UiState.Error -> ErrorState(state.message, navController)
        }

            // Floating Action Buttons
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 32.dp, end = 16.dp)
            ) {
                productId?.let { id ->
                    ProductFloatingActions(
                        productId = id,
                        isWishlisted = isWishlisted,
                        hasPriceAlert = hasPriceAlert,
                        onWishlistClick = { viewModel.toggleWishlist(id) },
                        onAlertClick = { showPriceAlertDialog = true }
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            )

            if (showPriceAlertDialog && productId != null) {
                PriceAlertTargetDialog(
                    productName = currentProductTitle,
                    currentPrice = currentProductPrice,
                    isLoading = priceAlertState is UiState.Loading,
                    onDismiss = {
                        if (priceAlertState !is UiState.Loading) {
                            showPriceAlertDialog = false
                        }
                    },
                    onConfirm = { targetPrice ->
                        viewModel.createPriceAlert(productId, targetPrice)
                    },
                )
            }
        }
    }
}

@Composable
private fun PriceAlertTargetDialog(
    productName: String,
    currentPrice: Double?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var targetPriceInput by remember(currentPrice) {
        mutableStateOf(currentPrice?.let { "%.0f".format(it) } ?: "")
    }
    val targetPrice = targetPriceInput.replace(",", "").trim().toDoubleOrNull()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 18.dp,
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "CẢNH BÁO GIÁ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Text(
                    text = "Theo dõi ${productName}. Chúng tôi sẽ gửi email ngay khi giá giảm xuống mức này.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                )

                OutlinedTextField(
                    value = targetPriceInput,
                    onValueChange = { raw ->
                        if (raw.all { it.isDigit() || it == ',' || it == '.' }) {
                            targetPriceInput = raw
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("VD: 7690000") },
                    suffix = { Text("đ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    shape = RoundedCornerShape(16.dp),
                )

                Button(
                    onClick = { targetPrice?.let(onConfirm) },
                    enabled = targetPrice != null && targetPrice > 0.0 && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(
                            text = "XÁC NHẬN ĐẶT THÔNG BÁO",
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductMetaChips(
    inStock: Boolean,
    platformCount: Int,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AssistChip(
            onClick = {},
            label = { Text(if (inStock) "Còn hàng" else "Hết hàng") },
            leadingIcon = {
                Icon(
                    imageVector = if (inStock) Icons.Default.Inventory2 else Icons.Default.RemoveShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                labelColor = if (inStock) PH_Status_Success_Text else MaterialTheme.colorScheme.error,
                leadingIconContentColor = if (inStock) PH_Status_Success_Text else MaterialTheme.colorScheme.error,
                containerColor = if (inStock) PH_Status_Success_Bg else MaterialTheme.colorScheme.errorContainer,
            ),
            border = null,
        )
        AssistChip(
            onClick = {},
            label = { Text("$platformCount sàn") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        )
    }
}

@Composable
fun ProductHeaderCinematic(listing: PlatformListingDto, imageUrl: String?) {
    val price = listing.currentPrice.toDoubleOrNull() ?: 0.0
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.product_logo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(80.dp)
                    .alpha(0.5f),
                contentScale = ContentScale.Fit
            )
        }

        // Soft Light Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.7f to MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        1f to MaterialTheme.colorScheme.background
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Surface(
                color = PH_Primary,
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    text = "GIÁ TỐT NHẤT",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "%,.0f đ".format(price),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 0.sp
            )
        }
    }
}

@Composable
fun PriceAnalysisCards(analysis: PriceAnalysisResponse) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        AnalysisCard(
            label = "Thấp nhất",
            value = "%,.0f đ".format(analysis.allTimeLow ?: analysis.currentPrice),
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            color = Color(0xFF00C853),
            modifier = Modifier.weight(1f)
        )
        AnalysisCard(
            label = "Giá trung bình",
            value = "%,.0f đ".format(analysis.averagePrice ?: analysis.currentPrice),
            icon = Icons.Default.Timeline,
            color = Color(0xFF2962FF),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AnalysisCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ModernPriceChart(history: List<PriceRecordResponse>) {
    val points = history
        .mapIndexedNotNull { index, record ->
            record.price.toFloatOrNull()?.let { price ->
                PriceChartPoint(
                    price = price,
                    recordedAt = record.recordedAt,
                    recordedAtMillis = parseRecordedAtMillis(record.recordedAt),
                    sourceIndex = index,
                )
            }
        }
        .sortedWith { left, right ->
            when {
                left.recordedAtMillis != null && right.recordedAtMillis != null ->
                    left.recordedAtMillis.compareTo(right.recordedAtMillis)
                else -> left.sourceIndex.compareTo(right.sourceIndex)
            }
        }

    if (points.size < 2) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Chưa đủ dữ liệu vẽ biểu đồ", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val prices = points.map { it.price }
    val rawMaxPrice = prices.maxOrNull() ?: 1f
    val rawMinPrice = prices.minOrNull() ?: 0f
    val rawRange = rawMaxPrice - rawMinPrice
    val pricePadding = when {
        rawMaxPrice <= 0f -> 1f
        rawRange <= 0f -> rawMaxPrice * 0.08f
        else -> rawRange * 0.18f
    }.coerceAtLeast(1f)
    val chartMaxPrice = rawMaxPrice + pricePadding
    val chartMinPrice = (rawMinPrice - pricePadding).coerceAtLeast(0f)
    val chartRange = (chartMaxPrice - chartMinPrice).coerceAtLeast(1f)
    val yAxisLabels = List(5) { index ->
        chartMaxPrice - (chartRange * index / 4f)
    }
    val xAxisIndexes = chartAxisIndexes(points.size)
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(28.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 58.dp, end = 18.dp, top = 28.dp, bottom = 42.dp)
        ) {
            val width = size.width
            val height = size.height
            val spacing = width / (prices.size - 1).coerceAtLeast(1)

            yAxisLabels.forEach { label ->
                val y = height - ((label - chartMinPrice) / chartRange * height)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f),
                )
            }

            val linePath = Path()
            prices.forEachIndexed { i, price ->
                val x = i * spacing
                val y = height - ((price - chartMinPrice) / chartRange * height)
                if (i == 0) linePath.moveTo(x, y) else {
                    val prevX = (i - 1) * spacing
                    val prevY = height - ((prices[i-1] - chartMinPrice) / chartRange * height)
                    linePath.cubicTo(
                        (prevX + x) / 2f, prevY,
                        (prevX + x) / 2f, y,
                        x, y
                    )
                }
            }

            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    listOf(PH_Primary.copy(alpha = 0.24f), Color.Transparent),
                    startY = 0f,
                    endY = height,
                ),
            )

            drawPath(
                path = linePath,
                color = PH_Primary,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            val lastX = (prices.size - 1) * spacing
            val lastY = height - ((prices.last() - chartMinPrice) / chartRange * height)
            drawCircle(color = PH_Primary.copy(alpha = 0.2f), radius = 15.dp.toPx(), center = Offset(lastX, lastY))
            drawCircle(color = PH_Primary, radius = 6.dp.toPx(), center = Offset(lastX, lastY))
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(lastX, lastY))
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 22.dp, bottom = 48.dp)
                .width(44.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End,
        ) {
            yAxisLabels.forEach { label ->
                Text(
                    text = formatCompactPrice(label),
                    fontSize = 11.sp,
                    color = axisTextColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 58.dp, end = 18.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            xAxisIndexes.forEach { index ->
                Text(
                    text = formatDateAxisLabel(points[index].recordedAt),
                    fontSize = 11.sp,
                    color = axisTextColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private data class PriceChartPoint(
    val price: Float,
    val recordedAt: String,
    val recordedAtMillis: Long?,
    val sourceIndex: Int,
)

private fun chartAxisIndexes(size: Int): List<Int> {
    if (size <= 4) return (0 until size).toList()
    return listOf(0, size / 3, (size * 2) / 3, size - 1).distinct()
}

private fun formatCompactPrice(price: Float): String =
    when {
        price >= 1_000_000f -> String.format(Locale.US, "%.1fM", price / 1_000_000f)
        price >= 1_000f -> String.format(Locale.US, "%.0fK", price / 1_000f)
        else -> String.format(Locale.US, "%.0f", price)
    }

private fun formatDateAxisLabel(rawDate: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM")
    return runCatching { OffsetDateTime.parse(rawDate).format(formatter) }
        .recoverCatching { LocalDateTime.parse(rawDate).format(formatter) }
        .getOrElse {
            if (rawDate.length >= 10) "${rawDate.substring(8, 10)}/${rawDate.substring(5, 7)}" else rawDate
        }
}

private fun parseRecordedAtMillis(rawDate: String): Long? =
    runCatching { OffsetDateTime.parse(rawDate).toInstant().toEpochMilli() }
        .recoverCatching {
            LocalDateTime.parse(rawDate).toInstant(OffsetDateTime.now().offset).toEpochMilli()
        }
        .getOrNull()

@Composable
fun MarketComparisonSection(listings: List<PlatformListingDto>) {
    val context = LocalContext.current
    val sortedListings = listings.sortedBy { it.currentPrice.toDoubleOrNull() ?: Double.MAX_VALUE }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sortedListings.forEachIndexed { index, listing ->
            val price = listing.currentPrice.toDoubleOrNull() ?: 0.0
            val platformName = platformName(listing.platformId)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(listing.affiliateUrl ?: listing.url))
                        context.startActivity(intent)
                    },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 2.dp
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    PlatformBadge(platformName = platformName)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(platformName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = when {
                                index == 0 && listing.inStock -> "Giá tốt nhất • Còn hàng"
                                listing.inStock -> "Còn hàng"
                                else -> "Hết hàng"
                            },
                            fontSize = 12.sp,
                            color = if (listing.inStock) PH_Status_Success_Text else MaterialTheme.colorScheme.error,
                        )
                    }
                    Text("%,.0f đ".format(price), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PH_Primary)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PlatformBadge(platformName: String) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = platformName.firstOrNull()?.uppercase() ?: "S",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
        )
    }
}

private fun platformName(platformId: Int): String =
    when (platformId) {
        1 -> "Shopee"
        2 -> "Lazada"
        3 -> "Tiki"
        7 -> "FPT Shop"
        8 -> "Phong Vu"
        9 -> "CellphoneS"
        else -> "Platform $platformId"
    }

private fun List<PlatformListingDto>.bestPricedListing(): PlatformListingDto =
    minByOrNull { it.currentPrice.toDoubleOrNull() ?: Double.MAX_VALUE } ?: first()

@Composable
private fun EmptyState(navController: NavController) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Không có dữ liệu", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = PH_Primary)) {
                Text("Quay lại", color = Color.White)
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, navController: NavController) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Lỗi: $message", color = PH_Status_Error_Text, textAlign = TextAlign.Center)
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = PH_Primary)) {
                Text("Quay lại", color = Color.White)
            }
        }
    }
}

@Composable
private fun Text(
    text: String, 
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified, 
    color: Color = Color.Unspecified, 
    fontWeight: FontWeight? = null,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null
) {
    androidx.compose.material3.Text(
        text = text, 
        modifier = modifier,
        fontSize = fontSize, 
        color = color, 
        fontWeight = fontWeight,
        style = style,
        lineHeight = lineHeight,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
}
