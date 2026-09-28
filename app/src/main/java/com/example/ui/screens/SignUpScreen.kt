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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceTertiary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@Composable
fun SignUpScreen(
    selectedRole: UserRole = UserRole.CUSTOMER,
    onRoleSelected: (UserRole) -> Unit = {},
    onSignUpSuccess: (email: String, password: String, displayName: String, role: UserRole) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    fun validateForm(): Boolean {
        return when {
            displayName.isBlank() -> {
                errorMessage = "Full name is required"
                false
            }
            email.isBlank() -> {
                errorMessage = "Email is required"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                errorMessage = "Invalid email format"
                false
            }
            phone.isBlank() || phone.length < 10 -> {
                errorMessage = "Valid phone number required (10+ digits)"
                false
            }
            password.isBlank() -> {
                errorMessage = "Password is required"
                false
            }
            password.length < 6 -> {
                errorMessage = "Password must be at least 6 characters"
                false
            }
            password != confirmPassword -> {
                errorMessage = "Passwords do not match"
                false
            }
            else -> {
                errorMessage = ""
                true
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
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToLogin) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Create Account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Join as ${selectedRole.name.lowercase()}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Full Name
                Text(
                    text = "Full Name",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    modifier = Modifier.fillMaxWidth().testTag("signup_name_input"),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AmberPrimary) },
                    placeholder = { Text("John Doe", color = TextMuted) },
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

                // Email
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
                    modifier = Modifier.fillMaxWidth().testTag("signup_email_input"),
                    leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = AmberPrimary) },
                    placeholder = { Text("john@example.com", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

                // Phone
                Text(
                    text = "Phone Number",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { ch -> ch.isDigit() }.take(10) },
                    modifier = Modifier.fillMaxWidth().testTag("signup_phone_input"),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AmberPrimary) },
                    placeholder = { Text("9876543210", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

                // Password
                Text(
                    text = "Create Password",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth().testTag("signup_password_input"),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberPrimary) },
                    placeholder = { Text("Min 6 characters", color = TextMuted) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

                // Confirm Password
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
                    modifier = Modifier.fillMaxWidth().testTag("signup_confirm_password_input"),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberPrimary) },
                    placeholder = { Text("Re-enter password", color = TextMuted) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = BorderLight,
                        focusedContainerColor = SurfaceTertiary,
                        unfocusedContainerColor = SurfaceTertiary
                    )
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 11.sp,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Submit Button
        Button(
            onClick = {
                if (validateForm()) {
                    isLoading = true
                    onSignUpSuccess(email, password, displayName, selectedRole)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("signup_submit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Create Account", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Back to Login
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ", fontSize = 12.sp, color = TextMuted)
            TextButton(onClick = onBackToLogin) {
                Text("Log In", fontWeight = FontWeight.Bold, color = AmberPrimary, fontSize = 12.sp)
            }
        }
    }
}
