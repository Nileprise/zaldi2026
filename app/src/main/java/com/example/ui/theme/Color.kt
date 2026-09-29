package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Raw Color Primitives
// Define the absolute color values here. Do not use these directly in UI components.
// ============================================================================

// Brand: Electric Blue
val Blue50 = Color(0xFFEFF6FF)   // AccentBlueContainer (Light)
val Blue100 = Color(0xFFDBEAFE)
val Blue500 = Color(0xFF3B82F6)  // Primary (Dark Mode)
val Blue600 = Color(0xFF2563EB)  // Primary (Light Mode)
val Blue900 = Color(0xFF1E3A8A)  // OnAccentBlueContainer

// Brand: Slate (Grays)
val Slate50 = Color(0xFFF8FAFC)  // Background (Light)
val Slate100 = Color(0xFFF1F5F9) // SurfaceVariant (Light)
val Slate200 = Color(0xFFE2E8F0) // Outline (Light)
val Slate500 = Color(0xFF64748B) // TextMuted
val Slate700 = Color(0xFF334155) // SurfaceVariant (Dark)
val Slate800 = Color(0xFF1E293B) // Surface (Dark)
val Slate900 = Color(0xFF0F172A) // Background (Dark) / TextDark (Light)

// Semantic: Error
val Red100 = Color(0xFFFEE2E2)
val Red600 = Color(0xFFDC2626)
val Red800 = Color(0xFF991B1B)
val Red300 = Color(0xFFFCA5A5) // Error (Dark Mode)

// Semantic: Success & Warning (For custom extensions)
val Emerald100 = Color(0xFFD1FAE5)
val Emerald600 = Color(0xFF059669)
val Emerald300 = Color(0xFF6EE7B7) // Success (Dark Mode)

val Amber100 = Color(0xFFFEF3C7)
val Amber600 = Color(0xFFD97706)
val Amber300 = Color(0xFFFCD34D) // Warning (Dark Mode)
