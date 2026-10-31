package com.simats.selfora.data.api

import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Standardized network result and error classification for SELFORA.
 * Distinguishes communication failures cleanly without leaking raw stack traces.
 */
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(
        val type: NetworkErrorType,
        val userFriendlyMessage: String,
        val statusCode: Int? = null,
        val cause: Throwable? = null
    ) : NetworkResult<Nothing>()
}

enum class NetworkErrorType {
    BACKEND_UNREACHABLE,
    CONNECTION_TIMEOUT,
    INVALID_BASE_URL,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    SERVER_ERROR,
    CLIENT_ERROR,
    UNKNOWN_NETWORK_ERROR
}

object NetworkErrorHandler {

    /**
     * Converts a throwable to a secure, user-friendly error message.
     */
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is ConnectException, is UnknownHostException ->
                "Unable to reach the SELFORA server. Please verify your network connection and ensure the backend is running."
            is SocketTimeoutException ->
                "The server took too long to respond (connection timed out). Please try again."
            is MalformedURLException, is IllegalArgumentException ->
                "Invalid server address configuration. Please check your network settings."
            is IOException ->
                "A network error occurred while communicating with the server. Please check your connection."
            else ->
                "An unexpected network error occurred. Please try again later."
        }
    }

    /**
     * Identifies the category of network error from a throwable.
     */
    fun getErrorType(throwable: Throwable): NetworkErrorType {
        return when (throwable) {
            is ConnectException, is UnknownHostException -> NetworkErrorType.BACKEND_UNREACHABLE
            is SocketTimeoutException -> NetworkErrorType.CONNECTION_TIMEOUT
            is MalformedURLException, is IllegalArgumentException -> NetworkErrorType.INVALID_BASE_URL
            else -> NetworkErrorType.UNKNOWN_NETWORK_ERROR
        }
    }

    /**
     * Maps HTTP status codes to user-friendly messages.
     */
    fun getHttpErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            401 -> "Session expired or invalid credentials. Please log in again."
            403 -> "Access denied. You do not have permission to perform this action."
            404 -> "The requested clinical resource could not be found on the server."
            in 500..599 -> "The server encountered an error processing your request. Please try again later."
            else -> "Request failed (HTTP $statusCode). Please try again."
        }
    }

    /**
     * Maps HTTP status codes to category types.
     */
    fun getHttpErrorType(statusCode: Int): NetworkErrorType {
        return when (statusCode) {
            401 -> NetworkErrorType.UNAUTHORIZED
            403 -> NetworkErrorType.FORBIDDEN
            404 -> NetworkErrorType.NOT_FOUND
            in 500..599 -> NetworkErrorType.SERVER_ERROR
            else -> NetworkErrorType.CLIENT_ERROR
        }
    }

    /**
     * Safe wrapper for Retrofit API calls returning a structured NetworkResult.
     */
    inline fun <T> safeApiCall(call: () -> Response<T>): NetworkResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                val code = response.code()
                NetworkResult.Error(
                    type = getHttpErrorType(code),
                    userFriendlyMessage = getHttpErrorMessage(code),
                    statusCode = code
                )
            }
        } catch (e: Throwable) {
            NetworkResult.Error(
                type = getErrorType(e),
                userFriendlyMessage = getErrorMessage(e),
                cause = e
            )
        }
    }
}

