package android.app.producthunt.ui.screens.main

import android.app.producthunt.domain.UiState
import android.app.producthunt.ui.components.card.SmartDealCard
import android.app.producthunt.ui.theme.*
import android.app.producthunt.ui.viewmodel.WishlistViewModel
import android.app.producthunt.ui.navigation.Route
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun WishlistScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WishlistViewModel = hiltViewModel(),
) {
    val wishlistState by viewModel.wishlistState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PH_Background)
    ) {
        // Header hiện đại
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                text = "MY COLLECTION",
                style = MaterialTheme.typography.labelSmall,
                color = PH_Primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "Danh sách yêu thích",
                style = MaterialTheme.typography.headlineMedium,
                color = PH_OnBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Theo dõi biến động giá của những sản phẩm bạn quan tâm",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        when (val state = wishlistState) {
            is UiState.Loading, UiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PH_Primary)
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Không thể tải danh sách: ${state.message}", color = PH_Status_Error_Text)
                }
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyWishlistState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data) { item ->
                            val product = item.product
                            SmartDealCard(
                                title = product?.productName ?: "Sản phẩm",
                                currentPrice = "Đang theo dõi giá",
                                targetPrice = "Tự động",
                                imageUrl = product?.mainImageUrl,
                                onClick = {
                                    val encodedUrl = product?.mainImageUrl?.let { Uri.encode(it) } ?: ""
                                    navController.navigate("${Route.PRODUCT_DETAIL}/${item.productId}?imageUrl=$encodedUrl")
                                },
                                onRemoveClick = { viewModel.remove(item.productId) }
                            )
                        }
                        // Thêm khoảng trống ở cuối danh sách
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWishlistState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = PHIcons.WishlistOutlined,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Danh sách của bạn đang trống", color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(text = "Hãy thêm sản phẩm để theo dõi giá nhé!", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}
