package android.app.producthunt.ui.screens

import android.app.producthunt.domain.UiState
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.theme.PHSpacing
import android.app.producthunt.ui.viewmodel.AuthViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun VerifyOtpScreen(
    navController: NavController,
    email: String,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var otp by remember { mutableStateOf("") }
    val verifyOtpState by viewModel.verifyOtpState.collectAsState()

    LaunchedEffect(verifyOtpState) {
        if (verifyOtpState is UiState.Success) {
            navController.navigate("${Route.RESET_PASSWORD}/$email/$otp")
            viewModel.resetVerifyOtpState()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PHSpacing.ScreenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mã OTP đã được gửi đến $email. Vui lòng kiểm tra hộp thư của bạn.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                label = { Text("Mã OTP") },
                placeholder = { Text("Nhập 6 chữ số") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (verifyOtpState is UiState.Error) {
                Text(
                    text = (verifyOtpState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { viewModel.verifyOtp(email, otp) },
                enabled = otp.length == 6 && verifyOtpState !is UiState.Loading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (verifyOtpState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("XÁC NHẬN", fontWeight = FontWeight.Bold)
                }
            }
            
            TextButton(onClick = { viewModel.forgotPassword(email) }) {
                Text("Gửi lại mã OTP")
            }
        }
    }
}
