package android.app.producthunt.ui.screens.main

import android.app.producthunt.data.remote.dto.PriceAlertStatusMapper
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
import android.app.producthunt.ui.theme.PHSpacing
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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    onProductSelected: (String, String?) -> Unit = { _, _ -> },
    viewModel: PriceAlertViewModel = hiltViewModel(),
) {
    val alertsState by viewModel.alertsState.collectAsState()
    val createState by viewModel.createState.collectAsState()
    val triggerState by viewModel.triggerState.collectAsState()
    val deleteAllState by viewModel.deleteAllState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val alerts = when (val s = alertsState) {
        is UiState.Success -> s.data.mapIndexed { index, dto ->
            val currentPrice = PriceAlertStatusMapper.displayCurrentPrice(dto)
            val targetReached = PriceAlertStatusMapper.isTargetReached(dto)
            val productName = dto.product?.productName
                ?: dto.productName
                ?: "Tracked product"
            val productDetails = listOfNotNull(
                dto.product?.brand,
                dto.product?.category,
                if (dto.isActive) "Active alert" else "Paused",
            ).joinToString(" • ")
            PriceAlert(
                id = index,
                name = productName,
                subtitle = productDetails.ifBlank { "Product ${dto.productId.take(8)}" },
                imageUrl = dto.product?.mainImageUrl ?: dto.mainImageUrl,
                currentPrice = currentPrice,
                targetPrice = dto.targetPrice,
                statusText = PriceAlertStatusMapper.statusText(dto),
                targetReached = targetReached,
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
            onConfirm = { productId, targetPrice ->
                viewModel.create(productId, targetPrice)
                scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
            },
        )
    }

    LaunchedEffect(createState) {
        when (val state = createState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Price alert saved", duration = SnackbarDuration.Short)
                viewModel.resetCreateState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
                viewModel.resetCreateState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(triggerState) {
        when (val state = triggerState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(
                    state.data.message ?: "Price check started",
                    duration = SnackbarDuration.Short,
                )
                viewModel.resetTriggerState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
                viewModel.resetTriggerState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(deleteAllState) {
        when (val state = deleteAllState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(
                    if (state.data > 0) "Cleared ${state.data} price alerts" else "No price alerts to clear",
                    duration = SnackbarDuration.Short,
                )
                viewModel.resetDeleteAllState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
                viewModel.resetDeleteAllState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item {
                    PageHeader(
                        isTriggering = triggerState is UiState.Loading,
                        isClearing = deleteAllState is UiState.Loading,
                        hasAlerts = alerts.isNotEmpty(),
                        onAddClick = { showSheet = true },
                        onTriggerClick = { viewModel.trigger() },
                        onClearAllClick = { viewModel.deleteAll() },
                    )
                }
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
                when (alertsState) {
                    is UiState.Loading, UiState.Idle -> item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Error -> item {
                        Text(
                            text = (alertsState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        )
                    }
                    is UiState.Success -> {
                        if (alerts.isEmpty()) {
                            item {
                                Text(
                                    text = "No active price alerts",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                )
                            }
                        } else {
                            items(alerts.size) { index ->
                                val dto = (alertsState as UiState.Success).data[index]
                                AlertCard(
                                    alert = alerts[index],
                                    onDeleteClick = { viewModel.delete(dto.productId) },
                                    onClick = { onProductSelected(dto.productId, alerts[index].imageUrl) },
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp),
        )
        Text(
            text = "ProductHunter",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.sp,
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
private fun PageHeader(
    isTriggering: Boolean,
    isClearing: Boolean,
    hasAlerts: Boolean,
    onAddClick: () -> Unit,
    onTriggerClick: () -> Unit,
    onClearAllClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PHSpacing.ScreenHorizontal, vertical = PHSpacing.ScreenVertical),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onClearAllClick,
            enabled = hasAlerts && !isClearing,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        ) {
            if (isClearing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onError,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Clear\nAll",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onError,
                lineHeight = 16.sp,
            )
        }

        Spacer(Modifier.width(12.dp))

        Button(
            onClick = onTriggerClick,
            enabled = !isTriggering,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (isTriggering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondary,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Run\nCheck",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary,
                lineHeight = 16.sp,
            )
        }

        Spacer(Modifier.width(12.dp))

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier.shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
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
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Scheduled Drops",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "You don't have any seasonal or recurring alerts set up. Track historical sales to anticipate the next big dip.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Browse Price History Trends",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
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
        color = MaterialTheme.colorScheme.surface,
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
        targetValue = if (item.selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
    onConfirm: (productId: String, targetPrice: Double) -> Unit,
) {
    var productId by remember { mutableStateOf("") }
    var minInput by remember { mutableStateOf("100") }
    var maxInput by remember { mutableStateOf("500") }
    var range by remember { mutableStateOf(100f..500f) }

    fun syncFromSlider(r: ClosedFloatingPointRange<Float>) {
        range = r; minInput = r.start.toInt().toString(); maxInput = r.endInclusive.toInt().toString()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
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
            Text("Set Price Range", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text("Alert when price falls within your target", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            OutlinedTextField(
                value = productId,
                onValueChange = { productId = it },
                label = { Text("Product ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

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
                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                                    focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                                ),
                                textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.width(80.dp),
                            )
                        }
                    }
                    if (i == 0) Box(Modifier.size(32.dp, 2.dp).background(MaterialTheme.colorScheme.outlineVariant))
                }
            }

            // Range slider
            RangeSlider(
                value = range,
                onValueChange = ::syncFromSlider,
                valueRange = 0f..2000f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary, inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("$0", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$2,000", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .clickable { syncFromSlider(preset) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }

            // Confirm button
            Button(
                onClick = { onConfirm(productId.trim(), range.endInclusive.toDouble()) },
                enabled = productId.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
