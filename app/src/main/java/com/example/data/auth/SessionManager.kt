Here is the modernized, professional version of your SessionManager.
I have applied the following modern Android architecture practices:
 * Preferences DataStore: Replaced the legacy, synchronous SharedPreferences with DataStore. DataStore is fully asynchronous, thread-safe, and designed specifically to work natively with Kotlin Flows.
 * Single Source of Truth: Instead of manually maintaining MutableStateFlows that can get out of sync with storage, the states (currentSession and isLoggedIn) are now reactively derived directly from the DataStore using .map {} and .stateIn().
 * Async Writes (suspend): All write operations (saveSession, clearSession) are now suspend functions to ensure disk I/O doesn't block the main UI thread.
 * Exception Handling: Added standard .catch {} block to handle disk IOExceptions gracefully, which is a DataStore best practice.
Modernized SessionManager.kt
package com.example.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException

data class UserSession(
    val uid: String,
    val email: String,
    val phone: String,
    val displayName: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
)

// 1. Top-level DataStore delegate (Single instance per app)
private val Context.sessionDataStore by preferencesDataStore(name = "user_session")

class SessionManager(
    private val context: Context,
    // 2. Pass a CoroutineScope (typically an Application scope via Dependency Injection)
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        // 3. Type-safe Preferences Keys
        private val UID_KEY = stringPreferencesKey("uid")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val PHONE_KEY = stringPreferencesKey("phone")
        private val DISPLAY_NAME_KEY = stringPreferencesKey("displayName")
        private val ROLE_KEY = stringPreferencesKey("role")
        private val CREATED_AT_KEY = longPreferencesKey("createdAt")
    }

    // 4. Reactive StateFlow directly tied to disk storage (Single Source of Truth)
    val currentSession: StateFlow<UserSession?> = context.sessionDataStore.data
        .catch { exception ->
            // Handle standard IO exceptions during file read
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val uid = preferences[UID_KEY]
            
            if (uid == null) {
                null
            } else {
                UserSession(
                    uid = uid,
                    email = preferences[EMAIL_KEY] ?: "",
                    phone = preferences[PHONE_KEY] ?: "",
                    displayName = preferences[DISPLAY_NAME_KEY] ?: "",
                    role = runCatching { UserRole.valueOf(preferences[ROLE_KEY] ?: "") }
                        .getOrDefault(UserRole.CUSTOMER),
                    createdAt = preferences[CREATED_AT_KEY] ?: System.currentTimeMillis()
                )
            }
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    // Derived state, automatically updates when currentSession updates
    val isLoggedIn: StateFlow<Boolean> = currentSession
        .map { it != null }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    // 5. Suspend functions for all writes (Prevents blocking the Main Thread)
    suspend fun saveSession(session: UserSession) {
        context.sessionDataStore.edit { prefs ->
            prefs[UID_KEY] = session.uid
            prefs[EMAIL_KEY] = session.email
            prefs[PHONE_KEY] = session.phone
            prefs[DISPLAY_NAME_KEY] = session.displayName
            prefs[ROLE_KEY] = session.role.name
            prefs[CREATED_AT_KEY] = session.createdAt
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { it.clear() }
    }

    suspend fun updateRole(role: UserRole) {
        context.sessionDataStore.edit { prefs ->
            // Only update the role if a session currently exists
            if (prefs.contains(UID_KEY)) {
                prefs[ROLE_KEY] = role.name
            }
        }
    }
}

