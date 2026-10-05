package com.simats.selfora.data.api

import com.simats.selfora.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SelforaApiService {

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    // Therapist Module
    @GET("api/therapist/dashboard-summary")
    suspend fun getTherapistDashboardSummary(): Response<TherapistDashboardSummaryResponse>

    @POST("api/therapist/create-child-with-caregiver")
    suspend fun createChildWithCaregiver(@Body request: CreateChildWithCaregiverRequest): Response<CredentialsSuccessResponse>

    @GET("api/therapist/children")
    suspend fun getTherapistChildren(
        @Query("filter") filter: String? = null,
        @Query("search") search: String? = null
    ): Response<List<ChildSummaryItem>>

    @POST("api/therapist/caregivers/{caregiverId}/reset-password")
    suspend fun resetCaregiverPassword(@Path("caregiverId") caregiverId: Long): Response<ResetPasswordResultResponse>

    @POST("api/therapist/caregivers/{caregiverId}/toggle-status")
    suspend fun toggleCaregiverStatus(
        @Path("caregiverId") caregiverId: Long,
        @Query("active") active: Boolean
    ): Response<Unit>

    // Children
    @GET("api/children")
    suspend fun getMyChildren(): Response<List<ChildDto>>

    @GET("api/children/{id}")
    suspend fun getChildById(@Path("id") id: Long): Response<ChildDto>

    @POST("api/children")
    suspend fun createChild(@Body child: ChildDto): Response<ChildDto>

    // Activities
    @GET("api/activities")
    suspend fun getAllActivities(): Response<List<ActivityResponse>>

    @GET("api/activities/{id}")
    suspend fun getActivityById(@Path("id") id: Long): Response<ActivityResponse>

    @GET("api/activities/{id}/steps")
    suspend fun getTaskSteps(@Path("id") id: Long): Response<List<TaskStepResponse>>

    // Assessment
    @POST("api/assessments")
    suspend fun createAssessment(@Body request: CreateAssessmentRequest): Response<AssessmentResponse>

    @GET("api/assessments/{id}")
    suspend fun getAssessmentById(@Path("id") id: Long): Response<AssessmentResponse>

    // Sessions
    @POST("api/sessions/start")
    suspend fun startSession(@Body request: StartSessionRequest): Response<SessionResponse>

    @POST("api/sessions/{id}/performance")
    suspend fun recordPerformance(
        @Path("id") id: Long,
        @Body request: RecordPerformanceRequest
    ): Response<PerformanceRecordResponse>

    @POST("api/sessions/{id}/complete")
    suspend fun completeSession(@Path("id") id: Long): Response<SessionSummaryResponse>

    @GET("api/sessions/{id}")
    suspend fun getSessionById(@Path("id") id: Long): Response<SessionResponse>

    // Adaptive Plans
    @POST("api/adaptive-plans")
    suspend fun createAdaptivePlan(@Body request: CreateAdaptivePlanRequest): Response<AdaptivePlanResponse>

    @POST("api/adaptive-plans/{id}/approve")
    suspend fun approveAdaptivePlan(@Path("id") id: Long): Response<AdaptivePlanResponse>

    @GET("api/adaptive-plans/latest")
    suspend fun getLatestAdaptivePlan(
        @Query("childId") childId: Long,
        @Query("activityId") activityId: Long
    ): Response<AdaptivePlanResponse>

    @GET("api/adaptive-plans/child/{childId}")
    suspend fun getAllAdaptivePlansForChild(@Path("childId") childId: Long): Response<List<AdaptivePlanResponse>>

    // Prompt Fading
    @POST("api/prompt-fading/plans")
    suspend fun createPromptFadingPlan(@Body request: CreatePromptFadingPlanRequest): Response<PromptFadingPlanResponse>

    @POST("api/prompt-fading/plans/{planId}/approve")
    suspend fun approvePromptFadingPlan(@Path("planId") planId: Long): Response<PromptFadingPlanResponse>

    @PUT("api/prompt-fading/plans/{planId}/status")
    suspend fun updatePromptFadingPlanStatus(
        @Path("planId") planId: Long,
        @Query("status") status: String
    ): Response<PromptFadingPlanResponse>

    @GET("api/prompt-fading/child/{childId}/plans")
    suspend fun getPromptFadingPlansForChild(@Path("childId") childId: Long): Response<List<PromptFadingPlanResponse>>

    @GET("api/prompt-fading/child/{childId}/history")
    suspend fun getPromptFadingHistoryForChild(@Path("childId") childId: Long): Response<List<PromptFadingHistoryItemResponse>>

    // Home Programs & Caregiver Practice
    @POST("api/home-programs")
    suspend fun createHomeProgram(@Body request: CreateHomeProgramRequest): Response<HomeProgramResponse>

    @GET("api/home-programs/child/{childId}")
    suspend fun getActiveHomeProgramForChild(@Path("childId") childId: Long): Response<HomeProgramResponse>

    @GET("api/home-programs/today")
    suspend fun getTodayHomePrograms(): Response<List<HomeProgramResponse>>

    @POST("api/caregiver-practice/submit")
    suspend fun submitPractice(@Body request: SubmitPracticeRequest): Response<CaregiverPracticeSessionResponse>

    @GET("api/caregiver-practice/reviews/pending")
    suspend fun getPendingReviews(): Response<List<CaregiverPracticeSessionResponse>>

    @GET("api/caregiver-practice/history/child/{childId}")
    suspend fun getPracticeHistory(@Path("childId") childId: Long): Response<List<CaregiverPracticeSessionResponse>>

    @POST("api/caregiver-practice/reviews/{sessionId}")
    suspend fun reviewPracticeSession(
        @Path("sessionId") sessionId: Long,
        @Body request: TherapistReviewRequest
    ): Response<CaregiverPracticeSessionResponse>

    // Progress & Analyze & Adapt
    @GET("api/children/{childId}/progress")
    suspend fun getChildProgress(
        @Path("childId") childId: Long,
        @Query("activityId") activityId: Long = 1
    ): Response<ProgressSummaryResponse>

    @GET("api/children/{childId}/analyze-adapt")
    suspend fun getAnalyzeAndAdaptDashboard(
        @Path("childId") childId: Long,
        @Query("activityId") activityId: Long = 1
    ): Response<AnalyzeAndAdaptDashboardResponse>

    // Messages
    @POST("api/messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<MessageResponse>

    @GET("api/messages/child/{childId}")
    suspend fun getConversation(@Path("childId") childId: Long): Response<List<MessageResponse>>

    // Child Mode Stage 7 Endpoints
    @GET("api/child-mode/dashboard/{childId}")
    suspend fun getChildModeDashboard(@Path("childId") childId: Long): Response<ChildDashboardResponse>

    @POST("api/child-mode/progress")
    suspend fun recordChildStepProgress(@Body request: RecordStepProgressRequest): Response<Unit>

    @GET("api/child-mode/rewards/{childId}")
    suspend fun getChildRewards(@Path("childId") childId: Long): Response<List<ChildRewardDto>>

    @GET("api/child-mode/preferences/{childId}")
    suspend fun getChildPreferences(@Path("childId") childId: Long): Response<ChildModePreferenceDto>

    @PUT("api/child-mode/preferences/{childId}")
    suspend fun updateChildPreferences(
        @Path("childId") childId: Long,
        @Body dto: ChildModePreferenceDto
    ): Response<ChildModePreferenceDto>

    // Super Admin Endpoints
    @GET("api/admin/dashboard/stats")
    suspend fun getAdminDashboardStats(): Response<AdminDashboardStatsResponse>

    @GET("api/admin/dashboard/charts")
    suspend fun getAdminChartData(): Response<AdminChartDataResponse>

    @GET("api/admin/therapists")
    suspend fun getAllTherapists(): Response<List<TherapistItem>>

    @POST("api/admin/therapists")
    suspend fun createTherapist(@Body request: CreateTherapistRequest): Response<TherapistItem>

    @PUT("api/admin/therapists/{id}/status")
    suspend fun updateTherapistStatus(
        @Path("id") id: Long,
        @Query("active") active: Boolean
    ): Response<TherapistItem>

    @GET("api/admin/audit-logs")
    suspend fun getAuditLogs(): Response<List<AuditLogItem>>
}


