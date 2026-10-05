package com.simats.selfora.data.local

import android.content.Context
import android.content.SharedPreferences
import com.simats.selfora.data.api.ApiClient

object SessionManager {
    private const val PREF_NAME = "selfora_user_session"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_JWT_TOKEN = "jwt_token"
    private const val KEY_MUST_CHANGE_PASSWORD = "must_change_password"

    // Child Context & Child Mode Keys
    private const val KEY_ACTIVE_CHILD_ID = "active_child_id"
    private const val KEY_ACTIVE_CHILD_NAME = "active_child_name"
    private const val KEY_ACTIVE_CHILD_GENDER = "active_child_gender"
    private const val KEY_IS_CHILD_MODE_ACTIVE = "is_child_mode_active"
    private const val KEY_PARENT_ADULT_ROLE = "parent_adult_role"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val savedToken = getJwtToken()
            if (!savedToken.isNullOrBlank()) {
                ApiClient.setJwtToken(savedToken)
            }
        }
    }

    fun saveSession(role: String, userId: Long, username: String = "", token: String = "", mustChangePassword: Boolean = false) {
        if (!::prefs.isInitialized) return
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ROLE, role)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_JWT_TOKEN, token)
            putBoolean(KEY_MUST_CHANGE_PASSWORD, mustChangePassword)
            apply()
        }
        if (token.isNotBlank()) {
            ApiClient.setJwtToken(token)
        }
    }

    fun mustChangePassword(): Boolean {
        if (!::prefs.isInitialized) return false
        return prefs.getBoolean(KEY_MUST_CHANGE_PASSWORD, false)
    }

    fun setMustChangePassword(mustChange: Boolean) {
        if (!::prefs.isInitialized) return
        prefs.edit().putBoolean(KEY_MUST_CHANGE_PASSWORD, mustChange).apply()
    }

    fun isLoggedIn(): Boolean {
        if (!::prefs.isInitialized) return false
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserRole(): String {
        if (!::prefs.isInitialized) return "ROLE_THERAPIST"
        return prefs.getString(KEY_USER_ROLE, "ROLE_THERAPIST") ?: "ROLE_THERAPIST"
    }

    fun isSuperAdmin(): Boolean {
        return getUserRole() == "ROLE_SUPER_ADMIN"
    }

    fun getUserId(): Long {
        if (!::prefs.isInitialized) return 1L
        return prefs.getLong(KEY_USER_ID, 1L)
    }

    fun getUsername(): String {
        if (!::prefs.isInitialized) return ""
        return prefs.getString(KEY_USERNAME, "") ?: ""
    }

    fun getJwtToken(): String? {
        if (!::prefs.isInitialized) return null
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    // ==========================================
    // Child Context & Child Mode Management
    // ==========================================
    fun saveActiveChildContext(childId: Long, childName: String, gender: String = "BOY") {
        if (!::prefs.isInitialized) return
        prefs.edit().apply {
            putLong(KEY_ACTIVE_CHILD_ID, childId)
            putString(KEY_ACTIVE_CHILD_NAME, childName)
            putString(KEY_ACTIVE_CHILD_GENDER, gender)
            apply()
        }
    }

    fun getActiveChildId(): Long {
        if (!::prefs.isInitialized) return 1L
        return prefs.getLong(KEY_ACTIVE_CHILD_ID, 1L)
    }

    fun getActiveChildName(): String {
        if (!::prefs.isInitialized) return "Aarav Sharma"
        return prefs.getString(KEY_ACTIVE_CHILD_NAME, "Aarav Sharma") ?: "Aarav Sharma"
    }

    fun getActiveChildGender(): String {
        if (!::prefs.isInitialized) return "BOY"
        return prefs.getString(KEY_ACTIVE_CHILD_GENDER, "BOY") ?: "BOY"
    }

    fun enterChildMode(adultRole: String, childId: Long, childName: String, gender: String = "BOY") {
        if (!::prefs.isInitialized) return
        prefs.edit().apply {
            putBoolean(KEY_IS_CHILD_MODE_ACTIVE, true)
            putString(KEY_PARENT_ADULT_ROLE, adultRole)
            putLong(KEY_ACTIVE_CHILD_ID, childId)
            putString(KEY_ACTIVE_CHILD_NAME, childName)
            putString(KEY_ACTIVE_CHILD_GENDER, gender)
            apply()
        }
    }

    fun exitChildMode(): String {
        if (!::prefs.isInitialized) return "ROLE_THERAPIST"
        val adultRole = prefs.getString(KEY_PARENT_ADULT_ROLE, getUserRole()) ?: getUserRole()
        prefs.edit().apply {
            putBoolean(KEY_IS_CHILD_MODE_ACTIVE, false)
            remove(KEY_PARENT_ADULT_ROLE)
            apply()
        }
        return adultRole
    }

    fun isChildModeActive(): Boolean {
        if (!::prefs.isInitialized) return false
        return prefs.getBoolean(KEY_IS_CHILD_MODE_ACTIVE, false)
    }

    fun clearSession() {
        if (!::prefs.isInitialized) return
        prefs.edit().clear().apply()
        ApiClient.setJwtToken(null)
    }
}
