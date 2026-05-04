package android.app.producthunt.ui.screens.main

import android.app.producthunt.domain.UiState
import android.app.producthunt.model.PriceAlert
import android.app.producthunt.ui.components.card.AlertCard
import android.app.producthunt.ui.screens.notify.MasterNotificationsCard
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import android.app.producthunt.ui.viewmodel.PriceAlertViewModel
import android.app.producthunt.ui.theme.ColorBorder
import android.app.producthunt.ui.theme.ColorDivider
import android.app.producthunt.ui.theme.ColorOrange
import android.app.producthunt.ui.theme.ColorSurface
import android.app.producthunt.ui.theme.ColorText
import android.app.producthunt.ui.theme.ColorTextSub
import android.app.producthunt.ui.theme.ColorTrackBg
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SheetState
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAlertsScreen(
    onNavigateToHunt: () -> Unit = {},
    onNavigateToDeals: () -> Unit = {},
    onNavigateToSaved: () -> Unit = {},
    viewModel: PriceAlertViewModel = hiltViewModel(),
) {
    val alertsState by viewModel.alertsState.collectAsState()

    val alerts = when (val s = alertsState) {
        is UiState.Success -> s.data.mapIndexed { index, dto ->
            PriceAlert(
                id = index,
                name = dto.product?.productName ?: dto.productId,
                subtitle = dto.product?.category ?: "",
                currentPrice = 0.0,
                targetPrice = dto.targetPrice,
                placeholderColor = Color(0xFF1E1E2E),
                placeholderIcon = Icons.Filled.Headphones,
            )
        }
        else -> emptyList()
    }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (showSheet) {
        AddAlertSheet(
            sheetState = sheetState,
            onDismiss = { showSheet = false },
            onConfirm = { scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false } },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item { PageHeader(onAddClick = { showSheet = true }) }
            item {
                Spacer(Modifier.height(20.dp))
                MasterNotificationsCard(
                    enabled = notificationsEnabled,
                    onToggle = { notificationsEnabled = it },
                )
                Spacer(Modifier.height(24.dp))
            }
            item {
                SectionLabel("● ACTIVE PRECISION TRACKING")
                Spacer(Modifier.height(12.dp))
            }
            items(alerts.size) { index ->
                AlertCard(alert = alerts[index])
                Spacer(Modifier.height(12.dp))
            }
            item {
                Spacer(Modifier.height(8.dp))
                SectionLabel("◌ UPCOMING ALERTS")
                Spacer(Modifier.height(12.dp))
                NoScheduledDropsCard()
            }
        }

    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = ColorOrange,
            modifier = Modifier.size(26.dp),
        )
        Text(
            text = "ProductHunter",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ColorOrange,
            letterSpacing = (-0.5).sp,
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─── Page Header ─────────────────────────────────────────────────────────────

@Composable
private fun PageHeader(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(
                text = "Price Alerts",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = ColorText,
                lineHeight = 42.sp,
                letterSpacing = (-1).sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Manage your precision tracking",
                fontSize = 13.sp,
                color = ColorTextSub,
            )
        }
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorOrange),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier.shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = ColorOrange.copy(alpha = 0.4f),
                spotColor = ColorOrange.copy(alpha = 0.5f),
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Add New\nAlert",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 16.sp,
            )
        }
    }
}

// ─── Section Label ───────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = ColorTextSub,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 20.dp),
    )
}

// ─── No Scheduled Drops ──────────────────────────────────────────────────────

@Composable
private fun NoScheduledDropsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(ColorDivider),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = ColorTextSub,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Scheduled Drops",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorText,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "You don't have any seasonal or recurring alerts set up. Track historical sales to anticipate the next big dip.",
                fontSize = 13.sp,
                color = ColorTextSub,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Browse Price History Trends",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorOrange,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { },
            )
        }
    }
}

