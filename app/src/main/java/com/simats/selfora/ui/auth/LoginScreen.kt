package com.simats.selfora.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.R
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.LoginRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (role: String, userId: Long) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    var isUsernameFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }

    val isImeVisible = WindowInsets.isImeVisible
    val isEditing = isImeVisible || isUsernameFocused || isPasswordFocused

    val primaryBlue = Color(0xFF2563EB)
    val textPrimary = Color(0xFF172033)
    val textSecondary = Color(0xFF64748B)

    // Smooth Auto-Scroll when Keyboard Appears
    LaunchedEffect(isEditing) {
        if (isEditing) {
            scrollState.animateScrollTo(250)
        }
    }

    fun performLogin() {
        if (isLoading) return
        keyboardController?.hide()
        val cleanUsername = username.trim()
        val cleanPassword = password.trim()

        if (cleanUsername.isEmpty()) {
            errorMessage = "Please enter your User ID or Email"
            return
        }
        if (cleanPassword.isEmpty()) {
            errorMessage = "Please enter your password"
            return
        }

        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            try {
                val response = ApiClient.apiService.login(LoginRequest(cleanUsername, cleanPassword))
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    val token = authResponse.token
                    val userId = authResponse.userId
                    val mustChange = authResponse.mustChangePassword ?: false

                    val primaryRole = extractRoleFromResponse(authResponse.roles)

                    if (primaryRole != null) {
                        SessionManager.saveSession(
                            role = primaryRole,
                            userId = userId,
                            username = authResponse.username ?: cleanUsername,
                            token = token,
                            mustChangePassword = mustChange
                        )
                        onLoginSuccess(primaryRole, userId)
                    } else {
                        errorMessage = "Your account role could not be verified. Please contact the administrator."
                    }
                } else {
                    val code = response.code()
                    errorMessage = when (code) {
                        401 -> "Invalid User ID or password. Please check your credentials."
                        403 -> "Your account has been disabled or expired. Please contact support."
                        404 -> "User account not found."
                        else -> "Authentication failed (Error $code). Please try again."
                    }
                }
            } catch (e: Exception) {
                errorMessage = if (e is java.io.IOException) {
                    "Unable to connect to server. Please check your network connection."
                } else {
                    "Authentication error occurred. Please try again later."
                }
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F7FC),
                        Color(0xFFEFF6FF),
                        Color(0xFFE0F2FE)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isEditing) Arrangement.Top else Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Brand Header & Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isEditing) 36.dp else 44.dp)
                        .clip(CircleShape)
                        .background(primaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "SELFIDO Logo",
                        tint = primaryBlue,
                        modifier = Modifier.size(if (isEditing) 20.dp else 26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "SELFIDO",
                    fontSize = if (isEditing) 22.sp else 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryBlue,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Smoothly collapse tagline when typing / focused
            AnimatedVisibility(
                visible = !isEditing,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(250))
            ) {
                Text(
                    text = "Building Daily Living Skills for a Brighter Tomorrow",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Smoothly collapse hero avatar graphic when typing / focused
            AnimatedVisibility(
                visible = !isEditing,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(350)),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(300))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.6f))
                            .border(1.dp, Color.White, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.welcome_hero_kids),
                            contentDescription = "SELFIDO Avatars",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isEditing) 10.dp else 18.dp))

            // Main Glassmorphism Login Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp), clip = false),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Welcome back",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Sign in to continue to SELFIDO",
                        fontSize = 12.5.sp,
                        color = textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Message Display Area
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEE2E2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // User ID / Email Input
                    Text(
                        text = "User ID or Email",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (errorMessage != null) errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isUsernameFocused = it.isFocused },
                        placeholder = { Text("Enter your username or email", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = primaryBlue
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input
                    Text(
                        text = "Password",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (errorMessage != null) errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isPasswordFocused = it.isFocused },
                        placeholder = { Text("Enter your password", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = primaryBlue
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { performLogin() }
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Forgot Password Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showForgotPasswordDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Forgot Password?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sign In Action Button
                    Button(
                        onClick = { performLogin() },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryBlue,
                            disabledContainerColor = primaryBlue.copy(alpha = 0.6f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Sign In",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Footer Info
            Text(
                text = "SELFIDO Clinical Platform • Multi-Role Secure Access",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Forgot Password Info Dialog
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = {
                    Text(
                        text = "Reset Password",
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                },
                text = {
                    Text(
                        text = "For security reasons, password resets are managed by your System Administrator or assigned Clinician. Please contact support or your Super Admin to reset your account credentials.",
                        fontSize = 13.sp,
                        color = textSecondary,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("OK", fontWeight = FontWeight.Bold, color = primaryBlue)
                    }
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = Color.White
            )
        }
    }
}

private fun extractRoleFromResponse(roles: List<String>?): String? {
    if (roles.isNullOrEmpty()) return null
    return when {
        roles.contains("ROLE_SUPER_ADMIN") || roles.contains("ROLE_ADMIN") -> "ROLE_SUPER_ADMIN"
        roles.contains("ROLE_THERAPIST") -> "ROLE_THERAPIST"
        roles.contains("ROLE_CAREGIVER") -> "ROLE_CAREGIVER"
        roles.contains("ROLE_CHILD") -> "ROLE_CHILD"
        else -> roles.firstOrNull()
    }
}
