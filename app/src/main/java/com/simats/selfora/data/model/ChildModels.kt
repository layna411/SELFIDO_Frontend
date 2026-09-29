package com.simats.selfora.data.model

data class ChildDto(
    val id: Long? = null,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val gender: String,
    val avatarUrl: String? = null,
    val diagnosisNotes: String? = null,
    val isActive: Boolean? = true,
    val caregiverName: String? = null,
    val therapistName: String? = null
)
