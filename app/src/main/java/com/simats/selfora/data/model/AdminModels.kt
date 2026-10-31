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

data class AdminChildItem(
    val id: Long,
    val fullName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String? = null,
    val age: Int = 6,
    val gender: String = "MALE",
    val avatarUrl: String? = null,
    val isActive: Boolean = true,
    val assignedTherapistId: Long? = null,
    val assignedTherapistName: String = "Unassigned",
    val linkedCaregiverId: Long? = null,
    val linkedCaregiverName: String = "Unassigned Caregiver",
    val activeAdlCategory: String = "Dressing - Upper Body T-Shirt",
    val createdAt: String? = null
)

data class AdminChildDetailProfile(
    val summary: AdminChildItem,
    val diagnosisNotes: String = "",
    val supportedAdlCategories: List<String> = listOf("Dressing", "Eating", "Grooming", "Shoes & Socks"),
    val totalSessions: Long = 0L,
    val completedAssessments: Long = 0L,
    val activeHomeProgramTitle: String = "",
    val homePracticeSessionsCount: Long = 0L
)

data class CaregiverItem(
    val id: Long,
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "+91 9876543210",
    val isActive: Boolean = true,
    val mustChangePassword: Boolean = false,
    val createdAt: String? = null,
    val linkedChildrenCount: Long = 0L,
    val linkedChildrenNames: List<String> = emptyList(),
    val linkedChildren: List<AdminChildItem> = emptyList()
)

data class CreateCaregiverRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val username: String,
    val password: String
)

data class UpdateCaregiverRequest(
    val fullName: String,
    val email: String,
    val phone: String
)

data class ChildReassignRequest(
    val childId: Long,
    val newTherapistId: Long,
    val reason: String
)

data class ResetPasswordRequest(
    val newPassword: String
)
