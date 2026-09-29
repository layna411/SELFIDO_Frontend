package com.simats.selfora.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.LoginRequest
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (role: String, userId: Long) -> Unit
) {
    var username by remember { mutableStateOf("therapist1") }
    var password by remember { mutableStateOf("password123") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SelforaBluePrimary, SelforaBlueDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SelforaBlueLight,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "SELFORA Icon",
                            tint = SelforaBlueDark,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SELFORA",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaBlueDark
                )

                Text(
                    text = "Occupational Therapy ADL Training Platform",
                    fontSize = 12.sp,
                    color = SelforaTextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Select Quick Persona Role:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SelforaTextPrimary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SelforaBackground, shape = RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val roles = listOf(
                        Triple("therapist1", "Therapist", Icons.Default.MedicalServices),
                        Triple("caregiver1", "Caregiver", Icons.Default.FamilyRestroom),
                        Triple("child1", "Child", Icons.Default.ChildCare)
                    )

                    roles.forEach { (roleUser, roleName, roleIcon) ->
                        val isSelected = username == roleUser
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable {
                                    username = roleUser
                                    password = "password123"
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SelforaPrimary else Color.Transparent,
                            shadowElevation = if (isSelected) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = roleIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else SelforaTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = roleName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else SelforaTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SelforaTextPrimary,
                        unfocusedTextColor = SelforaTextPrimary,
                        focusedContainerColor = SelforaBackground,
                        unfocusedContainerColor = SelforaBackground,
                        focusedBorderColor = SelforaPrimary,
                        unfocusedBorderColor = SelforaBorder,
                        focusedLabelColor = SelforaPrimary,
                        unfocusedLabelColor = SelforaTextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SelforaTextPrimary,
                        unfocusedTextColor = SelforaTextPrimary,
                        focusedContainerColor = SelforaBackground,
                        unfocusedContainerColor = SelforaBackground,
                        focusedBorderColor = SelforaPrimary,
                        unfocusedBorderColor = SelforaBorder,
                        focusedLabelColor = SelforaPrimary,
                        unfocusedLabelColor = SelforaTextSecondary
                    )
                )


                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = SelforaRedAccent,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val response = ApiClient.apiService.login(LoginRequest(username, password))
                                if (response.isSuccessful && response.body() != null) {
                                    val authRes = response.body()!!
                                    ApiClient.setJwtToken(authRes.token)
                                    val primaryRole = authRes.roles.firstOrNull() ?: "ROLE_THERAPIST"
                                    com.simats.selfora.data.local.SessionManager.saveSession(primaryRole, authRes.userId, username, authRes.token)
                                    onLoginSuccess(primaryRole, authRes.userId)
                                } else {
                                    // Direct offline fallback for quick access during testing/demos
                                    val fallbackRole = when (username) {
                                        "caregiver1" -> "ROLE_CAREGIVER"
                                        "child1" -> "ROLE_CHILD"
                                        else -> "ROLE_THERAPIST"
                                    }
                                    com.simats.selfora.data.local.SessionManager.saveSession(fallbackRole, 1L, username, "")
                                    onLoginSuccess(fallbackRole, 1L)
                                }
                            } catch (e: Exception) {
                                // Offline fallback launch
                                val fallbackRole = when (username) {
                                    "caregiver1" -> "ROLE_CAREGIVER"
                                    "child1" -> "ROLE_CHILD"
                                    else -> "ROLE_THERAPIST"
                                }
                                com.simats.selfora.data.local.SessionManager.saveSession(fallbackRole, 1L, username, "")
                                onLoginSuccess(fallbackRole, 1L)
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaBluePrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "LOGIN TO SELFORA",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
