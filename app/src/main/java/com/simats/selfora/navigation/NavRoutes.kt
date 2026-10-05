package com.simats.selfora.navigation

sealed class NavRoutes(val route: String) {
    object Welcome : NavRoutes("welcome")
    object TherapistLogin : NavRoutes("therapist_login")
    object CaregiverLogin : NavRoutes("caregiver_login")
    object SuperAdminLogin : NavRoutes("super_admin_login")
    object Login : NavRoutes("login")

    object ChangePassword : NavRoutes("change_password")

    // Super Admin Routes
    object SuperAdminDashboard : NavRoutes("super_admin_dashboard")
    object TherapistManagement : NavRoutes("therapist_management")
    object TherapistDetails : NavRoutes("therapist_details/{therapistId}") {
        fun createRoute(therapistId: Long) = "therapist_details/$therapistId"
    }
    object CreateTherapist : NavRoutes("create_therapist")
    object ChildManagement : NavRoutes("child_management")
    object AdminChildDetails : NavRoutes("admin_child_details/{childId}") {
        fun createRoute(childId: Long) = "admin_child_details/$childId"
    }
    object CaregiverManagement : NavRoutes("caregiver_management")
    object ClinicalOverview : NavRoutes("clinical_overview")
    object AdminHomeProgramme : NavRoutes("admin_home_programme")
    object AdminReports : NavRoutes("admin_reports")
    object AdminAuditLogs : NavRoutes("admin_audit_logs")
    object AdminSettings : NavRoutes("admin_settings")

    // Therapist Routes
    object TherapistDashboard : NavRoutes("therapist_dashboard")
    object ChildList : NavRoutes("child_list")
    object AddChildWorkflow : NavRoutes("add_child_workflow")
    object ChildProfile : NavRoutes("child_profile/{childId}") {
        fun createRoute(childId: Long) = "child_profile/$childId"
    }
    object Assessment : NavRoutes("assessment/{childId}") {
        fun createRoute(childId: Long) = "assessment/$childId"
    }
    object TherapySession : NavRoutes("therapy_session/{childId}/{activityId}") {
        fun createRoute(childId: Long, activityId: Long = 1L) = "therapy_session/$childId/$activityId"
    }
    object SessionSummary : NavRoutes("session_summary/{sessionId}") {
        fun createRoute(sessionId: Long) = "session_summary/$sessionId"
    }
    object Adaptation : NavRoutes("adaptation/{sessionId}") {
        fun createRoute(sessionId: Long) = "adaptation/$sessionId"
    }
    object AnalyzeAndAdapt : NavRoutes("analyze_and_adapt/{childId}") {
        fun createRoute(childId: Long = 1L) = "analyze_and_adapt/$childId"
    }
    object PromptFading : NavRoutes("prompt_fading/{childId}") {
        fun createRoute(childId: Long = 1L) = "prompt_fading/$childId"
    }
    object HomeProgramCreate : NavRoutes("home_program_create/{childId}") {
        fun createRoute(childId: Long = 1L) = "home_program_create/$childId"
    }
    object HomeProgramReview : NavRoutes("home_program_review/{childId}") {
        fun createRoute(childId: Long = 1L) = "home_program_review/$childId"
    }
    object Progress : NavRoutes("progress/{childId}") {
        fun createRoute(childId: Long = 1L) = "progress/$childId"
    }
    object Messages : NavRoutes("messages/{childId}") {
        fun createRoute(childId: Long = 1L) = "messages/$childId"
    }

    // Caregiver Routes
    object CaregiverDashboard : NavRoutes("caregiver_dashboard")
    object HomeProgramme : NavRoutes("home_programme")
    object HomePractice : NavRoutes("home_practice/{programId}") {
        fun createRoute(programId: Long = 1L) = "home_practice/$programId"
    }
    object CaregiverProgress : NavRoutes("caregiver_progress")
    object CaregiverHistory : NavRoutes("caregiver_history")
    object CaregiverNotifications : NavRoutes("caregiver_notifications")
    object CaregiverProfile : NavRoutes("caregiver_profile")
    object CaregiverMessages : NavRoutes("caregiver_messages") {
        fun createRoute(childId: Long = 1L) = "caregiver_messages"
    }

    // Child Routes
    object ChildHome : NavRoutes("child_home")
    object ChildActivityStep : NavRoutes("child_activity_step/{activityId}") {
        fun createRoute(activityId: Long = 1L) = "child_activity_step/$activityId"
    }
    object ChildCompletion : NavRoutes("child_completion/{activityId}") {
        fun createRoute(activityId: Long = 1L) = "child_completion/$activityId"
    }
    object ChildSettings : NavRoutes("child_settings")
    object DressingTraining : NavRoutes("dressing_training/{activityId}/{userRole}") {
        fun createRoute(activityId: String = "boy_tshirt_activity", userRole: String = "ROLE_CHILD") = "dressing_training/$activityId/$userRole"
    }
    object RewardCelebration : NavRoutes("reward_celebration")
}
