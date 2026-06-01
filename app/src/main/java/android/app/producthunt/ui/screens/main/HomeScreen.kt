package android.app.producthunt.ui.screens.main

import android.app.producthunt.data.remote.dto.TrendingDealResponse
import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.remote.dto.detailProductId
import android.app.producthunt.data.remote.dto.discountLabel
import android.app.producthunt.ui.components.card.ProductGridCard
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import android.app.producthunt.ui.theme.PH_Primary
import android.app.producthunt.ui.viewmodel.TrendingViewModel
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    trendingViewModel: TrendingViewModel = hiltViewModel(),
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        val trendingState by trendingViewModel.trendingState.collectAsState()
        val wishlistedIds by trendingViewModel.wishlistedIds.collectAsState()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                SectionHeader(title = "Trending Deals", onActionClick = { /* View more logic */ })
            }
            
            item {
                ProductListSection(
                    trendingState = trendingState,
                    wishlistedIds = wishlistedIds,
                    onProductClick = { deal ->
                        val encodedImage = deal.mainImageUrl?.let { Uri.encode(it) }
                        val encodedName = Uri.encode(deal.productName)
                        val route = buildString {
                            append("${Route.PRODUCT_DETAIL}/${deal.detailProductId}")
                            append("?imageUrl=${encodedImage.orEmpty()}")
                            append("&productName=$encodedName")
                        }
                        navController.navigate(route)
                    },
                    onWishlistClick = { trendingViewModel.toggleWishlist(it) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TextButton(onClick = onActionClick) {
            Text("See all", color = PH_Primary)
        }
    }
}

@Composable
private fun ProductListSection(
    trendingState: UiState<List<TrendingDealResponse>>,
    wishlistedIds: Set<String>,
    onProductClick: (TrendingDealResponse) -> Unit,
    onWishlistClick: (String) -> Unit,
) {
    when (trendingState) {
        is UiState.Success -> {
            val deals = trendingState.data.take(10)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                deals.chunked(2).forEach { rowDeals ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowDeals.forEach { deal ->
                            val currentPriceLabel = "%,.0f đ".format(deal.currentPrice)
                            val originalPriceLabel = deal.originalPrice?.let { "%,.0f đ".format(it) }
                            val discountLabel = deal.discountLabel()

                            ProductGridCard(
                                title = deal.productName,
                                imageUrl = deal.mainImageUrl,
                                brand = "Hàng chính hãng",
                                currentPrice = currentPriceLabel,
                                originalPrice = originalPriceLabel,
                                discount = discountLabel,
                                modifier = Modifier.weight(1f),
                                isWishlisted = deal.detailProductId in wishlistedIds,
                                onProductClick = { onProductClick(deal) },
                                onWishlistClick = { onWishlistClick(deal.detailProductId) },
                            )
                        }
                        if (rowDeals.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        is UiState.Loading -> {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PH_Primary)
            }
        }
        is UiState.Error -> {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(text = "Failed to load deals", color = MaterialTheme.colorScheme.error)
            }
        }
        else -> {}
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AndroidAppProductHuntTheme {
        HomeScreen(navController = rememberNavController())
    }
}
