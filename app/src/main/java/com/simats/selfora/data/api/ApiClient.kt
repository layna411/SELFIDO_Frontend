package com.simats.selfora.data.api

import com.simats.selfora.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Central HTTP & REST API Client for SELFORA.
 * Standardized across Emulator, Physical Device LAN, and Production environments.
 */
object ApiClient {

    @Volatile
    private var jwtToken: String? = null

    fun setJwtToken(token: String?) {
        this.jwtToken = token
    }

    fun getJwtToken(): String? = jwtToken

    /**
     * Preserves Bearer JWT authorization token injection into outbound requests.
     */
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        jwtToken?.let { token ->
            if (token.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
        }

        requestBuilder.header("Content-Type", "application/json")
        chain.proceed(requestBuilder.build())
    }

    /**
     * Preserves logging interceptor; enabled in debug builds, disabled in production.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    /**
     * Shared OkHttpClient instance preserving 15-second timeouts.
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var cachedRetrofit: Retrofit? = null

    @Volatile
    private var cachedApiService: SelforaApiService? = null

    /**
     * Returns the singleton SelforaApiService instance, lazily and thread-safely initialized.
     */
    val apiService: SelforaApiService
        get() {
            return cachedApiService ?: synchronized(this) {
                cachedApiService ?: buildRetrofit().create(SelforaApiService::class.java).also {
                    cachedApiService = it
                }
            }
        }

    /**
     * Returns the currently active Base URL string.
     */
    fun getBaseUrl(): String {
        return NetworkConfig.getBaseUrl()
    }

    /**
     * Builds and validates the Retrofit instance with proper trailing-slash handling.
     */
    private fun buildRetrofit(): Retrofit {
        val currentBaseUrl = NetworkConfig.getBaseUrl()
        require(NetworkConfig.isValidBaseUrl(currentBaseUrl)) {
            "Invalid base URL configured: '$currentBaseUrl'. URL must start with http:// or https:// and include a valid host."
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(NetworkConfig.ensureTrailingSlash(currentBaseUrl))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        cachedRetrofit = retrofit
        return retrofit
    }

    /**
     * Resets the cached Retrofit and ApiService instances.
     * Useful in development when switching between Emulator and LAN environments.
     */
    fun resetClient() {
        synchronized(this) {
            cachedRetrofit = null
            cachedApiService = null
        }
    }

    /**
     * Updates the base URL dynamically in debug mode.
     */
    fun updateBaseUrl(newUrl: String?) {
        if (!BuildConfig.DEBUG) return
        NetworkConfig.setCustomBaseUrl(newUrl)
        resetClient()
    }
}
