package com.simats.selfora.data.model

data class AdminDashboardStatsResponse(
    val totalTherapists: Long = 12L,
    val activeTherapists: Long = 10L,
    val totalChildren: Long = 48L,
    val totalCaregivers: Long = 42L,
    val activeTherapySessions: Long = 156L,
    val completedAssessments: Long = 89L,
    val activeHomeProgrammes: Long = 34L,
    val pendingCaregiverReviews: Long = 7L
)

data class AdminChartDataResponse(
    val registrationsOverTime: Map<String, Long> = mapOf("Jan" to 8L, "Feb" to 15L, "Mar" to 24L, "Apr" to 35L, "May" to 42L, "Jun" to 48L),
    val sessionsOverTime: Map<String, Long> = mapOf("Mon" to 18L, "Tue" to 28L, "Wed" to 22L, "Thu" to 32L, "Fri" to 40L, "Sat" to 12L, "Sun" to 4L),
    val adlCategoryDistribution: Map<String, Long> = mapOf("Dressing" to 45L, "Eating" to 28L, "Grooming" to 18L, "Shoes & Socks" to 9L)
)

data class TherapistItem(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String = "+91 9876543210",
    val designation: String = "Senior Occupational Therapist",
    val specialization: String = "Pediatric ADL & Fine Motor Skills",
    val qualification: String = "BOT, MOT (Pediatrics)",
    val experience: String = "6 Years",
    val isActive: Boolean = true,
    val assignedChildrenCount: Int = 8
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

data class AuditLogItem(
    val id: Long,
    val actorId: Long,
    val actorRole: String,
    val action: String,
    val affectedEntity: String,
    val changeSummary: String,
    val timestamp: String
)
