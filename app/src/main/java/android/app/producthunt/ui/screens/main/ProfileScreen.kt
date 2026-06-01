package android.app.producthunt.ui.screens.main

import android.app.producthunt.core.state.UiState
import android.app.producthunt.ui.components.UserAvatar
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.viewmodel.AuthViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val scrollState = rememberScrollState()
    val currentUserState by viewModel.currentUserState.collectAsState()
    val currentUser = (currentUserState as? UiState.Success)?.data

    LaunchedEffect(currentUserState) {
        if (currentUserState == UiState.Idle) {
            viewModel.loadCurrentUser()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section (Image 08)
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(contentAlignment = Alignment.Center) {
                UserAvatar(
                    name = currentUser?.fullName,
                    email = currentUser?.email,
                    size = 100.dp,
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = currentUser?.fullName?.takeIf { it.isNotBlank() } ?: "Product Hunter User",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = currentUser?.email ?: "Loading account...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = currentUser?.plan?.takeIf { it.isNotBlank() } ?: "Free Plan",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Menu Items (Image 08)
            ProfileMenuItem(
                icon = Icons.Default.Settings,
                title = "Account Settings",
                onClick = { /* Navigate to Account Settings */ }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.NotificationsNone,
                title = "Notification Settings",
                onClick = { /* Navigate to Notifications */ }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.CreditCard,
                title = "Manage Subscription",
                onClick = { /* Navigate to Subscription */ }
            )
            
            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "Help & Support",
                onClick = { /* Navigate to Support */ }
            )
            
            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Log Out",
                textColor = MaterialTheme.colorScheme.error,
                showArrow = false,
                onClick = {
                    viewModel.logout()
                    navController.navigate(Route.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (textColor == MaterialTheme.colorScheme.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            
            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
