package android.app.producthunt.ui.screens.main

import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.remote.dto.PriceAlertStatusMapper
import android.app.producthunt.model.PriceAlert
import android.app.producthunt.ui.components.card.AlertCard
import android.app.producthunt.ui.i18n.LocalAppStrings
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import android.app.producthunt.ui.theme.PHSpacing
import android.app.producthunt.ui.viewmodel.PriceAlertViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAlertsScreen(
    onProductSelected: (String, String, String?, String?) -> Unit = { _, _, _, _ -> },
    viewModel: PriceAlertViewModel = hiltViewModel(),
) {
    val strings = LocalAppStrings.current
    val alertsState by viewModel.alertsState.collectAsState()
    val createState by viewModel.createState.collectAsState()
    val deleteAllState by viewModel.deleteAllState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(PRICE_ALERT_REFRESH_INTERVAL_MS)
            viewModel.loadAlerts()
        }
    }

    val alerts = when (val s = alertsState) {
        is UiState.Success -> s.data.mapIndexed { index, dto ->
            val currentPrice = PriceAlertStatusMapper.displayCurrentPrice(dto)
            val targetReached = PriceAlertStatusMapper.isTargetReached(dto)
            val productName = dto.product?.productName
                ?: dto.productName
                ?: dto.rawName
                ?: strings.trackedProduct
            val productDetails = listOfNotNull(
                dto.product?.brand,
                dto.product?.category,
            ).joinToString(" • ")
            PriceAlert(
                id = index,
                name = productName,
                subtitle = productDetails.ifBlank { strings.productFallback(dto.productId) },
                imageUrl = dto.product?.mainImageUrl ?: dto.mainImageUrl,
                currentPrice = currentPrice,
                targetPrice = dto.targetPrice,
                statusText = if (targetReached) strings.targetReached else strings.waitingForPriceDrop,
                targetReached = targetReached,
                placeholderColor = Color(0xFF1E1E2E),
                placeholderIcon = Icons.Filled.Headphones,
            )
        }
        else -> emptyList()
    }

    LaunchedEffect(createState) {
        when (val state = createState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(strings.priceAlertSaveSuccess, duration = SnackbarDuration.Short)
                viewModel.resetCreateState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
                viewModel.resetCreateState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(deleteAllState) {
        when (val state = deleteAllState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(
                    if (state.data > 0) strings.clearedAlerts(state.data) else strings.noAlertsToClear,
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

    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(strings.priceAlertRemoved, duration = SnackbarDuration.Short)
                viewModel.resetDeleteState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
                viewModel.resetDeleteState()
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
            ) {
                item {
                    PageHeader(
                        isClearing = deleteAllState is UiState.Loading,
                        hasAlerts = alerts.isNotEmpty(),
                        onClearAllClick = { viewModel.deleteAll() },
                    )
                }
                item {
                    Spacer(Modifier.height(20.dp))
                    PriceAlertCounterPanel(alertCount = alerts.size)
                    Spacer(Modifier.height(16.dp))
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
                                    text = strings.noActivePriceAlerts,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                )
                            }
                        } else {
                            items(alerts.size) { index ->
                                val dto = (alertsState as UiState.Success).data[index]
                                AlertCard(
                                    alert = alerts[index],
                                    onDeleteClick = { viewModel.delete(dto.platformProductId) },
                                    onClick = {
                                        onProductSelected(
                                            dto.productId,
                                            dto.platformProductId,
                                            alerts[index].imageUrl,
                                            alerts[index].name,
                                        )
                                    },
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val PRICE_ALERT_REFRESH_INTERVAL_MS = 30_000L

@Composable
private fun PriceAlertCounterPanel(alertCount: Int) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PHSpacing.ScreenHorizontal)
            .heightIn(min = CounterPanelMinHeight),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = strings.priceAlerts,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = alertCount.toString(),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private val CounterPanelMinHeight = 82.dp

// ─── Page Header ─────────────────────────────────────────────────────────────

@Composable
private fun PageHeader(
    isClearing: Boolean,
    hasAlerts: Boolean,
    onClearAllClick: () -> Unit,
) {
    val strings = LocalAppStrings.current
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
                text = strings.clearAll.replace("\n", " "),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onError,
                lineHeight = 16.sp,
            )
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
