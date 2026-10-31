package com.simats.selfora.data.api

import android.os.Build
import com.simats.selfora.BuildConfig
import com.simats.selfora.data.local.SessionManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Standardized network and environment configuration for the SELFORA Android application.
 * Manages clean transitions across Android Studio Emulator, Physical Device LAN, and Production.
 */
object NetworkConfig {

    enum class Environment {
        EMULATOR,
        PHYSICAL_LAN,
        PRODUCTION,
        CUSTOM
    }

    /**
     * Determines whether the current execution runtime is an Android Studio Emulator or Genymotion.
     */
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"))
    }

    /**
     * Guarantees that the base URL has a terminating forward slash as required by Retrofit.
     */
    fun ensureTrailingSlash(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    /**
     * Validates that the supplied URL has a valid scheme (http/https) and non-empty host.
     */
    fun isValidBaseUrl(url: String): Boolean {
        if (url.isBlank()) return false
        val parsed = url.toHttpUrlOrNull() ?: return false
        return (parsed.scheme == "http" || parsed.scheme == "https") && parsed.host.isNotBlank()
    }

    /**
     * Returns the currently active Environment descriptor.
     */
    fun getActiveEnvironment(): Environment {
        if (!BuildConfig.DEBUG) {
            return Environment.PRODUCTION
        }
        val customUrl = SessionManager.getCustomBaseUrl()
        if (!customUrl.isNullOrBlank() && isValidBaseUrl(customUrl)) {
            return Environment.CUSTOM
        }
        return if (isEmulator()) {
            Environment.EMULATOR
        } else {
            Environment.PHYSICAL_LAN
        }
    }

    /**
     * Resolves the appropriate base URL based on build configuration and runtime context:
     * - Production (Release): Strictly returns BuildConfig.PROD_BASE_URL (or BuildConfig.BASE_URL)
     * - Debug Custom: Uses user-configured LAN base URL if provided
     * - Debug Emulator: Automatically selects BuildConfig.EMULATOR_BASE_URL (http://10.0.2.2:8080/)
     * - Debug Physical Device: Uses configured LAN URL (BuildConfig.DEFAULT_LAN_BASE_URL or fallback)
     */
    fun getBaseUrl(): String {
        // In Release builds, enforce production URL strictly without runtime overrides
        if (!BuildConfig.DEBUG) {
            val prodUrl = BuildConfig.BASE_URL
            return ensureTrailingSlash(prodUrl)
        }

        // In Debug builds:
        // 1. Runtime override (e.g. tester specified physical device LAN IP)
        val customUrl = SessionManager.getCustomBaseUrl()
        if (!customUrl.isNullOrBlank() && isValidBaseUrl(customUrl)) {
            return ensureTrailingSlash(customUrl)
        }

        // 2. Automated Emulator Detection
        if (isEmulator()) {
            return ensureTrailingSlash(BuildConfig.EMULATOR_BASE_URL)
        }

        // 3. Physical device LAN default from BuildConfig
        val defaultUrl = BuildConfig.BASE_URL
        return if (isValidBaseUrl(defaultUrl)) {
            ensureTrailingSlash(defaultUrl)
        } else {
            ensureTrailingSlash(BuildConfig.EMULATOR_BASE_URL)
        }
    }

    /**
     * Updates the custom LAN base URL at runtime. Restricted to DEBUG builds only.
     */
    fun setCustomBaseUrl(url: String?) {
        if (!BuildConfig.DEBUG) return
        if (url.isNullOrBlank()) {
            SessionManager.setCustomBaseUrl(null)
        } else {
            val formatted = ensureTrailingSlash(url)
            require(isValidBaseUrl(formatted)) {
                "Malformed base URL: '$url'. Must start with http:// or https:// and include a valid host."
            }
            SessionManager.setCustomBaseUrl(formatted)
        }
    }
}

