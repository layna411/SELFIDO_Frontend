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

    @GET("api/adaptive-plans/latest")
    suspend fun getLatestAdaptivePlan(
        @Query("childId") childId: Long,
        @Query("activityId") activityId: Long
    ): Response<AdaptivePlanResponse>

    // Home Programs
    @POST("api/home-programs")
    suspend fun createHomeProgram(@Body request: CreateHomeProgramRequest): Response<HomeProgramResponse>

    @GET("api/home-programs/child/{childId}")
    suspend fun getActiveHomeProgramForChild(@Path("childId") childId: Long): Response<HomeProgramResponse>

    @GET("api/home-programs/today")
    suspend fun getTodayHomePrograms(): Response<List<HomeProgramResponse>>

    // Progress
    @GET("api/children/{childId}/progress")
    suspend fun getChildProgress(
        @Path("childId") childId: Long,
        @Query("activityId") activityId: Long = 1
    ): Response<ProgressSummaryResponse>

    // Messages
    @POST("api/messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<MessageResponse>

    @GET("api/messages/child/{childId}")
    suspend fun getConversation(@Path("childId") childId: Long): Response<List<MessageResponse>>
}
