package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSession(
    val uid: String,
    val email: String,
    val phone: String,
    val displayName: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
)

class SessionManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    
    private val _currentSession = MutableStateFlow<UserSession?>(loadSession())
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(loadSession() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun saveSession(session: UserSession) {
        prefs.edit().apply {
            putString("uid", session.uid)
            putString("email", session.email)
            putString("phone", session.phone)
            putString("displayName", session.displayName)
            putString("role", session.role.name)
            putLong("createdAt", session.createdAt)
            apply()
        }
        _currentSession.value = session
        _isLoggedIn.value = true
    }

    private fun loadSession(): UserSession? {
        val uid = prefs.getString("uid", null) ?: return null
        val email = prefs.getString("email", "") ?: ""
        val phone = prefs.getString("phone", "") ?: ""
        val displayName = prefs.getString("displayName", "") ?: ""
        val roleStr = prefs.getString("role", UserRole.CUSTOMER.name) ?: UserRole.CUSTOMER.name
        val createdAt = prefs.getLong("createdAt", System.currentTimeMillis())
        
        val role = try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.CUSTOMER
        }
        
        return UserSession(uid, email, phone, displayName, role, createdAt)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _currentSession.value = null
        _isLoggedIn.value = false
    }

    fun updateRole(role: UserRole) {
        val current = _currentSession.value ?: return
        val updated = current.copy(role = role)
        saveSession(updated)
    }
}
