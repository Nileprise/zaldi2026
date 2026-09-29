package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ForgotStep {
    ENTER_EMAIL,
    VERIFY_OTP,
    RESET_PASSWORD,
    SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
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
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // ============================================================================
    // Real-time Mock API Functions (In production, these call the ViewModel)
    // ============================================================================

    fun sendOTP() {
        focusManager.clearFocus()
        keyboardController?.hide()
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage = "Please enter a valid email address"
            return
        }
        errorMessage = null
        isLoading = true
        
        // Simulate real API latency
        coroutineScope.launch {
            delay(1200) 
            isLoading = false
            successMessage = "Secure OTP sent to $email. Please check your inbox."
            step = ForgotStep.VERIFY_OTP
        }
    }

    fun verifyOTP() {
        focusManager.clearFocus()
        keyboardController?.hide()
        if (otp.length < 6) {
            errorMessage = "Please enter the 6-digit OTP code"
            return
        }
        errorMessage = null
        isLoading = true
        
        // Simulate real API token verification
        coroutineScope.launch {
            delay(1200)
            isLoading = false
            successMessage = "Identity verified successfully."
            step = ForgotStep.RESET_PASSWORD
        }
    }

    fun resetPassword() {
        focusManager.clearFocus()
        keyboardController?.hide()
        when {
            newPassword.length < 8 -> errorMessage = "Password must be at least 8 characters"
            newPassword != confirmPassword -> errorMessage = "Passwords do not match"
            else -> {
                errorMessage = null
                isLoading = true
                
                // Simulate real API password update
                coroutineScope.launch {
                    delay(1500)
                    isLoading = false
                    step = ForgotStep.SUCCESS
                    successMessage = "Password reset successfully. Redirecting to login..."
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Login")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Dynamic Header Text
            Text(
                text = when (step) {
                    ForgotStep.ENTER_EMAIL -> "Enter your registered email address to receive a secure password reset code."
                    ForgotStep.VERIFY_OTP -> "We've sent a 6-digit verification code to your email."
                    ForgotStep.RESET_PASSWORD -> "Create a strong new password for your account."
                    ForgotStep.SUCCESS -> "Your account is now secure."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )

            // Animated Form Container
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "form_step_animation"
            ) { currentStep ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (currentStep) {
                            ForgotStep.ENTER_EMAIL -> {
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { 
                                        email = it
                                        errorMessage = null 
                                    },
                                    label = { Text("Email Address") },
                                    placeholder = { Text("john@example.com") },
                                    leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    modifier = Modifier.fillMaxWidth().testTag("forgot_email_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { sendOTP() }),
                                    singleLine = true,
                                    isError = errorMessage != null,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            ForgotStep.VERIFY_OTP -> {
                                OutlinedTextField(
                                    value = otp,
                                    onValueChange = { 
                                        otp = it.filter { ch -> ch.isDigit() }.take(6)
                                        errorMessage = null
                                    },
                                    label = { Text("6-Digit OTP Code") },
                                    placeholder = { Text("000000") },
                                    modifier = Modifier.fillMaxWidth().testTag("forgot_otp_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { verifyOTP() }),
                                    singleLine = true,
                                    isError = errorMessage != null,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            ForgotStep.RESET_PASSWORD -> {
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { 
                                        newPassword = it
                                        errorMessage = null
                                    },
                                    label = { Text("New Password") },
                                    placeholder = { Text("Min 8 characters") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth().testTag("forgot_new_password_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                    singleLine = true,
                                    isError = errorMessage != null,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { 
                                        confirmPassword = it
                                        errorMessage = null
                                    },
                                    label = { Text("Confirm Password") },
                                    placeholder = { Text("Re-enter new password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    trailingIcon = {
                                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                            Icon(
                                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                            )
                                        }
                                    },
                                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth().testTag("forgot_confirm_password_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { resetPassword() }),
                                    singleLine = true,
                                    isError = errorMessage != null,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            
                            ForgotStep.SUCCESS -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF059669), // Success Green
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Password Reset Complete!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Status Messages
                        if (errorMessage != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = errorMessage!!, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                            }
                        } else if (successMessage != null && currentStep != ForgotStep.SUCCESS) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = successMessage!!, style = MaterialTheme.typography.labelMedium, color = Color(0xFF059669))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ============================================================================
            // Action Buttons
            // ============================================================================
            when (step) {
                ForgotStep.ENTER_EMAIL -> {
                    Button(
                        onClick = { sendOTP() },
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("forgot_send_otp_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Send Secure OTP", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                ForgotStep.VERIFY_OTP -> {
                    Button(
                        onClick = { verifyOTP() },
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("forgot_verify_otp_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Verify Token", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { 
                            step = ForgotStep.ENTER_EMAIL 
                            errorMessage = null
                            successMessage = null
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Text("Change Email Address")
                    }
                }
                ForgotStep.RESET_PASSWORD -> {
                    Button(
                        onClick = { resetPassword() },
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("forgot_reset_password_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Update Password", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { 
                            step = ForgotStep.VERIFY_OTP 
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Text("Back to OTP")
                    }
                }
                ForgotStep.SUCCESS -> {
                    Button(
                        onClick = { onResetSuccess(email, newPassword) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Text("Return to Login", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// ============================================================================
// UI Previews
// ============================================================================

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    MaterialTheme {
        ForgotPasswordScreen(
            onResetSuccess = { _, _ -> },
            onBackToLogin = {}
        )
    }
}
