Here is the modernized version of your AuthManager.
I applied several modern Android and Kotlin best practices:
 * sealed interface: Upgraded AuthResult from a sealed class to a sealed interface (Kotlin standard for states).
 * DataStore: Replaced legacy SharedPreferences with modern Preferences DataStore and exposed the role reactively as a StateFlow.
 * callbackFlow for Phone Auth: Eliminated "callback hell" in sendPhoneOtp by converting it into a Kotlin Flow that your UI/ViewModel can cleanly collect.
 * Firebase KTX: Used modern Firebase Kotlin extensions (Firebase.auth).
 * Constructor Injection: Structured the class to be easily injectable (e.g., using Hilt/Dagger) by passing dependencies through the constructor with default values.
Modernized AuthManager.kt
package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

// 1. Use 'sealed interface' instead of 'sealed class' for modern state modeling
sealed interface AuthResult {
    data class Success(val user: FirebaseUser, val role: UserRole) : AuthResult
    data class CodeSent(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken?) : AuthResult
    data class Error(val message: String) : AuthResult
}

// DataStore extension at the top level
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AuthManager(
    private val context: Context,
    private val auth: FirebaseAuth = Firebase.auth, // Modern KTX property
    private val credentialManager: CredentialManager = CredentialManager.create(context)
) {
    companion object {
        private const val TAG = "AuthManager"
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    }

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // 2. Modern DataStore implementation exposing a StateFlow
    val userRole: StateFlow<UserRole> = context.dataStore.data
        .map { preferences ->
            val roleStr = preferences[USER_ROLE_KEY] ?: UserRole.CUSTOMER.name
            runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.CUSTOMER)
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = UserRole.CUSTOMER
        )

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    suspend fun saveRole(role: UserRole) {
        context.dataStore.edit { preferences ->
            preferences[USER_ROLE_KEY] = role.name
        }
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun signInWithGoogle(
        activity: Activity,
        serverClientId: String? = null
    ): AuthResult {
        return try {
            val clientId = serverClientId?.takeIf { it.isNotBlank() }
                ?: "992829853631.apps.googleusercontent.com"

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val result = credentialManager.getCredential(
                request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build(),
                context = activity
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val user = auth.signInWithCredential(authCredential).await().user
                
                if (user != null) AuthResult.Success(user, userRole.value)
                else AuthResult.Error("Failed to obtain Firebase user.")
            } else {
                AuthResult.Error("Unsupported credential type received.")
            }
        } catch (e: GetCredentialCancellationException) {
            AuthResult.Error("Google Sign-In was cancelled.")
        } catch (e: GetCredentialException) {
            Log.e(TAG, "CredentialManager error: ${e.message}", e)
            AuthResult.Error(e.localizedMessage ?: "Google Sign-In unavailable.")
        } catch (e: CancellationException) {
            throw e // Always rethrow CancellationExceptions in coroutines
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in unexpected error", e)
            AuthResult.Error(e.localizedMessage ?: "Authentication failed.")
        }
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult = try {
        val user = auth.signInWithEmailAndPassword(email.trim(), password).await().user
        if (user != null) AuthResult.Success(user, userRole.value)
        else AuthResult.Error("Failed to sign in. Please check credentials.")
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        AuthResult.Error(e.localizedMessage ?: "Email sign-in failed.")
    }

    suspend fun signUpWithEmail(
        email: String, 
        password: String, 
        displayName: String, 
        role: UserRole
    ): AuthResult = try {
        saveRole(role)
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user
        
        if (user != null) {
            if (displayName.isNotBlank()) {
                // Modern KTX builder for profile updates
                val profileUpdates = userProfileChangeRequest { 
                    this.displayName = displayName.trim() 
                }
                user.updateProfile(profileUpdates).await()
            }
            AuthResult.Success(user, role)
        } else {
            AuthResult.Error("Account creation failed.")
        }
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        AuthResult.Error(e.localizedMessage ?: "Sign up failed.")
    }

    // 3. Converted to a CallbackFlow to eliminate callback hell in UI layer
    fun sendPhoneOtp(
        activity: Activity,
        phoneNumber: String
    ): Flow<AuthResult> = callbackFlow {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Launch in the flow's scope to perform the async sign-in
                launch {
                    try {
                        val user = auth.signInWithCredential(credential).await().user
                        if (user != null) trySend(AuthResult.Success(user, userRole.value))
                        else trySend(AuthResult.Error("Auto verification failed: User is null."))
                    } catch (e: Exception) {
                        trySend(AuthResult.Error(e.localizedMessage ?: "Auto verification failed."))
                    }
                    close()
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                trySend(AuthResult.Error(e.localizedMessage ?: "Verification failed. Check phone format."))
                close()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                trySend(AuthResult.CodeSent(verificationId, token))
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber.trim())
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        // Suspend until flow is closed
        awaitClose { /* Cleanup if necessary */ }
    }

    suspend fun verifyPhoneOtp(
        verificationId: String,
        otpCode: String,
        role: UserRole
    ): AuthResult = try {
        saveRole(role)
        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode.trim())
        val user = auth.signInWithCredential(credential).await().user
        
        if (user != null) AuthResult.Success(user, role)
        else AuthResult.Error("Invalid OTP entered.")
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        AuthResult.Error(e.localizedMessage ?: "Verification failed.")
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }
}

