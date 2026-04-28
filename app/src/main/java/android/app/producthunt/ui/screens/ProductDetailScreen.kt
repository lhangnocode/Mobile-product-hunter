package android.app.producthunt.ui.screens

import android.app.producthunt.R
import android.app.producthunt.ui.components.appbar.BackTopBar
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ProductDetailScreen(
    navController: NavController
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            BackTopBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(vertical = 8.dp),
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            ProductFloatingActions()
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
        ) {
            ProductImageSection()

            Column(modifier = Modifier.padding(16.dp)) {
                ProductBasicInfo()
                Spacer(modifier = Modifier.height(24.dp))
                
                BestPriceCard()
                Spacer(modifier = Modifier.height(24.dp))

                PriceHistorySection()
                Spacer(modifier = Modifier.height(24.dp))

                MarketComparisonSection()
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ProductFloatingActions() {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        SmallFloatingActionButton(
            onClick = { /* TODO */ },
            containerColor = Color(0xFF222222),
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Add to Wishlist",
                modifier = Modifier.size(20.dp)
            )
        }

        FloatingActionButton(
            onClick = { /* TODO */ },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Set Alert",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ProductImageSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color(0xFF121212))
    ) {
        Image(
            painter = painterResource(id = R.drawable.product_logo),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentScale = ContentScale.Fit
        )
        
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            Text(
                text = "BEST VALUE",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProductBasicInfo() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Star, 
                contentDescription = null, 
                tint = Color(0xFFFFB400), 
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = " 4.9 ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "(2.4k Reviews)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sony WH-1000XM5 Noise Canceling",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "The industry-leading noise canceling headphones with dual processors and 8 microphones for unprecedented sound quality.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun BestPriceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("CURRENT BEST PRICE", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "$348.00", 
                    style = MaterialTheme.typography.headlineLarge, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "↘ -15% Today", 
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("BUY NOW AT SHOPEE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PriceHistorySection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Price History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row {
                Text("3 MONTHS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.width(12.dp))
                Text("6 MONTHS", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF121212), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    Text(" Lowest Price Ever: $312.00", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.ShowChart, 
                        contentDescription = null, 
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun MarketComparisonSection() {
    Column {
        Text("Market Comparison", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        ComparisonItem("Shopee", "$348.00", "Free Shipping", true)
        Spacer(modifier = Modifier.height(12.dp))
        ComparisonItem("Lazada", "$355.00", "+ $2.50", false)
        Spacer(modifier = Modifier.height(12.dp))
        ComparisonItem("Tiki", "$352.90", "Free Shipping", false)
    }
}

@Composable
fun ComparisonItem(shop: String, price: String, shipping: String, isBest: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(12.dp),
        border = if (isBest) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFF222222), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(shop.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(shop, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(price, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(shipping, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBest) MaterialTheme.colorScheme.primary else Color(0xFF222222)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("Go to Shop", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductDetailPreview() {
    AndroidAppProductHuntTheme(darkTheme = true) {
        ProductDetailScreen(navController = rememberNavController())
    }
}
