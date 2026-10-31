package com.simats.selfora

import com.simats.selfora.data.api.NetworkConfig
import com.simats.selfora.data.api.NetworkErrorHandler
import com.simats.selfora.data.api.NetworkErrorType
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkConfigTest {

    @Test
    fun testEnsureTrailingSlash() {
        assertEquals("http://10.0.2.2:8080/", NetworkConfig.ensureTrailingSlash("http://10.0.2.2:8080"))
        assertEquals("http://10.0.2.2:8080/", NetworkConfig.ensureTrailingSlash("http://10.0.2.2:8080/"))
        assertEquals("https://api.selfora.com/", NetworkConfig.ensureTrailingSlash("https://api.selfora.com"))
        assertEquals("http://192.168.1.50:8080/", NetworkConfig.ensureTrailingSlash("  http://192.168.1.50:8080  "))
    }

    @Test
    fun testIsValidBaseUrl() {
        assertTrue(NetworkConfig.isValidBaseUrl("http://10.0.2.2:8080/"))
        assertTrue(NetworkConfig.isValidBaseUrl("https://api.selfora.com/"))
        assertTrue(NetworkConfig.isValidBaseUrl("http://192.168.1.100:8080/"))

        assertFalse(NetworkConfig.isValidBaseUrl(""))
        assertFalse(NetworkConfig.isValidBaseUrl("ftp://10.0.2.2/"))
        assertFalse(NetworkConfig.isValidBaseUrl("invalid-url"))
        assertFalse(NetworkConfig.isValidBaseUrl("://missing-scheme"))
    }

    @Test
    fun testNetworkErrorHandlerMapsExceptionsCorrectly() {
        val connectEx = ConnectException("Failed to connect to /10.0.2.2:8080")
        assertEquals(NetworkErrorType.BACKEND_UNREACHABLE, NetworkErrorHandler.getErrorType(connectEx))
        assertTrue(NetworkErrorHandler.getErrorMessage(connectEx).contains("Unable to reach the SELFORA server"))

        val hostEx = UnknownHostException("api.selfora.placeholder")
        assertEquals(NetworkErrorType.BACKEND_UNREACHABLE, NetworkErrorHandler.getErrorType(hostEx))

        val timeoutEx = SocketTimeoutException("Read timed out")
        assertEquals(NetworkErrorType.CONNECTION_TIMEOUT, NetworkErrorHandler.getErrorType(timeoutEx))
        assertTrue(NetworkErrorHandler.getErrorMessage(timeoutEx).contains("connection timed out"))

        val genericIoEx = IOException("Broken pipe")
        assertEquals(NetworkErrorType.UNKNOWN_NETWORK_ERROR, NetworkErrorHandler.getErrorType(genericIoEx))
    }

    @Test
    fun testNetworkErrorHandlerMapsHttpStatusCodesCorrectly() {
        assertEquals(NetworkErrorType.UNAUTHORIZED, NetworkErrorHandler.getHttpErrorType(401))
        assertTrue(NetworkErrorHandler.getHttpErrorMessage(401).contains("Session expired or invalid credentials"))

        assertEquals(NetworkErrorType.FORBIDDEN, NetworkErrorHandler.getHttpErrorType(403))
        assertTrue(NetworkErrorHandler.getHttpErrorMessage(403).contains("Access denied"))

        assertEquals(NetworkErrorType.NOT_FOUND, NetworkErrorHandler.getHttpErrorType(404))
        assertTrue(NetworkErrorHandler.getHttpErrorMessage(404).contains("could not be found"))

        assertEquals(NetworkErrorType.SERVER_ERROR, NetworkErrorHandler.getHttpErrorType(500))
        assertTrue(NetworkErrorHandler.getHttpErrorMessage(500).contains("error processing your request"))

        assertEquals(NetworkErrorType.SERVER_ERROR, NetworkErrorHandler.getHttpErrorType(503))
        assertTrue(NetworkErrorHandler.getHttpErrorMessage(503).contains("error processing your request"))
    }
}

