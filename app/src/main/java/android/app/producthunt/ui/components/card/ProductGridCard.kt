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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.producthunt.ui.theme.*
import android.app.producthunt.R
import coil.compose.AsyncImage

@Composable
fun ProductGridCard(
    title: String,
    currentPrice: String? = null,
    imageUrl: String?,
    brand: String? = null,
    originalPrice: String? = null,
    discount: String? = null,
    isWishlisted: Boolean = false,
    modifier: Modifier = Modifier,
    onProductClick: () -> Unit = {},
    onWishlistClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onProductClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Product Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.product_logo),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).alpha(0.3f),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Discount Badge overlay
                if (!discount.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                        color = PH_Primary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = discount,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isWishlisted) PHIcons.Wishlist else PHIcons.WishlistOutlined,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Brand Label
            Text(
                text = brand ?: "Hàng chính hãng",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            // Product Name
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp,
                modifier = Modifier.heightIn(min = 36.dp)
            )

            // Price Section (Optional)
            if (!currentPrice.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = currentPrice,
                    color = PH_Primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!originalPrice.isNullOrBlank()) {
                    Text(
                        text = originalPrice,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
