package android.app.producthunt.ui.screens.main

import android.app.producthunt.domain.UiState
import android.app.producthunt.ui.components.card.CategoryChip
import android.app.producthunt.ui.components.card.SmartDealCard
import android.app.producthunt.ui.theme.*
import android.app.producthunt.ui.viewmodel.WishlistViewModel
import android.app.producthunt.ui.navigation.Route
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun WishlistScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WishlistViewModel = hiltViewModel(),
) {
    var selectedCategory by remember { mutableStateOf("All Items") }
    val categories = listOf("All Items")

    val wishlistState by viewModel.wishlistState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "PERSONAL WATCHLIST",
                style = MaterialTheme.typography.labelSmall,
                color = PH_Primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your Smart Deals",
                style = MaterialTheme.typography.headlineMedium,
                color = PH_OnBackground,
                fontWeight = FontWeight.Black
            )
        }

        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PH_Status_Error_Bg.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = PH_Primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = PHIcons.Notifications,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Alerts Active",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tracking items for price drops",
                        style = MaterialTheme.typography.bodySmall,
                        color = PH_OnBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Categories
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategoryChip(
                    text = category,
                    isSelected = category == selectedCategory,
                    onClick = { selectedCategory = category }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (val state = wishlistState) {
            is UiState.Loading, UiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PH_Primary)
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = PH_Status_Error_Text)
                }
            }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp)
                ) {
                    items(state.data) { item ->
                        val product = item.product
                        SmartDealCard(
                            title = product?.productName ?: item.productId,
                            currentPrice = "—",
                            originalPrice = null,
                            targetPrice = "—",
                            badgeText = null,
                            statusLabel = null,
                            onClick = {
                                val encodedUrl = product?.mainImageUrl?.let { Uri.encode(it) } ?: ""
                                navController.navigate("${Route.PRODUCT_DETAIL}/${item.productId}?imageUrl=$encodedUrl")
                            },
                            onRemoveClick = { viewModel.remove(item.productId) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WishlistScreenPreview() {
    AndroidAppProductHuntTheme {
        WishlistScreen(navController = rememberNavController())
    }
}
