package android.app.producthunt.ui.screens.main

import android.app.producthunt.domain.UiState
import android.app.producthunt.ui.components.card.ProductGridCard
import android.app.producthunt.ui.theme.*
import android.app.producthunt.ui.viewmodel.TrendingViewModel
import android.app.producthunt.ui.navigation.Route
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun TrendingScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TrendingViewModel = hiltViewModel(),
) {
    val trendingState by viewModel.trendingState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Text(
                text = "Sản phẩm đang giảm giá thật sự (đã kiểm tra lịch sử giá)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = PHSpacing.ScreenHorizontal, vertical = PHSpacing.ScreenVertical),
            )
            when (val state = trendingState) {
            is UiState.Loading, UiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PH_Primary)
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.message,
                        color = PH_Status_Error_Text,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Hiện chưa có deal nào đang trending. Quay lại sau nhé!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(state.data) { deal ->
                            val discountLabel = deal.discountPercent?.let { "-${it.toInt()}%" }
                            val currentPriceLabel = "%,.0f đ".format(deal.currentPrice)
                            val originalPriceLabel = deal.originalPrice?.let { "%,.0f đ".format(it) }
                            
                            ProductGridCard(
                                title = deal.productName,
                                currentPrice = currentPriceLabel,
                                imageUrl = deal.mainImageUrl,
                                originalPrice = originalPriceLabel,
                                discount = discountLabel,
                                isWishlisted = false,
                                onProductClick = {
                                    val encodedUrl = deal.mainImageUrl?.let { Uri.encode(it) } ?: ""
                                    navController.navigate("${Route.PRODUCT_DETAIL}/${deal.id}?imageUrl=$encodedUrl")
                                },
                                onWishlistClick = {},
                            )
                        }
                    }
                }
            }
            }
        }
    }
}
