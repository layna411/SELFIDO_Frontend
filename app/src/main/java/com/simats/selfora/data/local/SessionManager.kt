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

    fun saveSession(role: String, userId: Long, username: String = "", token: String = "") {
        if (!::prefs.isInitialized) return
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ROLE, role)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_JWT_TOKEN, token)
            apply()
        }
        if (token.isNotBlank()) {
            ApiClient.setJwtToken(token)
        }
    }

    fun isLoggedIn(): Boolean {
        if (!::prefs.isInitialized) return false
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserRole(): String {
        if (!::prefs.isInitialized) return "ROLE_THERAPIST"
        return prefs.getString(KEY_USER_ROLE, "ROLE_THERAPIST") ?: "ROLE_THERAPIST"
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

    fun clearSession() {
        if (!::prefs.isInitialized) return
        prefs.edit().clear().apply()
        ApiClient.setJwtToken(null)
    }
}
