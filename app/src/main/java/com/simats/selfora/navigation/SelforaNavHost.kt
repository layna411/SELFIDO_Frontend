package com.simats.selfora.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.CredentialsSuccessResponse
import com.simats.selfora.ui.auth.ChangePasswordScreen
import com.simats.selfora.ui.auth.LoginScreen
import com.simats.selfora.ui.caregiver.*
import com.simats.selfora.ui.child.ChildActivityStepScreen
import com.simats.selfora.ui.child.ChildHomeScreen
import com.simats.selfora.ui.child.RewardCelebrationScreen
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.screens.dressing.AdlDressingTrainingScreen
import com.simats.selfora.ui.theme.SelforaBackground
import com.simats.selfora.ui.therapist.*

@Composable
fun SelforaNavHost(
    navController: NavHostController = rememberNavController()
) {
    var createdCredentials by remember { mutableStateOf<CredentialsSuccessResponse?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val initialDestination = if (SessionManager.isLoggedIn()) {
        if (SessionManager.mustChangePassword()) {
            NavRoutes.ChangePassword.route
        } else {
            when (SessionManager.getUserRole()) {
                "ROLE_CAREGIVER" -> NavRoutes.CaregiverDashboard.route
                "ROLE_CHILD" -> NavRoutes.ChildHome.route
                else -> NavRoutes.TherapistDashboard.route
            }
        }
    } else {
        NavRoutes.Login.route
    }

    val isTherapistRoute = currentRoute?.let { r ->
        r.startsWith("therapist_") ||
        r.startsWith("child_list") ||
        r.startsWith("add_child") ||
        r.startsWith("child_profile") ||
        r.startsWith("assessment") ||
        r.startsWith("therapy_session") ||
        r.startsWith("session_summary") ||
        r.startsWith("adaptation") ||
        r.startsWith("home_program_create") ||
        r.startsWith("progress") ||
        r.startsWith("messages")
    } ?: false

    val isCaregiverRoute = currentRoute?.let { r ->
        r.startsWith("caregiver_") ||
        r.startsWith("home_programme") ||
        r.startsWith("home_practice")
    } ?: false

    val activeTherapistTab = when {
        currentRoute?.startsWith("child_list") == true ||
        currentRoute?.startsWith("add_child") == true ||
        currentRoute?.startsWith("child_profile") == true -> TherapistTab.CHILDREN

        currentRoute?.startsWith("assessment") == true ||
        currentRoute?.startsWith("therapy_session") == true ||
        currentRoute?.startsWith("session_summary") == true ||
        currentRoute?.startsWith("adaptation") == true ||
        currentRoute?.startsWith("home_program_create") == true -> TherapistTab.ASSESSMENTS

        currentRoute?.startsWith("progress") == true -> TherapistTab.PROGRESS

        currentRoute?.startsWith("messages") == true -> TherapistTab.MESSAGES

        else -> TherapistTab.DASHBOARD
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SelforaBackground)
    ) {
        NavHost(
            navController = navController,
            startDestination = initialDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(350)) + fadeOut(animationSpec = tween(350)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(350)) + fadeOut(animationSpec = tween(350)) }
        ) {
            // Auth
            composable(NavRoutes.Login.route) {
                LoginScreen(
                    onLoginSuccess = { role, _ ->
                        if (SessionManager.mustChangePassword()) {
                            navController.navigate(NavRoutes.ChangePassword.route) { popUpTo(NavRoutes.Login.route) { inclusive = true } }
                        } else {
                            when (role) {
                                "ROLE_CAREGIVER" -> navController.navigate(NavRoutes.CaregiverDashboard.route) { popUpTo(NavRoutes.Login.route) { inclusive = true } }
                                "ROLE_CHILD" -> navController.navigate(NavRoutes.ChildHome.route) { popUpTo(NavRoutes.Login.route) { inclusive = true } }
                                else -> navController.navigate(NavRoutes.TherapistDashboard.route) { popUpTo(NavRoutes.Login.route) { inclusive = true } }
                            }
                        }
                    }
                )
            }

            composable(NavRoutes.ChangePassword.route) {
                ChangePasswordScreen(
                    onPasswordChanged = {
                        val role = SessionManager.getUserRole()
                        when (role) {
                            "ROLE_CAREGIVER" -> navController.navigate(NavRoutes.CaregiverDashboard.route) { popUpTo(NavRoutes.ChangePassword.route) { inclusive = true } }
                            "ROLE_CHILD" -> navController.navigate(NavRoutes.ChildHome.route) { popUpTo(NavRoutes.ChangePassword.route) { inclusive = true } }
                            else -> navController.navigate(NavRoutes.TherapistDashboard.route) { popUpTo(NavRoutes.ChangePassword.route) { inclusive = true } }
                        }
                    }
                )
            }

            // Therapist Navigation
            composable(NavRoutes.TherapistDashboard.route) {
                TherapistMainSwipeableScreen(
                    onNavigateToAddChildWorkflow = { navController.navigate(NavRoutes.AddChildWorkflow.route) },
                    onNavigateToChildProfile = { id -> navController.navigate(NavRoutes.ChildProfile.createRoute(id)) },
                    onNavigateToAssessment = { id -> navController.navigate(NavRoutes.Assessment.createRoute(id)) },
                    onNavigateToHomePrograms = { id -> navController.navigate(NavRoutes.HomeProgramCreate.createRoute(id)) },
                    onNavigateToProgress = { id -> navController.navigate(NavRoutes.Progress.createRoute(id)) },
                    onNavigateToMessages = { id -> navController.navigate(NavRoutes.Messages.createRoute(id)) },
                    onNavigateToNotifications = { navController.navigate(NavRoutes.CaregiverNotifications.route) },
                    onLogout = {
                        SessionManager.clearSession()
                        navController.navigate(NavRoutes.Login.route) { popUpTo(0) }
                    }
                )
            }

            composable(NavRoutes.AddChildWorkflow.route) {
                if (createdCredentials != null) {
                    CredentialsSuccessScreen(
                        credentials = createdCredentials!!,
                        onDone = {
                            val childId = createdCredentials!!.childId
                            createdCredentials = null
                            navController.navigate(NavRoutes.ChildProfile.createRoute(childId)) {
                                popUpTo(NavRoutes.TherapistDashboard.route)
                            }
                        }
                    )
                } else {
                    AddChildCaregiverWorkflowScreen(
                        onBack = { navController.popBackStack() },
                        onSuccess = { res ->
                            createdCredentials = res
                        }
                    )
                }
            }

            composable(NavRoutes.ChildList.route) {
                ChildListScreen(
                    onChildSelected = { childId -> navController.navigate(NavRoutes.ChildProfile.createRoute(childId)) },
                    onAddChildClick = { navController.navigate(NavRoutes.AddChildWorkflow.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.ChildProfile.route,
                arguments = listOf(navArgument("childId") { type = NavType.LongType })
            ) { backStack ->
                val childId = backStack.arguments?.getLong("childId") ?: 1L
                ChildProfileScreen(
                    childId = childId,
                    onStartAssessment = { id -> navController.navigate(NavRoutes.Assessment.createRoute(id)) },
                    onAssignProgramme = { id -> navController.navigate(NavRoutes.HomeProgramCreate.createRoute(id)) },
                    onViewProgress = { id -> navController.navigate(NavRoutes.Progress.createRoute(id)) },
                    onMessageCaregiver = { id -> navController.navigate(NavRoutes.Messages.createRoute(id)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.Assessment.route,
                arguments = listOf(navArgument("childId") { type = NavType.LongType })
            ) { backStack ->
                val childId = backStack.arguments?.getLong("childId") ?: 1L
                AssessmentScreen(
                    childId = childId,
                    onAssessmentSaved = { navController.navigate(NavRoutes.Progress.createRoute(childId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.TherapySession.route,
                arguments = listOf(
                    navArgument("childId") { type = NavType.LongType },
                    navArgument("activityId") { type = NavType.LongType }
                )
            ) { backStack ->
                val childId = backStack.arguments?.getLong("childId") ?: 1L
                val activityId = backStack.arguments?.getLong("activityId") ?: 1L
                TherapySessionScreen(
                    childId = childId,
                    activityId = activityId,
                    onSessionCompleted = { sessionId -> navController.navigate(NavRoutes.SessionSummary.createRoute(sessionId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.SessionSummary.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStack ->
                val sessionId = backStack.arguments?.getLong("sessionId") ?: 1L
                SessionSummaryScreen(
                    sessionId = sessionId,
                    onNavigateToAdaptation = { sId -> navController.navigate(NavRoutes.Adaptation.createRoute(sId)) },
                    onBackToDashboard = { navController.navigate(NavRoutes.TherapistDashboard.route) { popUpTo(NavRoutes.TherapistDashboard.route) { inclusive = true } } }
                )
            }

            composable(
                route = NavRoutes.Adaptation.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStack ->
                val sessionId = backStack.arguments?.getLong("sessionId") ?: 1L
                AdaptationScreen(
                    sessionId = sessionId,
                    onAdaptationSaved = { navController.navigate(NavRoutes.HomeProgramCreate.createRoute(1L)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.HomeProgramCreate.route,
                arguments = listOf(navArgument("childId") { type = NavType.LongType })
            ) { backStack ->
                val childId = backStack.arguments?.getLong("childId") ?: 1L
                HomeProgramCreateScreen(
                    childId = childId,
                    onProgramCreated = { navController.navigate(NavRoutes.TherapistDashboard.route) { popUpTo(NavRoutes.TherapistDashboard.route) { inclusive = true } } },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.Progress.route,
                arguments = listOf(navArgument("childId") { type = NavType.LongType })
            ) { backStack ->
                val childId = backStack.arguments?.getLong("childId") ?: 1L
                TherapistProgressScreen(
                    childId = childId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.Messages.route,
                arguments = listOf(navArgument("childId") { type = NavType.LongType })
            ) { backStack ->
                val childId = backStack.arguments?.getLong("childId") ?: 1L
                MessageScreen(
                    childId = childId,
                    onBack = { navController.popBackStack() }
                )
            }

            // Caregiver Navigation
            composable(NavRoutes.CaregiverDashboard.route) {
                CaregiverMainSwipeableScreen(
                    onStartPractice = { progId, _ -> navController.navigate(NavRoutes.HomePractice.createRoute(progId)) },
                    onNavigateToNotifications = { navController.navigate(NavRoutes.CaregiverNotifications.route) },
                    onViewDetailedHistory = { navController.navigate(NavRoutes.CaregiverHistory.route) },
                    onLogout = {
                        SessionManager.clearSession()
                        navController.navigate(NavRoutes.Login.route) { popUpTo(0) }
                    }
                )
            }

            composable(NavRoutes.HomeProgramme.route) {
                HomeProgrammeScreen(
                    onStartPractice = { progId, _ -> navController.navigate(NavRoutes.HomePractice.createRoute(progId)) },
                    onNavigateToTab = { route -> navController.navigate(route) }
                )
            }

            composable(
                route = NavRoutes.HomePractice.route,
                arguments = listOf(navArgument("programId") { type = NavType.LongType })
            ) { backStack ->
                val programId = backStack.arguments?.getLong("programId") ?: 1L
                HomePracticeScreen(
                    programId = programId,
                    onPracticeCompleted = { navController.navigate(NavRoutes.CaregiverDashboard.route) { popUpTo(NavRoutes.CaregiverDashboard.route) { inclusive = true } } },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.CaregiverProgress.route) {
                CaregiverProgressScreen(
                    onNavigateToHistory = { navController.navigate(NavRoutes.CaregiverHistory.route) },
                    onNavigateToTab = { route -> navController.navigate(route) }
                )
            }

            composable(NavRoutes.CaregiverHistory.route) {
                CaregiverHistoryScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.CaregiverNotifications.route) {
                CaregiverNotificationsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.CaregiverProfile.route) {
                CaregiverProfileScreen(
                    onLogout = {
                        SessionManager.clearSession()
                        navController.navigate(NavRoutes.Login.route) { popUpTo(0) }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.CaregiverMessages.route) {
                CaregiverMessagesScreen(
                    onNavigateToTab = { route -> navController.navigate(route) }
                )
            }

            // Child Navigation
            composable(NavRoutes.ChildHome.route) {
                ChildHomeScreen(
                    onSelectDressing = { activityId -> navController.navigate(NavRoutes.DressingTraining.createRoute(activityId, "ROLE_CHILD")) },
                    onSelectEating = { navController.navigate(NavRoutes.ChildActivityStep.createRoute(13L)) },
                    onSelectShoes = { navController.navigate(NavRoutes.ChildActivityStep.createRoute(9L)) },
                    onSelectRewards = { navController.navigate(NavRoutes.RewardCelebration.route) },
                    onLogout = {
                        SessionManager.clearSession()
                        navController.navigate(NavRoutes.Login.route) { popUpTo(0) }
                    }
                )
            }

            composable(
                route = NavRoutes.DressingTraining.route,
                arguments = listOf(
                    navArgument("activityId") { type = NavType.StringType },
                    navArgument("userRole") { type = NavType.StringType }
                )
            ) { backStack ->
                val activityId = backStack.arguments?.getString("activityId") ?: "boy_tshirt_activity"
                val userRole = backStack.arguments?.getString("userRole") ?: "ROLE_CHILD"
                AdlDressingTrainingScreen(
                    activityId = activityId,
                    userRole = userRole,
                    onNavigateBack = { navController.popBackStack() },
                    onViewProgress = { navController.navigate(NavRoutes.CaregiverProgress.route) }
                )
            }

            composable(
                route = NavRoutes.ChildActivityStep.route,
                arguments = listOf(navArgument("activityId") { type = NavType.LongType })
            ) { backStack ->
                val activityId = backStack.arguments?.getLong("activityId") ?: 1L
                ChildActivityStepScreen(
                    activityId = activityId,
                    onFinished = { navController.navigate(NavRoutes.RewardCelebration.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.RewardCelebration.route) {
                RewardCelebrationScreen(
                    onGoHome = { navController.navigate(NavRoutes.ChildHome.route) { popUpTo(NavRoutes.ChildHome.route) { inclusive = true } } }
                )
            }
        }

        when {
            isTherapistRoute && currentRoute != NavRoutes.TherapistDashboard.route -> {
                GlassNavigationBar(
                    currentTab = activeTherapistTab,
                    onTabSelected = { tab ->
                        val targetRoute = when (tab) {
                            TherapistTab.DASHBOARD -> NavRoutes.TherapistDashboard.route
                            TherapistTab.CHILDREN -> NavRoutes.ChildList.route
                            TherapistTab.ASSESSMENTS -> NavRoutes.Assessment.createRoute(1L)
                            TherapistTab.PROGRESS -> NavRoutes.Progress.createRoute(1L)
                            TherapistTab.MESSAGES -> NavRoutes.Messages.createRoute(1L)
                        }
                        if (currentRoute != targetRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(NavRoutes.TherapistDashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }
            isCaregiverRoute && currentRoute != NavRoutes.CaregiverDashboard.route -> {
                CaregiverBottomNavigation(
                    currentRoute = currentRoute ?: "",
                    onTabSelected = { targetRoute ->
                        if (currentRoute != targetRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(NavRoutes.CaregiverDashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }
        }
    }
}
