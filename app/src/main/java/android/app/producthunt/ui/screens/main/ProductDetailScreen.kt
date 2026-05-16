package android.app.producthunt.ui.screens.main

import android.app.producthunt.R
import android.app.producthunt.data.remote.dto.PlatformListingDto
import android.app.producthunt.data.remote.dto.PriceAnalysisResponse
import android.app.producthunt.data.remote.dto.PriceRecordResponse
import android.app.producthunt.domain.UiState
import android.app.producthunt.ui.theme.*
import android.app.producthunt.ui.viewmodel.ProductDetailViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import android.content.Intent
import android.net.Uri

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

    LaunchedEffect(productId) {
        productId?.let { viewModel.loadProductDetails(it) }
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(PH_Background)) {
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
                    val firstListing = listings.first()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        ProductHeaderCinematic(firstListing, imageUrl)

                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                            Text(
                                text = firstListing.rawName ?: "Sản phẩm tìm thấy",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PH_OnBackground,
                                lineHeight = 36.sp
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
                                        .background(Color.White, RoundedCornerShape(24.dp))
                                        .border(1.dp, PH_Primary.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Đang tổng hợp dữ liệu giá...", color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))
                            
                            Text(
                                "Giá tại các sàn TMĐT",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PH_OnBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            MarketComparisonSection(listings)
                            
                            Spacer(modifier = Modifier.height(150.dp))
                        }
                    }
                }
            }
            is UiState.Error -> ErrorState(state.message, navController)
        }

        // Custom Top Bar Back Button (Light Mode)
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PH_OnBackground)
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
                    onWishlistClick = { viewModel.toggleWishlist(id) },
                    onAlertClick = { /* Hiện dialog báo giá */ }
                )
            }
        }
    }
}

@Composable
fun ProductHeaderCinematic(listing: PlatformListingDto, imageUrl: String?) {
    val price = listing.currentPrice.toDoubleOrNull() ?: 0.0
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(Color.White)
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
                        0.7f to PH_Background.copy(alpha = 0.5f),
                        1f to PH_Background
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
                color = PH_OnBackground,
                letterSpacing = (-1.5).sp
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
            icon = Icons.Default.TrendingDown,
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
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PH_OnSurface)
        }
    }
}

@Composable
fun ModernPriceChart(history: List<PriceRecordResponse>) {
    if (history.size < 2) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(1.dp, PH_Primary.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Chưa đủ dữ liệu vẽ biểu đồ", color = Color.Gray)
        }
        return
    }

    val prices = history.map { it.price.toFloatOrNull() ?: 0f }
    val maxPrice = prices.maxOrNull() ?: 1f
    val minPrice = prices.minOrNull() ?: 0f
    val range = (maxPrice - minPrice).coerceAtLeast(1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .border(1.dp, PH_Primary.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
            .padding(top = 40.dp, bottom = 20.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            val width = size.width
            val height = size.height
            val spacing = width / (prices.size - 1).coerceAtLeast(1)

            val path = Path()
            prices.forEachIndexed { i, price ->
                val x = i * spacing
                val y = height - ((price - minPrice) / range * height)
                if (i == 0) path.moveTo(x, y) else {
                    val prevX = (i - 1) * spacing
                    val prevY = height - ((prices[i-1] - minPrice) / range * height)
                    path.cubicTo(
                        (prevX + x) / 2f, prevY,
                        (prevX + x) / 2f, y,
                        x, y
                    )
                }
            }

            // Glow Effect
            drawPath(
                path = path,
                brush = Brush.verticalGradient(listOf(PH_Primary.copy(alpha = 0.3f), Color.Transparent)),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Main Line
            drawPath(
                path = path,
                color = PH_Primary,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Current Point Bloom
            val lastX = (prices.size - 1) * spacing
            val lastY = height - ((prices.last() - minPrice) / range * height)
            drawCircle(color = PH_Primary.copy(alpha = 0.2f), radius = 15.dp.toPx(), center = Offset(lastX, lastY))
            drawCircle(color = PH_Primary, radius = 6.dp.toPx(), center = Offset(lastX, lastY))
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(lastX, lastY))
        }
        
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("%,.0f đ".format(maxPrice), modifier = Modifier.align(Alignment.TopStart), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text("%,.0f đ".format(minPrice), modifier = Modifier.align(Alignment.BottomStart), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MarketComparisonSection(listings: List<PlatformListingDto>) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listings.forEach { listing ->
            val price = listing.currentPrice.toDoubleOrNull() ?: 0.0
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(listing.affiliateUrl ?: listing.url))
                        context.startActivity(intent)
                    },
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PH_Primary.copy(alpha = 0.05f)),
                shadowElevation = 2.dp
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    val platformName = when(listing.platformId) {
                        1 -> "Shopee"
                        2 -> "Lazada"
                        3 -> "Tiki"
                        else -> "Shop"
                    }
                    Image(
                        painter = painterResource(id = R.drawable.product_logo),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(platformName, fontWeight = FontWeight.Bold, color = PH_OnSurface)
                        Text(if (listing.inStock) "Còn hàng" else "Hết hàng", fontSize = 12.sp, color = if (listing.inStock) Color(0xFF00C853) else Color.Red)
                    }
                    Text("%,.0f đ".format(price), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PH_Primary)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(navController: NavController) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Không có dữ liệu", color = Color.Gray)
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
