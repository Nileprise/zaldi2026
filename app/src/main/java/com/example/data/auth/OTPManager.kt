package com.example.data.auth

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

sealed class OTPResult {
    data class Success(val message: String) : OTPResult()
    data class Error(val message: String) : OTPResult()
    object Loading : OTPResult()
}

class OTPManager {
    private val _otpState = MutableStateFlow<OTPResult>(OTPResult.Success("Ready"))
    val otpState: StateFlow<OTPResult> = _otpState.asStateFlow()

    private val sentOTPs = mutableMapOf<String, String>()
    private val otpExpiryTime = mutableMapOf<String, Long>()
    private val OTP_VALIDITY_MINUTES = 5
    private val DEMO_MODE = true // Set to false for production

    fun generateAndSendOTP(email: String, phone: String = ""): String {
        return try {
            _otpState.value = OTPResult.Loading
            
            val otp = generateOTP()
            val identifier = email.ifBlank { phone }
            
            sentOTPs[identifier] = otp
            otpExpiryTime[identifier] = System.currentTimeMillis() + (OTP_VALIDITY_MINUTES * 60 * 1000)
            
            if (DEMO_MODE) {
                _otpState.value = OTPResult.Success("Demo OTP: $otp sent to $identifier")
                Log.d("OTPManager", "Demo OTP for $identifier: $otp")
            } else {
                // In production, send via email service or Firebase SMS
                sendOTPViaEmail(email, otp)
                _otpState.value = OTPResult.Success("OTP sent to $email")
            }
            
            otp
        } catch (e: Exception) {
            _otpState.value = OTPResult.Error(e.localizedMessage ?: "Failed to send OTP")
            ""
        }
    }

    fun verifyOTP(email: String, phone: String = "", otp: String): Boolean {
        return try {
            val identifier = email.ifBlank { phone }
            val storedOTP = sentOTPs[identifier]
            val expiryTime = otpExpiryTime[identifier]
            
            if (storedOTP == null) {
                _otpState.value = OTPResult.Error("No OTP found for $identifier")
                return false
            }
            
            if (System.currentTimeMillis() > expiryTime ?: 0) {
                _otpState.value = OTPResult.Error("OTP expired. Please request a new one.")
                sentOTPs.remove(identifier)
                return false
            }
            
            if (storedOTP != otp) {
                _otpState.value = OTPResult.Error("Invalid OTP. Please try again.")
                return false
            }
            
            _otpState.value = OTPResult.Success("OTP verified successfully")
            sentOTPs.remove(identifier)
            otpExpiryTime.remove(identifier)
            true
        } catch (e: Exception) {
            _otpState.value = OTPResult.Error(e.localizedMessage ?: "Verification failed")
            false
        }
    }

    fun resendOTP(email: String, phone: String = ""): String {
        return generateAndSendOTP(email, phone)
    }

    private fun generateOTP(): String {
        return (100000 + Random.nextInt(900000)).toString()
    }

    private fun sendOTPViaEmail(email: String, otp: String) {
        // TODO: Implement email sending via Firebase Functions or third-party service
        // Example: Firebase Cloud Functions endpoint call
        Log.d("OTPManager", "Email OTP would be sent to $email: $otp")
    }

    fun clearOTP(email: String, phone: String = "") {
        val identifier = email.ifBlank { phone }
        sentOTPs.remove(identifier)
        otpExpiryTime.remove(identifier)
    }
}
