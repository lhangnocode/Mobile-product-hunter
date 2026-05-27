package android.app.producthunt.ui.screens

import android.app.producthunt.R
import android.app.producthunt.domain.UiState
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.viewmodel.AuthViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun AuthGateScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val sessionState by viewModel.sessionState.collectAsState()

    LaunchedEffect(Unit) {
        if (sessionState == UiState.Idle) {
            viewModel.checkSession()
        }
    }

    LaunchedEffect(sessionState) {
        when (sessionState) {
            is UiState.Success -> {
                val hasSession = (sessionState as UiState.Success<Boolean>).data
                // Navigate to SEARCH as it is the default tab now
                navController.navigate(if (hasSession) Route.SEARCH else Route.LOGIN) {
                    popUpTo(Route.AUTH_GATE) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is UiState.Error -> {
                navController.navigate(Route.LOGIN) {
                    popUpTo(Route.AUTH_GATE) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> Unit
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.product_logo),
                    contentDescription = "Product Hunter",
                    modifier = Modifier.size(96.dp)
                )

                Text(
                    text = "Product Hunter",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                CircularProgressIndicator()

                Text(
                    text = "Đang kiểm tra phiên đăng nhập...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}