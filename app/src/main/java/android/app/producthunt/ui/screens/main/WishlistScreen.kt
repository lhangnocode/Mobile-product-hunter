package android.app.producthunt.ui.screens.main

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
import android.app.producthunt.ui.components.card.CategoryChip
import android.app.producthunt.ui.components.card.SmartDealCard
import android.app.producthunt.ui.theme.*

data class WishlistItem(
    val id: String,
    val title: String,
    val currentPrice: String,
    val originalPrice: String? = null,
    val targetPrice: String,
    val badgeText: String? = null,
    val statusLabel: String? = null,
    val statusColor: Color = PH_Status_Warning_Text,
    val statusBgColor: Color = PH_Status_Warning_Bg,
    val isMatched: Boolean = false
)

@Composable
fun WishlistScreen(
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All Items") }
    val categories = listOf("All Items", "Dropped", "Target Reached")

    val wishlistItems = listOf(
        WishlistItem(
            "1", "Nike Air Zoom", "$120.00", "$150.00", "$115.00",
            badgeText = "-15%", statusLabel = "PRICE DROPPED",
            statusColor = PH_Status_Error_Text, statusBgColor = PH_Status_Error_Bg
        ),
        WishlistItem(
            "2", "MacBook Pro", "$2,499.00", null, "$2,299.00",
            statusLabel = "WAIT"
        ),
        WishlistItem(
            "3", "Yeezy Boost", "$310.00", "$380.00", "$310.00",
            badgeText = "NEW LOW", statusLabel = "BEST PRICE EVER",
            statusColor = PH_Status_Success_Text, statusBgColor = PH_Status_Success_Bg,
            isMatched = true
        ),
        WishlistItem(
            "4", "Ray-Ban Aviator", "$163.00", null, "$140.00",
            statusLabel = "WAIT"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PH_Background)
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
                        text = "Tracking 12 items across 4 categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = PH_OnBackground.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    imageVector = PHIcons.Add, // Using Add as a placeholder for chevron right if not in system
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = PH_OnBackground.copy(alpha = 0.3f)
                )
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

        // Wishlist Items
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp)
        ) {
            items(wishlistItems) { item ->
                SmartDealCard(
                    title = item.title,
                    currentPrice = item.currentPrice,
                    originalPrice = item.originalPrice,
                    targetPrice = item.targetPrice,
                    badgeText = item.badgeText,
                    statusLabel = item.statusLabel,
                    statusColor = item.statusColor,
                    statusBgColor = item.statusBgColor,
                    isMatched = item.isMatched,
                    onRemoveClick = { /* Handle remove */ }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WishlistScreenPreview() {
    AndroidAppProductHuntTheme {
        WishlistScreen()
    }
}
