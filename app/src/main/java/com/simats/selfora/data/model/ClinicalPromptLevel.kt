package com.simats.selfora.data.model

import androidx.compose.ui.graphics.Color

enum class ClinicalPromptLevel(
    val id: Int,
    val code: String,
    val label: String,
    val shortLabel: String,
    val description: String,
    val colorHex: Long,
    val isAssistance: Boolean
) {
    L0(0, "INDEPENDENT", "Level 0: Independent", "L0", "Child performs step independently with natural cue", 0xFF10B981, true),
    L1(1, "VISUAL", "Level 1: Visual Prompt", "L1", "Visual cue, step photograph, or picture sequence", 0xFF0284C7, true),
    L2(2, "GESTURE", "Level 2: Gesture Prompt", "L2", "Pointing or non-verbal gesture toward action", 0xFF6366F1, true),
    L3(3, "VERBAL", "Level 3: Verbal Prompt", "L3", "Short, clear spoken instruction or cue", 0xFFF59E0B, true),
    L4(4, "MODEL_VIDEO", "Level 4: Model / Video", "L4", "Therapist demonstration or video modeling", 0xFFEC4899, true),
    L5(5, "PARTIAL_PHYSICAL", "Level 5: Partial Physical", "L5", "Minimal physical guidance at elbow or wrist", 0xFF8B5CF6, true),
    L6(6, "FULL_PHYSICAL", "Level 6: Full Physical", "L6", "Hand-over-hand physical guidance", 0xFFEF4444, true),
    UNABLE(7, "UNABLE", "Unable", "Unable", "Child could not complete step during session", 0xFFDC2626, false),
    NOT_ASSESSED(8, "NOT_ASSESSED", "Not Assessed", "Skipped", "Step skipped or not clinically assessed", 0xFF6B7280, false);

    val composeColor: Color
        get() = Color(colorHex)

    companion object {
        val hierarchyLevels: List<ClinicalPromptLevel>
            get() = listOf(L0, L1, L2, L3, L4, L5, L6)

        val nonAssistanceLevels: List<ClinicalPromptLevel>
            get() = listOf(UNABLE, NOT_ASSESSED)

        fun fromId(id: Int?): ClinicalPromptLevel? {
            if (id == null) return null
            return values().firstOrNull { it.id == id }
        }

        fun fromCode(code: String?): ClinicalPromptLevel? {
            if (code == null) return null
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) }
        }
    }
}

