package android.app.producthunt.ui.screens.main

import android.app.producthunt.domain.UiState
import android.app.producthunt.ui.components.card.ProductGridCard
import android.app.producthunt.ui.theme.*
import android.app.producthunt.ui.viewmodel.TrendingViewModel
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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
    modifier: Modifier = Modifier,
    viewModel: TrendingViewModel = hiltViewModel(),
) {
    val trendingState by viewModel.trendingState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    )
                }
            }
            is UiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(state.data) { deal ->
                        val discountLabel = deal.discountPercent?.let { "-${it.toInt()}%" }
                        val currentPriceLabel = "%.0f đ".format(deal.currentPrice)
                        val originalPriceLabel = deal.originalPrice?.let { "%.0f đ".format(it) }
                        ProductGridCard(
                            title = deal.productName,
                            currentPrice = currentPriceLabel,
                            originalPrice = originalPriceLabel,
                            discount = discountLabel,
                            isWishlisted = false,
                            onProductClick = {},
                            onWishlistClick = {},
                        )
                    }
                }
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
