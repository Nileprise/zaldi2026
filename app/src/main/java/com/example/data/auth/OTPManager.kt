Here is the modernized, professional version of your OTPManager.
I have applied several critical updates for a production-ready environment:
 * Security (SecureRandom): Replaced the standard Random with java.security.SecureRandom. Standard random number generators are predictable and should never be used for security tokens like OTPs.
 * Thread Safety (ConcurrentHashMap): Replaced standard mutableMapOf with ConcurrentHashMap. If multiple coroutines or threads request/verify OTPs simultaneously, standard maps will crash with a ConcurrentModificationException.
 * Modern Kotlin Syntax: Upgraded sealed class to sealed interface, used Kotlin 1.9+ data object for the Loading/Idle states, and used standard CamelCase naming (Otp instead of OTP).
 * Asynchronous Operations (suspend): Changed the send methods to suspend functions and moved them to the Dispatchers.IO thread. In a real app, sending an email/SMS is a network call that must not block the main thread.
 * Kotlin Result API: Replaced arbitrary String returns with Kotlin's idiomatic Result<String> and Result<Unit> to properly encapsulate successes and failures for the caller.
Modernized OtpManager.kt
package com.example.data.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap

// 1. Use 'sealed interface' and 'data object' (Kotlin 1.9+)
sealed interface OtpState {
    data object Idle : OtpState
    data object Loading : OtpState
    data class Success(val message: String) : OtpState
    data class Error(val message: String) : OtpState
}

class OtpManager(
    private val isDemoMode: Boolean = true // Inject via DI/BuildConfig in production
) {
    companion object {
        private const val TAG = "OtpManager"
        private const val OTP_VALIDITY_MILLIS = 5 * 60 * 1000L // 5 minutes
    }

    private val _otpState = MutableStateFlow<OtpState>(OtpState.Idle)
    val otpState: StateFlow<OtpState> = _otpState.asStateFlow()

    // 2. Use ConcurrentHashMap for thread safety in async environments
    private val sentOtps = ConcurrentHashMap<String, String>()
    private val otpExpiryTimes = ConcurrentHashMap<String, Long>()

    // 3. Cryptographically secure random number generator for OTPs
    private val secureRandom = SecureRandom()

    /**
     * Generates and sends an OTP. 
     * Uses suspend to prevent blocking the caller during network operations.
     */
    suspend fun generateAndSendOtp(
        email: String, 
        phone: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val identifier = resolveIdentifier(email, phone)
            require(identifier.isNotBlank()) { "Email or phone must be provided" }

            _otpState.value = OtpState.Loading
            
            val otp = generateSecureOtp()
            
            sentOtps[identifier] = otp
            otpExpiryTimes[identifier] = System.currentTimeMillis() + OTP_VALIDITY_MILLIS
            
            if (isDemoMode) {
                _otpState.value = OtpState.Success("Demo OTP: $otp sent to $identifier")
                Log.d(TAG, "Demo OTP for $identifier: $otp")
            } else {
                // Perform actual network call here
                sendOtpViaEmail(email, otp)
                _otpState.value = OtpState.Success("OTP sent to $identifier")
            }
            
            Result.success(otp)
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Failed to send OTP"
            _otpState.value = OtpState.Error(errorMsg)
            Result.failure(e)
        }
    }

    fun verifyOtp(email: String, phone: String = "", otp: String): Boolean {
        val identifier = resolveIdentifier(email, phone)
        
        if (identifier.isBlank()) {
            _otpState.value = OtpState.Error("Invalid identifier provided.")
            return false
        }

        val storedOtp = sentOtps[identifier]
        val expiryTime = otpExpiryTimes[identifier] ?: 0L
        
        return when {
            storedOtp == null -> {
                _otpState.value = OtpState.Error("No OTP found for $identifier.")
                false
            }
            System.currentTimeMillis() > expiryTime -> {
                _otpState.value = OtpState.Error("OTP expired. Please request a new one.")
                clearOtp(identifier)
                false
            }
            storedOtp != otp.trim() -> {
                _otpState.value = OtpState.Error("Invalid OTP. Please try again.")
                false
            }
            else -> {
                _otpState.value = OtpState.Success("OTP verified successfully.")
                clearOtp(identifier)
                true
            }
        }
    }

    suspend fun resendOtp(email: String, phone: String = ""): Result<String> {
        return generateAndSendOtp(email, phone)
    }

    fun clearOtp(email: String, phone: String = "") {
        val identifier = resolveIdentifier(email, phone)
        if (identifier.isNotBlank()) {
            sentOtps.remove(identifier)
            otpExpiryTimes.remove(identifier)
        }
    }

    // Reset state to Idle when leaving the screen or retrying
    fun resetState() {
        _otpState.value = OtpState.Idle
    }

    private fun resolveIdentifier(email: String, phone: String): String {
        return email.trim().ifBlank { phone.trim() }
    }

    private fun generateSecureOtp(): String {
        // Generates a random number between 100000 and 999999 securely
        val number = secureRandom.nextInt(900000) + 100000
        return number.toString()
    }

    private suspend fun sendOtpViaEmail(email: String, otp: String) {
        // TODO: Implement actual email sending API call (e.g., Retrofit / Firebase Functions)
        Log.d(TAG, "Simulating network call to send Email OTP to $email: $otp")
    }
}

