package com.simats.selfora.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.simats.selfora.ui.screens.dressing.AdlDressingTrainingScreen
import com.simats.selfora.ui.therapist.*

@Composable
fun SelforaNavHost(
    navController: NavHostController = rememberNavController()
) {
    var createdCredentials by remember { mutableStateOf<CredentialsSuccessResponse?>(null) }

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

    NavHost(
        navController = navController,
        startDestination = initialDestination
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
            TherapistDashboardScreen(
                onNavigateToAddChildWorkflow = { navController.navigate(NavRoutes.AddChildWorkflow.route) },
                onNavigateToChildren = { navController.navigate(NavRoutes.ChildList.route) },
                onNavigateToAssessment = { navController.navigate(NavRoutes.Assessment.createRoute(1L)) },
                onNavigateToHomePrograms = { navController.navigate(NavRoutes.HomeProgramCreate.createRoute(1L)) },
                onNavigateToProgress = { navController.navigate(NavRoutes.Progress.createRoute(1L)) },
                onNavigateToMessages = { navController.navigate(NavRoutes.Messages.createRoute(1L)) },
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
            CaregiverDashboardScreen(
                onStartPractice = { progId, _ -> navController.navigate(NavRoutes.HomePractice.createRoute(progId)) },
                onNavigateToTab = { route -> navController.navigate(route) },
                onNavigateToNotifications = { navController.navigate(NavRoutes.CaregiverNotifications.route) },
                onNavigateToProfile = { navController.navigate(NavRoutes.CaregiverProfile.route) },
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
}
