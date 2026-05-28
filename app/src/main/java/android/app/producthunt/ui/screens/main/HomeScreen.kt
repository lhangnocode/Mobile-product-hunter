package android.app.producthunt.ui.screens.main

import android.app.producthunt.data.remote.dto.ProductResponse
import android.app.producthunt.domain.UiState
import android.app.producthunt.ui.components.card.ProductGridCard
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import android.app.producthunt.ui.theme.PH_Primary
import android.app.producthunt.ui.viewmodel.ProductViewModel
import android.app.producthunt.ui.viewmodel.TrendingViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    trendingViewModel: TrendingViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel(),
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            // Only Trending Deals section remains as requested
            item {
                SectionHeader(title = "Trending Deals", onActionClick = { /* View more logic */ })
            }
            
            item {
                val productsState by productViewModel.productsState.collectAsState()
                ProductListSection(
                    productsState = productsState,
                    onProductClick = { product ->
                        product.id?.let { id ->
                            val encodedImage = product.mainImageUrl?.let {
                                URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
                            }
                            val route = buildString {
                                append("${Route.PRODUCT_DETAIL}/$id")
                                if (!encodedImage.isNullOrBlank()) append("?imageUrl=$encodedImage")
                            }
                            navController.navigate(route)
                        }
                    }
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
    productsState: UiState<List<ProductResponse>>,
    onProductClick: (ProductResponse) -> Unit
) {
    when (productsState) {
        is UiState.Success -> {
            val products = productsState.data.take(10)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                products.chunked(2).forEach { rowProducts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowProducts.forEach { product ->
                            ProductGridCard(
                                title = product.productName ?: "",
                                imageUrl = product.mainImageUrl,
                                brand = product.brand,
                                currentPrice = "Check price",
                                modifier = Modifier.weight(1f),
                                onProductClick = { onProductClick(product) }
                            )
                        }
                        if (rowProducts.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        is UiState.Loading -> {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PH_Primary)
            }
        }
        else -> { /* Error handled simplified */ }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AndroidAppProductHuntTheme {
        HomeScreen(navController = rememberNavController())
    }
}
