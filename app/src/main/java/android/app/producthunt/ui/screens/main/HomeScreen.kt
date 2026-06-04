package android.app.producthunt.ui.screens.main

import android.app.producthunt.data.remote.dto.TrendingDealResponse
import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.local.LanguageMode
import android.app.producthunt.data.remote.dto.detailPlatformProductId
import android.app.producthunt.data.remote.dto.detailProductId
import android.app.producthunt.data.remote.dto.discountLabel
import android.app.producthunt.ui.components.card.ProductGridCard
import android.app.producthunt.ui.i18n.LocalAppStrings
import android.app.producthunt.ui.i18n.LocalLanguageMode
import android.app.producthunt.ui.i18n.formatPriceFromVnd
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
    val strings = LocalAppStrings.current
    val languageMode = LocalLanguageMode.current
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        val trendingState by trendingViewModel.trendingState.collectAsState()
        val wishlistedIds by trendingViewModel.wishlistedIds.collectAsState()
        val priceAlertIds by trendingViewModel.priceAlertIds.collectAsState()
        val wishlistActionState by trendingViewModel.wishlistActionState.collectAsState()
        val priceAlertState by trendingViewModel.priceAlertState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        var alertDeal by remember { mutableStateOf<TrendingDealResponse?>(null) }

        LaunchedEffect(wishlistActionState) {
            when (val state = wishlistActionState) {
                is UiState.Success -> {
                    snackbarHostState.showSnackbar(
                        if (state.data) strings.wishlistAdded else strings.wishlistRemoved,
                        duration = SnackbarDuration.Short,
                    )
                    trendingViewModel.resetWishlistActionState()
                }
                is UiState.Error -> {
                    snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
                    trendingViewModel.resetWishlistActionState()
                }
                else -> Unit
            }
        }

        LaunchedEffect(priceAlertState) {
            when (val state = priceAlertState) {
                is UiState.Success -> {
                    alertDeal = null
                    snackbarHostState.showSnackbar(strings.priceAlertSaved, duration = SnackbarDuration.Short)
                    trendingViewModel.resetPriceAlertState()
                }
                is UiState.Error -> {
                    snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
                    trendingViewModel.resetPriceAlertState()
                }
                else -> Unit
            }
        }

        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item {
                    SectionHeader(title = strings.trendingDeals, onActionClick = { /* View more logic */ })
                }
                
                item {
                    ProductListSection(
                        trendingState = trendingState,
                        languageMode = languageMode,
                        wishlistedIds = wishlistedIds,
                        priceAlertIds = priceAlertIds,
                        onProductClick = { deal ->
                            val encodedImage = deal.mainImageUrl?.let { Uri.encode(it) }
                            val encodedName = Uri.encode(deal.productName)
                            val route = buildString {
                                append("${Route.PRODUCT_DETAIL}/${deal.detailProductId}")
                                append("?imageUrl=${encodedImage.orEmpty()}")
                                append("&productName=$encodedName")
                                append("&platformProductId=${deal.detailPlatformProductId}")
                            }
                            navController.navigate(route)
                        },
                        onWishlistClick = { trendingViewModel.toggleWishlist(it) },
                        onPriceAlertClick = { alertDeal = it },
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            )

            alertDeal?.let { deal ->
                PriceAlertTargetDialog(
                    productName = deal.productName,
                    currentPrice = deal.currentPrice,
                    isLoading = priceAlertState is UiState.Loading,
                    onDismiss = {
                        if (priceAlertState !is UiState.Loading) alertDeal = null
                    },
                    onConfirm = { targetPrice ->
                        trendingViewModel.createPriceAlert(deal, targetPrice)
                    },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onActionClick: () -> Unit) {
    val strings = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TextButton(onClick = onActionClick) {
            Text(strings.seeAll, color = PH_Primary)
        }
    }
}

@Composable
private fun ProductListSection(
    trendingState: UiState<List<TrendingDealResponse>>,
    languageMode: LanguageMode,
    wishlistedIds: Set<String>,
    priceAlertIds: Set<String>,
    onProductClick: (TrendingDealResponse) -> Unit,
    onWishlistClick: (TrendingDealResponse) -> Unit,
    onPriceAlertClick: (TrendingDealResponse) -> Unit,
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
                            val currentPriceLabel = formatPriceFromVnd(deal.currentPrice, languageMode)
                            val originalPriceLabel = deal.originalPrice?.let { formatPriceFromVnd(it, languageMode) }
                            val discountLabel = deal.discountLabel()

                            ProductGridCard(
                                title = deal.productName,
                                imageUrl = deal.mainImageUrl,
                                brand = null,
                                currentPrice = currentPriceLabel,
                                originalPrice = originalPriceLabel,
                                discount = discountLabel,
                                modifier = Modifier.weight(1f),
                                isWishlisted = deal.detailPlatformProductId in wishlistedIds,
                                hasPriceAlert = deal.detailPlatformProductId in priceAlertIds,
                                onProductClick = { onProductClick(deal) },
                                onWishlistClick = { onWishlistClick(deal) },
                                onPriceAlertClick = { onPriceAlertClick(deal) },
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
