package android.app.producthunt.ui.screens.alerts

import android.app.producthunt.model.PriceAlert
import android.app.producthunt.ui.components.card.AlertCard
import android.app.producthunt.ui.screens.main.*
import android.app.producthunt.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@Composable
fun PriceAlertsScreen(
    navController: NavController = rememberNavController()
) {
    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Alerts) }
    
    val tabs = listOf(
        MainTab.Home,
        MainTab.Trending,
        MainTab.Wishlist,
        MainTab.Alerts,
        MainTab.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = PH_Surface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PH_Primary,
                            selectedTextColor = PH_Primary,
                            unselectedIconColor = PH_OnSurface.copy(alpha = 0.4f),
                            unselectedTextColor = PH_OnSurface.copy(alpha = 0.4f),
                            indicatorColor = PH_Primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        },
        containerColor = ColorBackground
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                MainTab.Home -> PriceAlertsContent()
                MainTab.Trending -> TrendingContent()
                MainTab.Wishlist -> WishlistContent()
                MainTab.Alerts -> PriceAlertsContent()
                MainTab.Profile -> ProfileContent(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAlertsContent() {
    val alerts = remember {
        listOf(
            PriceAlert(
                id = 1,
                name = "Sony WH-1000XM5",
                subtitle = "Headphones",
                currentPrice = 348.0,
                targetPrice = 299.0,
                placeholderColor = Color(0xFF1E1E2E),
                placeholderIcon = Icons.Filled.Headphones,
            ),
            PriceAlert(
                id = 2,
                name = "Apple Watch",
                subtitle = "Series 9",
                currentPrice = 389.0,
                targetPrice = 350.0,
                placeholderColor = Color(0xFFE2E8F0),
                placeholderIcon = Icons.Filled.Watch,
            ),
        )
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

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar()
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item { PageHeader(onAddClick = { showSheet = true }) }
            item {
                Spacer(Modifier.height(20.dp))
                // Sử dụng MasterNotificationsCard từ file Notificatons.kt
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

@Composable
private fun TopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
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
                )
                .align(Alignment.CenterEnd),
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
            Text("Set Price Range", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = ColorText)
            Text("Alert when price falls within your target", fontSize = 13.sp, color = ColorTextSub)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ColorBackground)
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
                                    focusedBorderColor = ColorOrange,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ColorText),
                                modifier = Modifier.width(80.dp),
                            )
                        }
                    }
                    if (i == 0) Box(Modifier.size(32.dp, 2.dp).background(ColorBorder))
                }
            }

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

            val presets = listOf("< $100" to (0f..100f), "$100–300" to (100f..300f), "$300–600" to (300f..600f), "> $600" to (600f..2000f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { (label, preset) ->
                    val selected = range == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) ColorOrange else ColorBackground)
                            .border(1.dp, if (selected) ColorOrange else ColorBorder, RoundedCornerShape(10.dp))
                            .clickable { syncFromSlider(preset) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else ColorTextSub, textAlign = TextAlign.Center)
                    }
                }
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorOrange),
            ) {
                Text("Set Alert  $${range.start.toInt()} – $${range.endInclusive.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PriceAlertsScreenPreview() {
    AndroidAppProductHuntTheme {
        PriceAlertsScreen()
    }
}
