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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.UserRole
import com.example.ui.components.RoleSwitcherPill
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceTertiary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

private enum class AuthMode {
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD
}

@Composable
fun LoginScreen(
    onLoginSuccess: (phone: String, role: UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var fullName by remember { mutableStateOf("Akhil Sharma") }
    var email by remember { mutableStateOf("akhil.user@gmail.com") }
    var phone by remember { mutableStateOf("9999999999") }
    var password by remember { mutableStateOf("Demo@123") }
    var confirmPassword by remember { mutableStateOf("Demo@123") }
    var otpCode by remember { mutableStateOf("123456") }
    var isOtpSent by remember { mutableStateOf(true) }
    var statusMessage by remember { mutableStateOf("Demo OTP 123456 is ready for login testing.") }

    val isLoginMode = authMode == AuthMode.LOGIN
    val isSignupMode = authMode == AuthMode.SIGNUP
    val isForgotMode = authMode == AuthMode.FORGOT_PASSWORD
    val demoOtp = "123456"

    fun sendDemoOtp() {
        isOtpSent = true
        statusMessage = "Demo OTP sent to ${if (email.isNotBlank()) email else phone}. Use $demoOtp"
    }

    fun handleSubmit() {
        when (authMode) {
            AuthMode.LOGIN -> {
                val input = if (email.isNotBlank()) email else phone
                if (input.isBlank()) {
                    statusMessage = "Enter your email or phone number first."
                    return
                }
                if (password.length < 6) {
                    statusMessage = "Password must be at least 6 characters."
                    return
                }
                if (otpCode.isBlank()) {
                    statusMessage = "Enter the OTP or use the demo code 123456."
                    return
                }
                if (otpCode != demoOtp) {
                    statusMessage = "Invalid OTP. Demo OTP is 123456."
                    return
                }
                statusMessage = "Login successful. Welcome back!"
                onLoginSuccess(phone.ifBlank { email }, selectedRole)
            }
            AuthMode.SIGNUP -> {
                if (fullName.isBlank()) {
                    statusMessage = "Please enter your full name."
                    return
                }
                if (email.isBlank()) {
                    statusMessage = "Please enter your email address."
                    return
                }
                if (password.length < 6) {
                    statusMessage = "Password must be at least 6 characters."
                    return
                }
                if (password != confirmPassword) {
                    statusMessage = "Passwords do not match."
                    return
                }
                if (otpCode != demoOtp) {
                    statusMessage = "OTP verification failed. Use the demo OTP 123456."
                    return
                }
                statusMessage = "Account created successfully. Please log in now."
                authMode = AuthMode.LOGIN
                password = "Demo@123"
                confirmPassword = "Demo@123"
                otpCode = demoOtp
            }
            AuthMode.FORGOT_PASSWORD -> {
                if (email.isBlank() && phone.isBlank()) {
                    statusMessage = "Enter your email or phone number to reset the password."
                    return
                }
                if (otpCode != demoOtp) {
                    statusMessage = "Reset failed. Use the demo OTP 123456."
                    return
                }
                if (password.length < 6) {
                    statusMessage = "New password must be at least 6 characters."
                    return
                }
                if (password != confirmPassword) {
                    statusMessage = "New passwords do not match."
                    return
                }
                statusMessage = "Password reset successful. Please sign in with your new password."
                authMode = AuthMode.LOGIN
                password = "Demo@123"
                confirmPassword = "Demo@123"
                otpCode = demoOtp
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
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(AmberPrimary, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Akhil Logistics Logo",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "AKHIL LOGISTICS",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp,
                color = AmberPrimary
            )

            Text(
                text = "Smart Delivery Access",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Secure login for customers, drivers and support teams",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            Text(
                text = "Select Persona",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, bottom = 6.dp)
            )

            RoleSwitcherPill(
                currentRole = selectedRole,
                onRoleSelected = { role ->
                    selectedRole = role
                    when (role) {
                        UserRole.CUSTOMER -> {
                            fullName = "Akhil Sharma"
                            email = "akhil.user@gmail.com"
                            phone = "9999999999"
                            password = "Demo@123"
                            confirmPassword = "Demo@123"
                            otpCode = demoOtp
                        }
                        UserRole.DRIVER -> {
                            fullName = "Ravi Kumar"
                            email = "ravi.driver@gmail.com"
                            phone = "8888888888"
                            password = "Driver@123"
                            confirmPassword = "Driver@123"
                            otpCode = demoOtp
                        }
                        UserRole.ADMIN -> {
                            fullName = "Admin Ops"
                            email = "admin@akhillogistics.com"
                            phone = "7777777777"
                            password = "Admin@123"
                            confirmPassword = "Admin@123"
                            otpCode = demoOtp
                        }
                    }
                    if (authMode == AuthMode.LOGIN) {
                        sendDemoOtp()
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabColors = listOf(
                    Pair(AuthMode.LOGIN, "Login"),
                    Pair(AuthMode.SIGNUP, "Sign Up"),
                    Pair(AuthMode.FORGOT_PASSWORD, "Forgot")
                )

                tabColors.forEach { (mode, label) ->
                    val selected = authMode == mode
                    OutlinedButton(
                        onClick = {
                            authMode = mode
                            statusMessage = when (mode) {
                                AuthMode.LOGIN -> "Demo OTP 123456 is ready for login testing."
                                AuthMode.SIGNUP -> "Create your account and verify with demo OTP 123456."
                                AuthMode.FORGOT_PASSWORD -> "Use your email or phone to receive the email OTP."
                            }
                            if (mode != AuthMode.SIGNUP) {
                                otpCode = demoOtp
                            }
                            if (mode == AuthMode.LOGIN || mode == AuthMode.FORGOT_PASSWORD) {
                                isOtpSent = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) AmberContainer else Color.Transparent,
                            contentColor = if (selected) TextDark else TextMuted
                        )
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    if (isSignupMode) {
                        Text(
                            text = "Full Name",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = AmberPrimary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberPrimary,
                                unfocusedBorderColor = BorderLight,
                                focusedContainerColor = SurfaceTertiary,
                                unfocusedContainerColor = SurfaceTertiary
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Text(
                        text = if (isForgotMode) "Email or Phone" else "Email Address",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth().testTag("email_or_phone_input"),
                        placeholder = { Text(if (isForgotMode) "name@example.com or 9876543210" else "name@example.com", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = BorderLight,
                            focusedContainerColor = SurfaceTertiary,
                            unfocusedContainerColor = SurfaceTertiary
                        )
                    )

                    if (!isForgotMode) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Mobile Number",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it.filter { ch -> ch.isDigit() }.take(10) },
                            modifier = Modifier.fillMaxWidth().testTag("phone_input"),
                            leadingIcon = { Text("+91 ", fontWeight = FontWeight.Bold, color = TextDark) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberPrimary,
                                unfocusedBorderColor = BorderLight,
                                focusedContainerColor = SurfaceTertiary,
                                unfocusedContainerColor = SurfaceTertiary
                            )
                        )
                    }

                    if (!isForgotMode || isSignupMode) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isSignupMode) "Create Password" else "Password",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth().testTag("password_input"),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberPrimary) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberPrimary,
                                unfocusedBorderColor = BorderLight,
                                focusedContainerColor = SurfaceTertiary,
                                unfocusedContainerColor = SurfaceTertiary
                            )
                        )
                    }

                    if (isSignupMode || isForgotMode) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isSignupMode) "Confirm Password" else "New Password",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            modifier = Modifier.fillMaxWidth().testTag("confirm_password_input"),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberPrimary) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberPrimary,
                                unfocusedBorderColor = BorderLight,
                                focusedContainerColor = SurfaceTertiary,
                                unfocusedContainerColor = SurfaceTertiary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isForgotMode) "Reset OTP" else "Verification Code (OTP)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it.filter { ch -> ch.isDigit() }.take(6) },
                        modifier = Modifier.fillMaxWidth().testTag("otp_input"),
                        placeholder = { Text("Enter 6-digit OTP", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = BorderLight,
                            focusedContainerColor = SurfaceTertiary,
                            unfocusedContainerColor = SurfaceTertiary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Secure",
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = statusMessage,
                                fontSize = 11.sp,
                                color = SuccessGreen,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                        TextButton(onClick = { sendDemoOtp() }) {
                            Text("Send OTP", fontWeight = FontWeight.Bold, color = AmberPrimary)
                        }
                    }

                    if (authMode == AuthMode.LOGIN && isOtpSent) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Demo OTP: $demoOtp",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoginMode) {
                Text(
                    text = "Quick Demo Logins",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedRole = UserRole.CUSTOMER
                            phone = "9999999999"
                            email = "akhil.user@gmail.com"
                            password = "Demo@123"
                            otpCode = demoOtp
                            statusMessage = "Customer demo credentials ready."
                            onLoginSuccess(phone, UserRole.CUSTOMER)
                        },
                        modifier = Modifier.weight(1f).testTag("quick_customer_login"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Customer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            selectedRole = UserRole.DRIVER
                            phone = "8888888888"
                            email = "ravi.driver@gmail.com"
                            password = "Driver@123"
                            otpCode = demoOtp
                            statusMessage = "Driver demo credentials ready."
                            onLoginSuccess(phone, UserRole.DRIVER)
                        },
                        modifier = Modifier.weight(1f).testTag("quick_driver_login"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Driver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            selectedRole = UserRole.ADMIN
                            phone = "7777777777"
                            email = "admin@akhillogistics.com"
                            password = "Admin@123"
                            otpCode = demoOtp
                            statusMessage = "Admin demo credentials ready."
                            onLoginSuccess(phone, UserRole.ADMIN)
                        },
                        modifier = Modifier.weight(1f).testTag("quick_admin_login"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { handleSubmit() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("login_submit_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when (authMode) {
                        AuthMode.LOGIN -> "Verify & Access Platform"
                        AuthMode.SIGNUP -> "Create Account"
                        AuthMode.FORGOT_PASSWORD -> "Reset Password"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}
