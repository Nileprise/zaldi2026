package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
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
import com.example.data.model.UserRole
import com.example.ui.components.RoleSwitcherPill
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class AuthMode {
    LOGIN,
    SIGNUP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (identifier: String, role: UserRole) -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    
    // Form State
    var fullName by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") } // Email or Phone
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    
    // UI State
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Feedback State
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // ============================================================================
    // Real-time Mock API Functions (In production, these call the ViewModel)
    // ============================================================================

    fun requestOTP() {
        focusManager.clearFocus()
        keyboardController?.hide()
        
        if (identifier.isBlank()) {
            errorMessage = "Please enter your Email or Phone Number first."
            return
        }
        
        errorMessage = null
        isLoading = true
        
        // Simulate real API latency for requesting OTP
        coroutineScope.launch {
            delay(1200)
            isLoading = false
            isOtpSent = true
            successMessage = "Secure OTP sent to $identifier."
        }
    }

    fun submitForm() {
        focusManager.clearFocus()
        keyboardController?.hide()
        errorMessage = null
        successMessage = null

        // 1. Common Validation
        if (identifier.isBlank()) {
            errorMessage = "Email or Phone Number is required."
            return
        }
        if (password.length < 8) {
            errorMessage = "Password must be at least 8 characters."
            return
        }
        if (!isOtpSent) {
            errorMessage = "Please request and enter an OTP to proceed."
            return
        }
        if (otpCode.length < 6) {
            errorMessage = "Please enter the 6-digit OTP."
            return
        }

        // 2. Mode Specific Validation
        if (authMode == AuthMode.SIGNUP) {
            if (fullName.isBlank()) {
                errorMessage = "Full Name is required for registration."
                return
            }
            if (password != confirmPassword) {
                errorMessage = "Passwords do not match."
                return
            }
        }

        // 3. Network Execution
        isLoading = true
        coroutineScope.launch {
            delay(1500) // Simulate Auth API verification
            isLoading = false
            
            if (authMode == AuthMode.SIGNUP) {
                successMessage = "Account created successfully. Logging you in..."
                delay(800)
                onLoginSuccess(identifier, selectedRole)
            } else {
                successMessage = "Authentication successful. Welcome back!"
                delay(500)
                onLoginSuccess(identifier, selectedRole)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { /* Empty for clean design */ },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            // --- Header & Branding ---
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AKHIL LOGISTICS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Smart Delivery Access",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Role Selection ---
            Text(
                text = "Select Persona",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )

            RoleSwitcherPill(
                currentRole = selectedRole,
                onRoleSelected = { role ->
                    selectedRole = role
                    errorMessage = null
                    successMessage = null
                    isOtpSent = false
                    otpCode = ""
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Auth Mode Tabs ---
            TabRow(
                selectedTabIndex = authMode.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabRowIndicatorOffset(tabPositions[authMode.ordinal]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = authMode == AuthMode.LOGIN,
                    onClick = { 
                        authMode = AuthMode.LOGIN
                        errorMessage = null
                        successMessage = null
                    },
                    text = { Text("Login", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = authMode == AuthMode.SIGNUP,
                    onClick = { 
                        authMode = AuthMode.SIGNUP
                        errorMessage = null
                        successMessage = null
                    },
                    text = { Text("Sign Up", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Feedback Banner ---
            AnimatedVisibility(
                visible = errorMessage != null || successMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (errorMessage != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (errorMessage != null) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (errorMessage != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage ?: successMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (errorMessage != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // --- Form Container ---
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedContent(targetState = authMode, label = "form_fields") { mode ->
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Signup Only: Full Name
                            if (mode == AuthMode.SIGNUP) {
                                AuthTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it; errorMessage = null },
                                    label = "Full Name",
                                    icon = Icons.Default.Person,
                                    imeAction = ImeAction.Next
                                )
                            }

                            // Common: Email / Phone
                            AuthTextField(
                                value = identifier,
                                onValueChange = { identifier = it; errorMessage = null },
                                label = "Email or Phone Number",
                                icon = Icons.Default.Mail,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )

                            // Common: Password
                            AuthTextField(
                                value = password,
                                onValueChange = { password = it; errorMessage = null },
                                label = "Password",
                                icon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onVisibilityToggle = { passwordVisible = !passwordVisible },
                                imeAction = if (mode == AuthMode.SIGNUP) ImeAction.Next else ImeAction.Done
                            )

                            // Signup Only: Confirm Password
                            if (mode == AuthMode.SIGNUP) {
                                AuthTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it; errorMessage = null },
                                    label = "Confirm Password",
                                    icon = Icons.Default.Lock,
                                    isPassword = true,
                                    passwordVisible = confirmPasswordVisible,
                                    onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                                    imeAction = ImeAction.Done
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // MFA / OTP Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2FA Verification",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { requestOTP() },
                            enabled = !isLoading && identifier.isNotBlank()
                        ) {
                            Text(if (isOtpSent) "Resend OTP" else "Request OTP")
                        }
                    }

                    AuthTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it.filter { ch -> ch.isDigit() }.take(6); errorMessage = null },
                        label = "6-Digit OTP Code",
                        icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                        onDone = { submitForm() },
                        enabled = isOtpSent
                    )
                }
            }

            // --- Forgot Password Link ---
            if (authMode == AuthMode.LOGIN) {
                TextButton(
                    onClick = onNavigateToForgotPassword,
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                ) {
                    Text("Forgot Password?")
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- Submit Action ---
            Button(
                onClick = { submitForm() },
                modifier = Modifier.fillMaxWidth().height(54.dp).testTag("login_submit_button"),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (authMode == AuthMode.LOGIN) "Secure Login" else "Create Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

// ============================================================================
// Internal Sub-Components
// ============================================================================

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onVisibilityToggle: () -> Unit = {},
    enabled: Boolean = true
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { onDone() }
        ),
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}

// ============================================================================
// UI Previews
// ============================================================================

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            onLoginSuccess = { _, _ -> },
            onNavigateToForgotPassword = {}
        )
    }
}
