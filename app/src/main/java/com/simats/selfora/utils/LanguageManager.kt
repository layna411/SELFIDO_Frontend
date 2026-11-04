package com.simats.selfora.utils

import android.content.Context
import android.content.res.Configuration
import com.simats.selfora.data.local.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

object LanguageManager {
    private var appContext: Context? = null
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        val saved = SessionManager.getUserLanguage()
        _currentLanguage.value = saved
        applyLocale(context, saved)
    }

    fun setLanguage(languageCode: String, context: Context? = appContext, userId: Long = SessionManager.getUserId()) {
        val normalized = if (languageCode.equals("ta", ignoreCase = true)) "ta" else "en"
        SessionManager.setUserLanguage(normalized, userId)
        _currentLanguage.value = normalized
        val targetContext = context ?: appContext
        targetContext?.let { applyLocale(it, normalized) }
    }

    fun toggleLanguage(context: Context? = appContext, userId: Long = SessionManager.getUserId()): String {
        val newLang = if (_currentLanguage.value == "ta") "en" else "ta"
        setLanguage(newLang, context, userId)
        return newLang
    }

    fun applyLocale(context: Context, languageCode: String) {
        val locale = if (languageCode.equals("ta", ignoreCase = true)) {
            Locale("ta", "IN")
        } else {
            Locale.US
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun isTamil(): Boolean = _currentLanguage.value == "ta"
}
