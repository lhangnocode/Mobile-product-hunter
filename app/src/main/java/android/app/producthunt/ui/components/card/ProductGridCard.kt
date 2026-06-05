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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
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
    hasPriceAlert: Boolean = false,
    modifier: Modifier = Modifier,
    onProductClick: () -> Unit = {},
    onWishlistClick: () -> Unit = {},
    onPriceAlertClick: () -> Unit = {},
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
                
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    IconButton(
                        onClick = onPriceAlertClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (hasPriceAlert) PHIcons.Notifications else PHIcons.NotificationsOutlined,
                            contentDescription = "Price alert",
                            tint = if (hasPriceAlert) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onWishlistClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) PHIcons.Wishlist else PHIcons.WishlistOutlined,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (!brand.isNullOrBlank()) {
                Text(
                    text = brand,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))
            }

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

            // Price Section
            if (!currentPrice.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    if (!originalPrice.isNullOrBlank()) {
                        Text(
                            text = originalPrice,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            textDecoration = TextDecoration.LineThrough,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    
                    if (!discount.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = Color(0xFFFF5A00),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = discount,
                                fontSize = 11.sp,
                                color = Color(0xFFD84315),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))
                
                // Giá hiện tại (Đỏ đậm, to)
                Text(
                    text = currentPrice,
                    color = Color(0xFFB71C1C),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