// ─── Bottom Nav Bar ───────────────────────────────────────────────────────────

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun BottomNavBar(
    onNavigateToHunt: () -> Unit,
    onNavigateToDeals: () -> Unit,
    onNavigateToSaved: () -> Unit,
) {
    val items = listOf(
        NavItem("Hunt",   Icons.Outlined.Search,         selected = false, onClick = onNavigateToHunt),
        NavItem("Alerts", Icons.Filled.Notifications,    selected = true,  onClick = {}),
        NavItem("Deals",  Icons.Outlined.LocalOffer,     selected = false, onClick = onNavigateToDeals),
        NavItem("Saved",  Icons.Outlined.BookmarkBorder, selected = false, onClick = onNavigateToSaved),
    )

    Surface(
        shadowElevation = 16.dp,
        color = ColorSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            items.forEach { item -> NavBarItem(item) }
        }
    }
}

@Composable
private fun NavBarItem(item: NavItem) {
    val tint by animateColorAsState(
        targetValue = if (item.selected) ColorOrange else ColorTextSub,
        label = "nav_tint_${item.label}",
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = if (item.selected) FontWeight.Bold else FontWeight.Normal,
            color = tint,
        )
    }
}

// ─── Add Alert Bottom Sheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAlertSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var minInput by remember { mutableStateOf("100") }
    var maxInput by remember { mutableStateOf("500") }
    var range by remember { mutableStateOf(100f..500f) }

    fun syncFromSlider(r: ClosedFloatingPointRange<Float>) {
        range = r; minInput = r.start.toInt().toString(); maxInput = r.endInclusive.toInt().toString()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ColorSurface,
        modifier = Modifier.imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Text("Set Price Range", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = ColorText)
            Text("Alert when price falls within your target", fontSize = 13.sp, color = ColorTextSub)

            // Min / Max input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf("Min" to minInput, "Max" to maxInput).forEachIndexed { i, (label, value) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorTextSub, letterSpacing = 0.8.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorOrange)
                            OutlinedTextField(
                                value = value,
                                onValueChange = { raw ->
                                    if (i == 0) {
                                        minInput = raw
                                        raw.toFloatOrNull()?.let { v -> if (v < range.endInclusive) range = v..range.endInclusive }
                                    } else {
                                        maxInput = raw
                                        raw.toFloatOrNull()?.let { v -> if (v > range.start) range = range.start..v }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ColorOrange, unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                                ),
                                textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ColorText),
                                modifier = Modifier.width(80.dp),
                            )
                        }
                    }
                    if (i == 0) Box(Modifier.size(32.dp, 2.dp).background(ColorBorder))
                }
            }

            // Range slider
            RangeSlider(
                value = range,
                onValueChange = ::syncFromSlider,
                valueRange = 0f..2000f,
                colors = SliderDefaults.colors(
                    thumbColor = ColorOrange, activeTrackColor = ColorOrange, inactiveTrackColor = ColorTrackBg,
                ),
            )
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("$0", fontSize = 11.sp, color = ColorTextSub)
                Text("$2,000", fontSize = 11.sp, color = ColorTextSub)
            }

            // Quick presets
            val presets = listOf("< \$100" to (0f..100f), "\$100–300" to (100f..300f), "\$300–600" to (300f..600f), "> \$600" to (600f..2000f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { (label, preset) ->
                    val selected = range == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) ColorOrange else MaterialTheme.colorScheme.background)
                            .border(1.dp, if (selected) ColorOrange else ColorBorder, RoundedCornerShape(10.dp))
                            .clickable { syncFromSlider(preset) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else ColorTextSub, textAlign = TextAlign.Center)
                    }
                }
            }

            // Confirm button
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorOrange),
            ) {
                Text("Set Alert  \$${range.start.toInt()} – \$${range.endInclusive.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PriceAlertsScreenPreview() {
    AndroidAppProductHuntTheme {
        PriceAlertsScreen()
    }
}
