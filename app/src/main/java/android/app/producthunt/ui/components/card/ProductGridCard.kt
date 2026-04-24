package android.app.producthunt.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.producthunt.ui.theme.*

@Composable
fun ProductGridCard(
    title: String,
    currentPrice: String,
    originalPrice: String? = null,
    discount: String? = null,
    isWishlisted: Boolean = false,
    modifier: Modifier = Modifier,
    onProductClick: () -> Unit = {},
    onWishlistClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .width(180.dp)
            .padding(8.dp)
            .clickable { onProductClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PH_Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Product Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                // Placeholder for image
                Text(
                    text = "Product Image",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                // Discount Badge
                if (discount != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        color = Color(0xFF2D2D2D),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = discount,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Wishlist Icon overlay
                IconButton(
                    onClick = onWishlistClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isWishlisted) PHIcons.Wishlist else PHIcons.WishlistOutlined,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) PH_Primary else PH_OnSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = PH_OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 40.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentPrice,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PH_Status_Success_Text // Using success color for deal price
                )

                if (originalPrice != null) {
                    Text(
                        text = originalPrice,
                        style = MaterialTheme.typography.bodySmall,
                        color = PH_OnSurface.copy(alpha = 0.4f),
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductGridCardPreview() {
    AndroidAppProductHuntTheme {
        Box(modifier = Modifier.padding(16.dp).background(PH_Background)) {
            ProductGridCard(
                title = "Tablet Apple Ipad Air M2 11 Inch",
                currentPrice = "14.190.000 đ",
                originalPrice = "16.990.000 đ",
                discount = "-16%",
                isWishlisted = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductGridCardWishlistedPreview() {
    AndroidAppProductHuntTheme {
        Box(modifier = Modifier.padding(16.dp).background(PH_Background)) {
            ProductGridCard(
                title = "Smartphone Honor X9d",
                currentPrice = "9.790.000 đ",
                originalPrice = "10.990.000 đ",
                discount = "-11%",
                isWishlisted = true
            )
        }
    }
}
