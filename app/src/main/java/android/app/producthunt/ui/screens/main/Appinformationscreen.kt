package android.app.producthunt.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart

@Composable
fun AppInformationScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        HeroSection()
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
        AboutSection()
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
        FeaturesSection()
        Spacer(modifier = Modifier.height(16.dp))
        StatsRow()
        Spacer(modifier = Modifier.height(16.dp))
        SupportSection()
    }
}

@Composable
private fun HeroSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🎯", fontSize = 34.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Product Hunter",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "Version 1.0.0 · Free",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(4) { Text(text = "★", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary) }
            Text(text = "★", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "4.8 · 2,400 ratings",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        )
    }
}

@Composable
private fun AboutSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
        SectionLabel("About")
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Product Hunter is your ultimate smart shopping companion. Search any product and instantly compare prices, ratings, and shipping costs across all major e-commerce platforms — all in one place.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun FeaturesSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
        SectionLabel("Features")
        Spacer(modifier = Modifier.height(10.dp))
        val divider = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        FeatureItem(Icons.Outlined.Search,        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),   MaterialTheme.colorScheme.primary,   "Multi-platform price search",  "Compare prices from all major stores in a single search.")
        HorizontalDivider(color = divider, thickness = 0.5.dp)
        FeatureItem(Icons.Outlined.TableChart,    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),  MaterialTheme.colorScheme.tertiary,  "Comparison table",             "Side-by-side view of price, ratings & shipping fees.")
        HorizontalDivider(color = divider, thickness = 0.5.dp)
        FeatureItem(Icons.AutoMirrored.Outlined.ShowChart,     MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), MaterialTheme.colorScheme.secondary, "Price history chart",          "Track trends over time — know exactly when to buy.")
        HorizontalDivider(color = divider, thickness = 0.5.dp)
        FeatureItem(Icons.Outlined.Notifications, MaterialTheme.colorScheme.error.copy(alpha = 0.12f),     MaterialTheme.colorScheme.error,     "Smart price alerts",           "Set a target price and get notified the moment it drops.")
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconContainerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), lineHeight = 18.sp)
        }
    }
}

@Composable
private fun StatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val divColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        StatItem("Free",    "Price")
        VerticalDivider(modifier = Modifier.height(36.dp), color = divColor)
        StatItem("—",       "Size")
        VerticalDivider(modifier = Modifier.height(36.dp), color = divColor)
        StatItem("Android", "Platform")
        VerticalDivider(modifier = Modifier.height(36.dp), color = divColor)
        StatItem("4+",      "Age")
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
}

@Composable
private fun SupportSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionLabel("Support")
        Spacer(modifier = Modifier.height(4.dp))
        val divider = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        SupportItem(Icons.Outlined.Email,       "Contact us",       "support@producthunter.app")
        HorizontalDivider(color = divider, thickness = 0.5.dp)
        SupportItem(Icons.Outlined.Lock,        "Privacy Policy")
        HorizontalDivider(color = divider, thickness = 0.5.dp)
        SupportItem(Icons.Outlined.Description, "Terms of Service")
    }
}

@Composable
private fun SupportItem(icon: ImageVector, label: String, trailing: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (trailing != null) {
                Text(text = trailing, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852, name = "Samsung S24 Dark")
@Composable
private fun AppInformationScreenPreview() {
    AndroidAppProductHuntTheme(darkTheme = true) {
        AppInformationScreen()
    }
}