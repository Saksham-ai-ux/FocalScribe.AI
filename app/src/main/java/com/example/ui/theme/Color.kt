package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

fun CyberSurfaceBrush() = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E1E26),
        Color(0xFF131318)
    )
)

// --- Modern Cyber Slate Theme Colors ---
val CyberBg = Color(0xFF0A0A0C)
val CyberSurface = Color(0xFF131318)
val CyberSurfaceVariant = Color(0xFF1C1C24)

val CyberTeal = Color(0xFF00E5FF)       // Electric Cyan / Teal
val CyberIndigo = Color(0xFF7C4DFF)     // Rich Purple-Indigo
val CyberPink = Color(0xFFFF4081)       // Punchy Accent Magenta

val TextPrimary = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)

val GreenSuccess = Color(0xFF00E676)
val RedError = Color(0xFFFF1744)

// Classic Material Theme variables mappings
val Purple80 = CyberTeal
val PurpleGrey80 = CyberIndigo
val Pink80 = CyberPink

val Purple40 = Color(0xFF00ACC1)
val PurpleGrey40 = Color(0xFF5E35B1)
val Pink40 = Color(0xFFD81B60)
