package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceTertiary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

private enum class ForgotStep {
    ENTER_EMAIL,
    VERIFY_OTP,
    RESET_PASSWORD,
    SUCCESS
}

@Composable
fun ForgotPasswordScreen(
    onResetSuccess: (email: String, newPassword: String) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(ForgotStep.ENTER_EMAIL) }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var otpSent by remember { mutableStateOf(false) }

    fun sendOTP() {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage = "Please enter a valid email address"
            return
        }
        errorMessage = ""
        isLoading = true
        successMessage = "OTP sent to $email. Check your email for the demo OTP: 123456"
        otpSent = true
        isLoading = false
    }

    fun verifyOTP() {
        if (otp.isBlank()) {
            errorMessage = "Please enter the OTP"
            return
        }
        if (otp != "123456") {
            errorMessage = "Invalid OTP. Use demo code: 123456"
            return
        }
        errorMessage = ""
        step = ForgotStep.RESET_PASSWORD
    }

    fun resetPassword() {
        when {
            newPassword.isBlank() -> errorMessage = "Password is required"
            newPassword.length < 6 -> errorMessage = "Password must be at least 6 characters"
            newPassword != confirmPassword -> errorMessage = "Passwords do not match"
            else -> {
                errorMessage = ""
                isLoading = true
                step = ForgotStep.SUCCESS
                successMessage = "Password reset successfully. Redirecting to login..."
                onResetSuccess(email, newPassword)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToLogin) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Reset Password",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = when (step) {
                ForgotStep.ENTER_EMAIL -> "Enter your email to receive a password reset code"
                ForgotStep.VERIFY_OTP -> "Enter the code sent to your email"
                ForgotStep.RESET_PASSWORD -> "Create a new password"
                ForgotStep.SUCCESS -> "Password reset successfully!"
            },
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (step == ForgotStep.ENTER_EMAIL || step == ForgotStep.VERIFY_OTP || step == ForgotStep.RESET_PASSWORD) {
                    if (step == ForgotStep.ENTER_EMAIL || step == ForgotStep.VERIFY_OTP || step == ForgotStep.RESET_PASSWORD) {
                        Text(
                            text = "Email Address",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth().testTag("forgot_email_input"),
                            leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = AmberPrimary) },
                            placeholder = { Text("john@example.com", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = step == ForgotStep.ENTER_EMAIL,
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberPrimary,
                                unfocusedBorderColor = BorderLight,
                                focusedContainerColor = SurfaceTertiary,
                                unfocusedContainerColor = SurfaceTertiary
                            )
                        )
                    }
                }

                if (step == ForgotStep.VERIFY_OTP || step == ForgotStep.RESET_PASSWORD) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "OTP Code",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it.filter { ch -> ch.isDigit() }.take(6) },
                        modifier = Modifier.fillMaxWidth().testTag("forgot_otp_input"),
                        placeholder = { Text("6-digit code", color = TextMuted) },
                        enabled = step == ForgotStep.VERIFY_OTP,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = BorderLight,
                            focusedContainerColor = SurfaceTertiary,
                            unfocusedContainerColor = SurfaceTertiary
                        )
                    )
                    if (step == ForgotStep.VERIFY_OTP) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Demo OTP: 123456",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberPrimary
                        )
                    }
                }

                if (step == ForgotStep.RESET_PASSWORD) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "New Password",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        modifier = Modifier.fillMaxWidth().testTag("forgot_new_password_input"),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberPrimary) },
                        placeholder = { Text("Min 6 characters", color = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = BorderLight,
                            focusedContainerColor = SurfaceTertiary,
                            unfocusedContainerColor = SurfaceTertiary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Confirm Password",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth().testTag("forgot_confirm_password_input"),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberPrimary) },
                        placeholder = { Text("Re-enter password", color = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = BorderLight,
                            focusedContainerColor = SurfaceTertiary,
                            unfocusedContainerColor = SurfaceTertiary
                        )
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 11.sp,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (successMessage.isNotBlank() && step != ForgotStep.SUCCESS) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = successMessage,
                        fontSize = 11.sp,
                        color = SuccessGreen,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action Buttons
        when (step) {
            ForgotStep.ENTER_EMAIL -> {
                Button(
                    onClick = { sendOTP() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("forgot_send_otp_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Send OTP", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            ForgotStep.VERIFY_OTP -> {
                Button(
                    onClick = { verifyOTP() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("forgot_verify_otp_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                    enabled = !isLoading
                ) {
                    Text("Verify OTP", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { step = ForgotStep.ENTER_EMAIL },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Back", fontWeight = FontWeight.Bold)
                }
            }
            ForgotStep.RESET_PASSWORD -> {
                Button(
                    onClick = { resetPassword() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("forgot_reset_password_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Reset Password", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.size(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { step = ForgotStep.VERIFY_OTP },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Back", fontWeight = FontWeight.Bold)
                }
            }
            ForgotStep.SUCCESS -> {
                Button(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Back to Login", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
