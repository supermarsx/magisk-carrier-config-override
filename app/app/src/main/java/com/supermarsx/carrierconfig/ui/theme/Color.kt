package com.supermarsx.carrierconfig.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Glassmorphism Dark Theme Color Palette for CCO
 * Based on spec-design.md
 */

// Background Colors
val BackgroundDeepDark = Color(0xFF0A0E14)
val BackgroundDark = Color(0xFF12161E)
val BackgroundElevated = Color(0xFF1A1F2B)

// Glass Surface Overlays (with transparency)
val GlassSurface = Color(0x1AFFFFFF)          // 10% white - subtle glass
val GlassSurfaceMedium = Color(0x33FFFFFF)    // 20% white - medium glass
val GlassSurfaceStrong = Color(0x4DFFFFFF)    // 30% white - strong glass
val GlassSurfaceSubtle = Color(0x0DFFFFFF)    // 5% white - very subtle
val GlassBorder = Color(0x33FFFFFF)           // 20% white - border color
val GlassTint = GlassSurfaceMedium

// Gradient Overlays
val GradientTop = Color(0xFF0D1B2E)
val GradientBottom = Color(0xFF1A0B1E)

// Primary Accent (Cyan/Electric Blue)
val AccentPrimary = Color(0xFF00D9FF)
val AccentPrimaryLight = Color(0xFF6FEFFF)
val AccentPrimaryDark = Color(0xFF0099CC)
val AccentPrimaryGlow = Color(0x4D00D9FF)     // 30% alpha for glow
val AccentCyan = AccentPrimary

// Secondary Accent (Purple/Magenta)
val AccentSecondary = Color(0xFFB24BF3)
val AccentSecondaryLight = Color(0xFFD896FF)
val AccentSecondaryDark = Color(0xFF8B2FC9)
val AccentSecondaryGlow = Color(0x4DB24BF3)
val AccentPurple = AccentSecondary

// Success Accent (Neon Green)
val AccentSuccess = Color(0xFF00FF88)
val AccentSuccessGlow = Color(0x4D00FF88)

// Warning Accent (Amber)
val AccentWarning = Color(0xFFFFB020)
val AccentWarningGlow = Color(0x4DFFB020)

// Error Accent (Red/Pink)
val AccentError = Color(0xFFFF3366)
val AccentErrorGlow = Color(0x4DFF3366)

// Text Colors
val TextPrimary = Color(0xFFFFFFFF)           // Pure white
val TextSecondary = Color(0xCCFFFFFF)         // 80% white
val TextTertiary = Color(0x99FFFFFF)          // 60% white
val TextDisabled = Color(0x66FFFFFF)          // 40% white
val TextHint = TextSecondary

// Accent Text
val TextAccent = Color(0xFF00D9FF)
val TextSuccess = Color(0xFF00FF88)
val TextWarning = Color(0xFFFFB020)
val TextError = Color(0xFFFF3366)
