package com.simats.selfora.data.model

data class AdminDashboardStatsResponse(
    val totalTherapists: Long = 0L,
    val activeTherapists: Long = 0L,
    val totalChildren: Long = 0L,
    val totalCaregivers: Long = 0L,
    val activeTherapySessions: Long = 0L,
    val completedAssessments: Long = 0L,
    val activeHomeProgrammes: Long = 0L,
    val pendingCaregiverReviews: Long = 0L
)

data class AdminChartDataResponse(
    val registrationsOverTime: Map<String, Long> = emptyMap(),
    val sessionsOverTime: Map<String, Long> = emptyMap(),
    val adlCategoryDistribution: Map<String, Long> = emptyMap()
)

data class TherapistItem(
    val id: Long,
    val username: String = "",
    val fullName: String,
    val email: String,
    val phone: String = "+91 9876543210",
    val designation: String = "Senior Occupational Therapist",
    val specialization: String = "Pediatric ADL & Fine Motor Skills",
    val qualification: String = "BOT, MOT (Pediatrics)",
    val experience: String = "5 Years",
    val isActive: Boolean = true,
    val mustChangePassword: Boolean = false,
    val assignedChildrenCount: Int = 0
)

data class CreateTherapistRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val username: String,
    val password: String,
    val designation: String,
    val specialization: String,
    val qualification: String,
    val experience: String
)

data class UpdateTherapistRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val designation: String,
    val specialization: String,
    val qualification: String,
    val experience: String
)

data class AuditLogItem(
    val id: Long,
    val actorId: Long,
    val actorRole: String,
    val action: String,
    val affectedEntity: String,
    val changeSummary: String,
    val timestamp: String
)
