package com.simats.selfora.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// SELFORA Primary Color Palette
// ==========================================

val SelforaPrimary = Color(0xFF2563EB)        // Royal Blue
val SelforaSecondary = Color(0xFF7C3AED)      // Soft Purple
val SelforaSuccess = Color(0xFF10B981)        // Teal Green
val SelforaWarning = Color(0xFFF59E0B)        // Amber
val SelforaError = Color(0xFFEF4444)          // Red

val SelforaBackground = Color(0xFFF8FAFC)     // Soft White
val SelforaSurface = Color(0xFFFFFFFF)        // White
val SelforaTextPrimary = Color(0xFF172033)    // Dark Navy
val SelforaTextSecondary = Color(0xFF64748B)  // Slate Gray
val SelforaBorder = Color(0xFFE2E8F0)         // Light Slate

// Legacy Aliases for Component Backward-Compatibility
val PrimaryBlue = SelforaPrimary
val SecondaryPurple = SelforaSecondary
val SuccessTeal = SelforaSuccess
val WarningAmber = SelforaWarning
val ErrorRed = SelforaError
val BackgroundLight = SelforaBackground
val SurfaceWhite = SelforaSurface
val TextPrimary = SelforaTextPrimary
val TextSecondary = SelforaTextSecondary
val BorderLight = SelforaBorder

val SelforaBluePrimary = SelforaPrimary
val SelforaBlueDark = Color(0xFF1D4ED8)
val SelforaBlueLight = Color(0xFFDBEAFE)
val SelforaTeal = SelforaSuccess
val SelforaGreenSuccess = SelforaSuccess
val SelforaOrangeWarning = SelforaWarning
val SelforaRedAccent = SelforaError
val SelforaPurpleAccent = SelforaSecondary
val SelforaBgLight = SelforaBackground
val SelforaSurfaceWhite = SelforaSurface
val SelforaTextDark = SelforaTextPrimary
val SelforaTextMuted = SelforaTextSecondary

// Dark Mode Palette
val SelforaDarkBackground = Color(0xFF0F172A)
val SelforaDarkSurface = Color(0xFF1E293B)
val SelforaDarkPrimary = Color(0xFF60A5FA)
val SelforaDarkSecondary = Color(0xFFA78BFA)
val SelforaDarkSuccess = Color(0xFF34D399)
val SelforaDarkTextPrimary = Color(0xFFF8FAFC)
val SelforaDarkTextSecondary = Color(0xFFCBD5E1)

// ==========================================
// Prompt Hierarchy Colors
// ==========================================

val PromptLevel0Independent = Color(0xFF10B981)   // Level 0: Teal Green
val PromptLevel1Visual = Color(0xFF3B82F6)        // Level 1: Blue
val PromptLevel2Gesture = Color(0xFFF59E0B)       // Level 2: Amber
val PromptLevel3Verbal = Color(0xFFF97316)        // Level 3: Orange
val PromptLevel4Model = Color(0xFF8B5CF6)         // Level 4: Purple
val PromptLevel5PartialPhysical = Color(0xFFA855F7) // Level 5: Violet
val PromptLevel6FullPhysical = Color(0xFFEF4444)   // Level 6: Red
val PromptLevelUnable = Color(0xFF475569)         // Unable: Dark Slate

fun getPromptHierarchyColor(level: String?): Color {
    return when (level?.uppercase()) {
        "LEVEL_0", "INDEPENDENT", "0" -> PromptLevel0Independent
        "LEVEL_1", "VISUAL_PROMPT", "1" -> PromptLevel1Visual
        "LEVEL_2", "GESTURE_PROMPT", "2" -> PromptLevel2Gesture
        "LEVEL_3", "VERBAL_PROMPT", "3" -> PromptLevel3Verbal
        "LEVEL_4", "MODEL_PROMPT", "VIDEO", "4" -> PromptLevel4Model
        "LEVEL_5", "PARTIAL_PHYSICAL", "5" -> PromptLevel5PartialPhysical
        "LEVEL_6", "FULL_PHYSICAL", "6" -> PromptLevel6FullPhysical
        "UNABLE" -> PromptLevelUnable
        else -> SelforaTextSecondary
    }
}

// ==========================================
// Child Mode Softer Pastel Background Colors
// ==========================================

val ChildDressingCardBg = Color(0xFFDBEAFE) // Light Blue
val ChildEatingCardBg = Color(0xFFFEF3C7)   // Light Amber
val ChildShoesCardBg = Color(0xFFD1FAE5)    // Light Green
val ChildRewardsCardBg = Color(0xFFFCE7F3)  // Light Pink

// Playful Accent Aliases for Child Mode
val ChildYellowStar = Color(0xFFF59E0B)
val ChildOrangePlay = Color(0xFFF97316)
val ChildGreenPlay = Color(0xFF10B981)
val ChildBlueCard = ChildDressingCardBg
val ChildPinkAccent = ChildRewardsCardBg