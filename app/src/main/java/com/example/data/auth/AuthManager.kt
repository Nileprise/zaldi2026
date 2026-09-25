package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class AuthResult {
    data class Success(val user: FirebaseUser, val role: UserRole) : AuthResult()
    data class CodeSent(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userRole = MutableStateFlow(loadSavedRole())
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    private fun loadSavedRole(): UserRole {
        val roleStr = prefs.getString("user_role", UserRole.CUSTOMER.name)
        return try {
            UserRole.valueOf(roleStr ?: UserRole.CUSTOMER.name)
        } catch (_: Exception) {
            UserRole.CUSTOMER
        }
    }

    fun saveRole(role: UserRole) {
        _userRole.value = role
        prefs.edit().putString("user_role", role.name).apply()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun signInWithGoogle(
        activity: Activity,
        serverClientId: String? = null
    ): AuthResult {
        return try {
            // Default Web Client ID if provided, otherwise standard Google ID option
            val clientId = serverClientId?.takeIf { it.isNotBlank() }
                ?: "992829853631.apps.googleusercontent.com"

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    AuthResult.Success(user, _userRole.value)
                } else {
                    AuthResult.Error("Failed to obtain Firebase user.")
                }
            } else {
                AuthResult.Error("Unsupported credential type received.")
            }
        } catch (e: GetCredentialCancellationException) {
            AuthResult.Error("Google Sign-In was cancelled.")
        } catch (e: GetCredentialException) {
            Log.e("AuthManager", "CredentialManager error: ${e.message}", e)
            AuthResult.Error(
                e.localizedMessage ?: "Google Sign-In unavailable. Please use Email or Phone authentication."
            )
        } catch (e: Exception) {
            Log.e("AuthManager", "Google sign-in unexpected error: ${e.message}", e)
            AuthResult.Error(e.localizedMessage ?: "Authentication failed.")
        }
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user, _userRole.value)
            } else {
                AuthResult.Error("Failed to sign in. Please check credentials.")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Email sign-in failed.")
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String, role: UserRole): AuthResult {
        return try {
            saveRole(role)
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
            if (user != null) {
                if (displayName.isNotBlank()) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName.trim())
                        .build()
                    user.updateProfile(profileUpdates).await()
                }
                AuthResult.Success(user, role)
            } else {
                AuthResult.Error("Account creation failed.")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Sign up failed.")
        }
    }

    fun sendPhoneOtp(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationCompleted: (user: FirebaseUser) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        authResult.user?.let(onVerificationCompleted)
                    }
                    .addOnFailureListener { e ->
                        onError(e.localizedMessage ?: "Auto verification failed.")
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e.localizedMessage ?: "Verification failed. Check phone format.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber.trim())
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyPhoneOtp(
        verificationId: String,
        otpCode: String,
        role: UserRole
    ): AuthResult {
        return try {
            saveRole(role)
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode.trim())
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                AuthResult.Success(user, role)
            } else {
                AuthResult.Error("Invalid OTP entered.")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Verification failed.")
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }
}
