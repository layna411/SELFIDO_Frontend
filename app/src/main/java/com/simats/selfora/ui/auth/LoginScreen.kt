package com.simats.selfora.ui.auth

import androidx.compose.animation.*
import androidx.compose.runtime.*

@Composable
fun LoginScreen(
    onLoginSuccess: (role: String, userId: Long) -> Unit
) {
    var currentStep by remember { mutableStateOf("welcome") }

    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            if (targetState != "welcome") {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }
        },
        label = "LoginScreenTransition"
    ) { step ->
        when (step) {
            "therapist_login" -> {
                TherapistLoginScreen(
                    onLoginSuccess = onLoginSuccess,
                    onBack = { currentStep = "welcome" }
                )
            }
            "caregiver_login" -> {
                CaregiverLoginScreen(
                    onLoginSuccess = onLoginSuccess,
                    onBack = { currentStep = "welcome" }
                )
            }
            "super_admin_login" -> {
                com.simats.selfora.ui.admin.SuperAdminLoginScreen(
                    onLoginSuccess = onLoginSuccess,
                    onBack = { currentStep = "welcome" }
                )
            }
            else -> {
                SelfidoWelcomeScreen(
                    onNavigateToTherapistLogin = { currentStep = "therapist_login" },
                    onNavigateToCaregiverLogin = { currentStep = "caregiver_login" },
                    onNavigateToSuperAdminLogin = { currentStep = "super_admin_login" }
                )
            }
        }
    }
}
