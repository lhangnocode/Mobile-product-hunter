package android.app.producthunt.ui.screens.main

import android.app.producthunt.data.local.LanguageMode
import android.app.producthunt.ui.i18n.LocalAppStrings
import android.app.producthunt.ui.i18n.LocalLanguageMode
import android.widget.ImageView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart

@Composable
fun AppInformationScreen(
    modifier: Modifier = Modifier
) {
    val copy = appInfoCopy(LocalLanguageMode.current)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        HeroSection(copy)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
        AboutSection(copy)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
        FeaturesSection(copy)
        Spacer(modifier = Modifier.height(16.dp))
        StatsRow(copy)
        Spacer(modifier = Modifier.height(16.dp))
        SupportSection(copy)
    }
}

@Composable
private fun HeroSection(copy: AppInfoCopy) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLauncherIcon(modifier = Modifier.size(96.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Product Hunter",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = copy.versionPlan,
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
            text = copy.rating,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        )
    }
}

@Composable
private fun AppLauncherIcon(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            ImageView(viewContext).apply {
                contentDescription = "Product Hunter"
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageDrawable(context.packageManager.getApplicationIcon(context.packageName))
            }
        },
        update = { imageView ->
            imageView.setImageDrawable(context.packageManager.getApplicationIcon(context.packageName))
        },
    )
}

@Composable
private fun AboutSection(copy: AppInfoCopy) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
        SectionLabel(copy.aboutTitle)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = copy.aboutBody,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun FeaturesSection(copy: AppInfoCopy) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
        SectionLabel(copy.featuresTitle)
        Spacer(modifier = Modifier.height(10.dp))
        val divider = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        FeatureItem(Icons.Outlined.Search,        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),   MaterialTheme.colorScheme.primary,   copy.searchTitle, copy.searchDescription)
        HorizontalDivider(color = divider, thickness = 0.5.dp)
        FeatureItem(Icons.Outlined.TableChart,    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),  MaterialTheme.colorScheme.tertiary,  copy.compareTitle, copy.compareDescription)
        HorizontalDivider(color = divider, thickness = 0.5.dp)
        FeatureItem(Icons.AutoMirrored.Outlined.ShowChart,     MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), MaterialTheme.colorScheme.secondary, copy.historyTitle, copy.historyDescription)
        HorizontalDivider(color = divider, thickness = 0.5.dp)
        FeatureItem(Icons.Outlined.Notifications, MaterialTheme.colorScheme.error.copy(alpha = 0.12f),     MaterialTheme.colorScheme.error,     copy.alertTitle, copy.alertDescription)
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
private fun StatsRow(copy: AppInfoCopy) {
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
        StatItem(copy.free, copy.price)
        VerticalDivider(modifier = Modifier.height(36.dp), color = divColor)
        StatItem("—", copy.size)
        VerticalDivider(modifier = Modifier.height(36.dp), color = divColor)
        StatItem("Android", copy.platform)
        VerticalDivider(modifier = Modifier.height(36.dp), color = divColor)
        StatItem("4+", copy.age)
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
private fun SupportSection(copy: AppInfoCopy) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionLabel(copy.supportTitle)
        Spacer(modifier = Modifier.height(4.dp))
        val divider = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        SupportItem(Icons.Outlined.Email, copy.contactUs, "support@producthunter.app")
        HorizontalDivider(color = divider, thickness = 0.5.dp)
        SupportItem(Icons.Outlined.Lock, copy.privacyPolicy)
        HorizontalDivider(color = divider, thickness = 0.5.dp)
        SupportItem(Icons.Outlined.Description, copy.termsOfService)
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

private data class AppInfoCopy(
    val versionPlan: String,
    val rating: String,
    val aboutTitle: String,
    val aboutBody: String,
    val featuresTitle: String,
    val searchTitle: String,
    val searchDescription: String,
    val compareTitle: String,
    val compareDescription: String,
    val historyTitle: String,
    val historyDescription: String,
    val alertTitle: String,
    val alertDescription: String,
    val free: String,
    val price: String,
    val size: String,
    val platform: String,
    val age: String,
    val supportTitle: String,
    val contactUs: String,
    val privacyPolicy: String,
    val termsOfService: String,
)

@Composable
private fun appInfoCopy(languageMode: LanguageMode): AppInfoCopy {
    val strings = LocalAppStrings.current
    return when (languageMode) {
        LanguageMode.ENGLISH -> AppInfoCopy(
            versionPlan = "Version 1.0.0 · ${strings.freePlan}",
            rating = "4.8 · 2,400 ratings",
            aboutTitle = "About",
            aboutBody = "Product Hunter is your smart shopping companion. Search any product and instantly compare prices, ratings, and shipping costs across major e-commerce platforms in one place.",
            featuresTitle = "Features",
            searchTitle = "Multi-platform price search",
            searchDescription = "Compare prices from major stores in a single search.",
            compareTitle = "Comparison table",
            compareDescription = "Side-by-side view of price, ratings and shipping fees.",
            historyTitle = "Price history chart",
            historyDescription = "Track trends over time and know when to buy.",
            alertTitle = "Smart price alerts",
            alertDescription = "Set a target price and get notified when it drops.",
            free = "Free",
            price = "Price",
            size = "Size",
            platform = "Platform",
            age = "Age",
            supportTitle = "Support",
            contactUs = "Contact us",
            privacyPolicy = "Privacy Policy",
            termsOfService = "Terms of Service",
        )
        LanguageMode.VIETNAMESE -> AppInfoCopy(
            versionPlan = "Phiên bản 1.0.0 · ${strings.freePlan}",
            rating = "4,8 · 2.400 lượt đánh giá",
            aboutTitle = "Giới thiệu",
            aboutBody = "Product Hunter là trợ lý mua sắm thông minh. Tìm kiếm sản phẩm và so sánh nhanh giá, đánh giá, phí vận chuyển trên các sàn thương mại điện tử lớn trong một nơi.",
            featuresTitle = "Tính năng",
            searchTitle = "Tìm giá đa nền tảng",
            searchDescription = "So sánh giá từ các cửa hàng lớn chỉ với một lần tìm kiếm.",
            compareTitle = "Bảng so sánh",
            compareDescription = "Xem cạnh nhau giá bán, đánh giá và phí vận chuyển.",
            historyTitle = "Biểu đồ lịch sử giá",
            historyDescription = "Theo dõi xu hướng theo thời gian để biết lúc nên mua.",
            alertTitle = "Thông báo giá thông minh",
            alertDescription = "Đặt giá mục tiêu và nhận thông báo khi giá giảm.",
            free = "Miễn phí",
            price = "Giá",
            size = "Dung lượng",
            platform = "Nền tảng",
            age = "Độ tuổi",
            supportTitle = "Hỗ trợ",
            contactUs = "Liên hệ",
            privacyPolicy = "Chính sách bảo mật",
            termsOfService = "Điều khoản dịch vụ",
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852, name = "Samsung S24 Dark")
@Composable
private fun AppInformationScreenPreview() {
    AndroidAppProductHuntTheme(darkTheme = true) {
        AppInformationScreen()
    }
}
