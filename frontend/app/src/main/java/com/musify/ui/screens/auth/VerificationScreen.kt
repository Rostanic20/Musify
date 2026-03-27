package com.musify.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musify.ui.components.MusifyButton
import com.musify.ui.components.MusifyTextField
import kotlinx.coroutines.delay
import android.content.Intent
import androidx.core.net.toUri

@Composable
fun VerificationScreen(
    verificationData: String,
    onNavigateToLogin: () -> Unit,
    onVerificationSuccess: () -> Unit,
    viewModel: VerificationViewModel = hiltViewModel()
) {
    val (type, target) = verificationData.split(":", limit = 2).let {
        if (it.size == 2) Pair(it[0], it[1]) else Pair("email", verificationData)
    }

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val isEmail = type == "email"

    var smsCode by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (isEmail) {
            while (true) {
                delay(5000)
                viewModel.checkVerificationStatus()
            }
        }
    }

    LaunchedEffect(state.isVerified) {
        if (state.isVerified) {
            onVerificationSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isEmail) Icons.Default.Email else Icons.Default.Sms,
                contentDescription = if (isEmail) "Email" else "SMS",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isEmail) "Verify Your Email" else "Verify Your Phone Number",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isEmail) {
                Text(
                    text = "We've sent a verification link to",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "We've sent a verification code to",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = target,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isEmail) {
                Text(
                    text = "Please check your inbox and click the link to verify your account.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Enter the 6-digit code we sent you.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (!isEmail) {
                MusifyTextField(
                    value = smsCode,
                    onValueChange = { if (it.length <= 6) smsCode = it },
                    label = "Verification Code",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                MusifyButton(
                    onClick = {
                        viewModel.verifySMS(smsCode, target)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading && smsCode.length == 6
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Verify")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                MusifyButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_EMAIL)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val browserIntent = Intent(Intent.ACTION_VIEW,
                                "https://mail.google.com".toUri())
                            context.startActivity(browserIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Email App")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedButton(
                onClick = {
                    if (isEmail) {
                        viewModel.resendVerificationEmail(target)
                    } else {
                        viewModel.resendVerificationSMS(target)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isResending && state.resendCooldown == 0
            ) {
                if (state.isResending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else if (state.resendCooldown > 0) {
                    Text("Resend in ${state.resendCooldown}s")
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEmail) "Resend Verification Email" else "Resend SMS Code")
                }
            }

            state.message?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isError)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = if (state.isError)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(
                onClick = onNavigateToLogin
            ) {
                Text(
                    text = "Already verified? Go to Login",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
