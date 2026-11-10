package com.simats.selfora.data.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.MessageResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.URLEncoder

sealed class ChatWsEvent {
    data class NewMessage(val message: MessageResponse) : ChatWsEvent()
    data class MessagesRead(val childId: Long, val readByUserId: Long) : ChatWsEvent()
    data class ContactsUpdated(val childId: Long) : ChatWsEvent()
}

object ChatWebSocketClient {
    private const val TAG = "ChatWebSocketClient"
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _events = MutableSharedFlow<ChatWsEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ChatWsEvent> = _events.asSharedFlow()

    @Volatile
    private var activeSocket: WebSocket? = null

    @Volatile
    private var isConnecting = false

    @Volatile
    private var shouldStayConnected = false

    @Volatile
    private var lastTokenUsed: String? = null

    private var reconnectJob: Job? = null

    fun ensureConnected() {
        val token = ApiClient.getJwtToken() ?: SessionManager.getJwtToken()
        if (token.isNullOrBlank()) return

        shouldStayConnected = true

        // If already connected with the same token, nothing to do
        if (_isConnected.value && activeSocket != null && lastTokenUsed == token) {
            return
        }
        // If token changed (e.g. re-logged in as different user), close stale socket
        if (lastTokenUsed != null && lastTokenUsed != token) {
            activeSocket?.close(1000, "Token refreshed")
            activeSocket = null
            _isConnected.value = false
            isConnecting = false
        }

        if (isConnecting) return
        connectInternal(token)
    }

    private fun connectInternal(token: String) {
        isConnecting = true
        lastTokenUsed = token

        val rawBase = NetworkConfig.getBaseUrl().trimEnd('/')
        val wsBase = when {
            rawBase.startsWith("https://", ignoreCase = true) -> "wss://" + rawBase.substring(8)
            rawBase.startsWith("http://", ignoreCase = true) -> "ws://" + rawBase.substring(7)
            else -> "ws://$rawBase"
        }
        val encodedToken = try {
            URLEncoder.encode(token, "UTF-8")
        } catch (_: Exception) {
            token
        }
        val wsUrl = "$wsBase/ws/chat?token=$encodedToken"

        val request = Request.Builder()
            .url(wsUrl)
            .header("Authorization", "Bearer $token")
            .build()

        activeSocket = ApiClient.getWebSocketClient().newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                isConnecting = false
                _isConnected.value = true
                reconnectJob?.cancel()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val root = JsonParser.parseString(text).asJsonObject
                    val type = root.get("type")?.asString ?: return
                    when (type) {
                        "NEW_MESSAGE" -> {
                            val msgEl = root.get("message")
                            if (msgEl != null && msgEl.isJsonObject) {
                                val msg = gson.fromJson(msgEl, MessageResponse::class.java)
                                _events.tryEmit(ChatWsEvent.NewMessage(msg))
                            }
                        }
                        "MESSAGES_READ" -> {
                            val childId = root.get("childId")?.asLong ?: 0L
                            val readBy = root.get("readByUserId")?.asLong ?: 0L
                            _events.tryEmit(ChatWsEvent.MessagesRead(childId, readBy))
                        }
                        "CONTACTS_UPDATED" -> {
                            val childId = root.get("childId")?.asLong ?: 0L
                            _events.tryEmit(ChatWsEvent.ContactsUpdated(childId))
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing WS message: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnecting = false
                _isConnected.value = false
                activeSocket = null
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "WebSocket failed: ${t.message}")
                isConnecting = false
                _isConnected.value = false
                activeSocket = null
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (!shouldStayConnected) return
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            delay(3000)
            if (shouldStayConnected && !_isConnected.value) {
                val token = ApiClient.getJwtToken() ?: SessionManager.getJwtToken()
                if (!token.isNullOrBlank()) {
                    connectInternal(token)
                }
            }
        }
    }

    fun sendMessage(receiverId: Long, childId: Long, messageText: String): Boolean {
        val sock = activeSocket ?: return false
        if (!_isConnected.value) return false
        return try {
            val payload = mapOf(
                "type" to "SEND_MESSAGE",
                "receiverId" to receiverId,
                "childId" to childId,
                "messageText" to messageText
            )
            sock.send(gson.toJson(payload))
        } catch (_: Exception) {
            false
        }
    }

    fun markRead(childId: Long): Boolean {
        val sock = activeSocket ?: return false
        if (!_isConnected.value) return false
        return try {
            val payload = mapOf(
                "type" to "MARK_READ",
                "childId" to childId
            )
            sock.send(gson.toJson(payload))
        } catch (_: Exception) {
            false
        }
    }

    fun disconnect() {
        shouldStayConnected = false
        reconnectJob?.cancel()
        activeSocket?.close(1000, "User left chat")
        activeSocket = null
        isConnecting = false
        _isConnected.value = false
    }
}

