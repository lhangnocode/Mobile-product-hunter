package android.app.producthunt.ui.screens.main

import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.remote.dto.WishlistResponse
import android.app.producthunt.ui.i18n.LocalAppStrings
import android.app.producthunt.ui.i18n.LocalLanguageMode
import android.app.producthunt.ui.i18n.formatPriceFromVnd
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.theme.PHIcons
import android.app.producthunt.ui.theme.PH_Primary
import android.app.producthunt.ui.viewmodel.WishlistViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun WishlistScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    wishlistViewModel: WishlistViewModel = hiltViewModel(),
) {
    val strings = LocalAppStrings.current
    val wishlistState by wishlistViewModel.wishlistState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header for Wishlist
            Text(
                text = strings.wishlist,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (wishlistState is UiState.Success && (wishlistState as UiState.Success).data.isNotEmpty()) {
                TextButton(onClick = { wishlistViewModel.removeAll() }) {
                    Text(strings.clearAll.replace("\n", " "), color = MaterialTheme.colorScheme.error)
                }
            }
            
            SavedProductsContent(wishlistState, navController, wishlistViewModel)
        }
    }
}

@Composable
private fun SavedProductsContent(
    state: UiState<List<WishlistResponse>>,
    navController: NavController,
    viewModel: WishlistViewModel
) {
    val strings = LocalAppStrings.current
    val languageMode = LocalLanguageMode.current
    when (state) {
        is UiState.Loading, UiState.Idle -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PH_Primary)
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                EmptyWishlistState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.data) { item ->
                        val p = item.product
                        val name = p?.productName ?: item.productName ?: strings.trackedProduct
                        val image = p?.mainImageUrl ?: item.mainImageUrl
                        
                        WishlistProductCard(
                            title = name,
                            currentPrice = item.currentPrice?.let { formatPriceFromVnd(it, languageMode) } ?: strings.checking,
                            imageUrl = image,
                            onProductClick = {
                                val encodedImage = image?.let {
                                    URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
                                }
                                val route = buildString {
                                    append("${Route.PRODUCT_DETAIL}/${item.productId}")
                                    if (!encodedImage.isNullOrBlank()) append("?imageUrl=$encodedImage")
                                }
                                navController.navigate(route)
                            },
                            onRemoveClick = { viewModel.remove(item.productId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistProductCard(
    title: String,
    currentPrice: String,
    imageUrl: String?,
    onProductClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onProductClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(text = currentPrice, color = PH_Primary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(text = "↓ 300k from yesterday", color = Color(0xFF4CAF50), fontSize = 12.sp)
            }

            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
            }
        }
    }
}

@Composable
private fun EmptyWishlistState() {
    val strings = LocalAppStrings.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = PHIcons.WishlistOutlined,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = strings.emptyWishlist, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}
