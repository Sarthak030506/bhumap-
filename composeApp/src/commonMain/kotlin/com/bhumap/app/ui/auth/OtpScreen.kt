package com.bhumap.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhumap.app.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OtpScreen(phone: String, onSuccess: () -> Unit) {
    val vm: AuthViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val focusRequester = remember { FocusRequester() }

    // Auto-focus OTP field on entry
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Auto-submit when 6 digits entered
    LaunchedEffect(state.otp) {
        if (state.otp.length == 6) vm.verifyOtp(phone, onSuccess)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper50)
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            "Verify your number",
            style = MaterialTheme.typography.headlineSmall.copy(
                color      = Soil900,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "We sent a 6-digit OTP to $phone",
            style = MaterialTheme.typography.bodyMedium.copy(color = Soil500),
        )

        Spacer(Modifier.height(40.dp))

        // ─── 6-box OTP input ─────────────────────────────────────────────────
        BasicTextField(
            value         = state.otp,
            onValueChange = { if (it.length <= 6) vm.onOtpChange(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier      = Modifier.focusRequester(focusRequester),
            decorationBox = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    repeat(6) { index ->
                        val char = state.otp.getOrNull(index)
                        val isFocused = state.otp.length == index
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    color = if (char != null) Evergreen50 else Paper100,
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .border(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = if (isFocused) Evergreen else Soil300,
                                    shape = RoundedCornerShape(10.dp),
                                ),
                        ) {
                            Text(
                                text  = char?.toString() ?: "",
                                style = TextStyle(
                                    color      = Soil900,
                                    fontSize   = 22.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign  = TextAlign.Center,
                                ),
                            )
                        }
                    }
                }
            },
        )

        Spacer(Modifier.height(12.dp))

        if (state.error != null) {
            Text(
                state.error!!,
                style = MaterialTheme.typography.bodySmall.copy(color = Terracotta),
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick  = { vm.verifyOtp(phone, onSuccess) },
            enabled  = state.otp.length == 6 && !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Evergreen),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Paper50, strokeWidth = 2.dp)
            } else {
                Text("Verify OTP", style = MaterialTheme.typography.labelLarge.copy(color = Paper50))
            }
        }

        Spacer(Modifier.height(20.dp))

        TextButton(
            onClick = { vm.onPhoneChange(phone); vm.sendOtp {} },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Resend OTP", color = Evergreen)
        }
    }
}
