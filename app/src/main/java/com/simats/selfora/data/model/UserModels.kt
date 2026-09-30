package com.simats.selfora.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val tokenType: String,
    val userId: Long,
    val username: String,
    val fullName: String,
    val roles: List<String>,
    val mustChangePassword: Boolean? = false
)

data class ChangePasswordRequest(
    val currentPassword: String = "",
    val newPassword: String
)
