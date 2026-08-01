package com.bhumap.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bhumap.app.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(onOtpSent: (String) -> Unit) {
    val vm: AuthViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper50),
    ) {
        // ─── Top brand header ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Evergreen),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text  = "BhuMap",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color      = Paper50,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "Apni Zameen, Apna Hisaab",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Evergreen200),
                )
            }
        }

        // ─── Login card ───────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)
                .offset(y = 60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Paper50),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    "Enter your mobile number",
                    style = MaterialTheme.typography.titleMedium.copy(color = Soil900),
                )

                // Phone field with +91 prefix
                OutlinedTextField(
                    value         = state.phone,
                    onValueChange = vm::onPhoneChange,
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text("Phone number") },
                    prefix        = { Text("+91  ", color = Soil500) },
                    placeholder   = { Text("98765 43210") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction    = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { vm.sendOtp(onOtpSent) }
                    ),
                    isError       = state.error != null,
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Evergreen,
                        focusedLabelColor    = Evergreen,
                        cursorColor          = Evergreen,
                    ),
                )

                if (state.error != null) {
                    Text(
                        state.error!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = Terracotta),
                    )
                }

                Button(
                    onClick  = { vm.sendOtp(onOtpSent) },
                    enabled  = state.phone.length >= 10 && !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Evergreen),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color    = Paper50,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            "Send OTP",
                            style = MaterialTheme.typography.labelLarge.copy(color = Paper50),
                        )
                    }
                }
            }
        }
    }
}
