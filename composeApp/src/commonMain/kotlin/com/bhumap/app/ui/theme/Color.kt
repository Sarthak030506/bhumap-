package com.bhumap.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─── BhuMap Brand Palette ─────────────────────────────────────────────────────
val Evergreen900  = Color(0xFF0D3D2C)
val Evergreen800  = Color(0xFF155540)
val Evergreen700  = Color(0xFF1A6147)
val Evergreen     = Color(0xFF1F6F50)  // Primary
val Evergreen400  = Color(0xFF3D8F6D)
val Evergreen200  = Color(0xFF93C9B2)
val Evergreen50   = Color(0xFFE8F5EF)

val Terracotta    = Color(0xFFC8552B)  // Accent / Error
val Terracotta200 = Color(0xFFEDAF9A)
val Terracotta50  = Color(0xFFFAEDE8)

val Paper50       = Color(0xFFFBF7F0)  // Surface / Background
val Paper100      = Color(0xFFF3EDE3)
val Paper200      = Color(0xFFE8DDD0)

val Soil900       = Color(0xFF2A1F14)  // On-surface text
val Soil700       = Color(0xFF4A3728)
val Soil500       = Color(0xFF7A6254)
val Soil300       = Color(0xFFBAADA3)

val Amber500      = Color(0xFFF59E0B)  // Reserved plot
val Orange500     = Color(0xFFF97316)  // Sold-pending plot
val Slate500      = Color(0xFF64748B)  // Blocked plot

// ─── Material 3 Light Scheme Seeds ───────────────────────────────────────────
// Built from Evergreen as primary seed
val md_theme_light_primary            = Evergreen
val md_theme_light_onPrimary          = Color.White
val md_theme_light_primaryContainer   = Evergreen50
val md_theme_light_onPrimaryContainer = Evergreen900
val md_theme_light_secondary          = Terracotta
val md_theme_light_onSecondary        = Color.White
val md_theme_light_secondaryContainer = Terracotta50
val md_theme_light_onSecondaryContainer = Terracotta
val md_theme_light_error              = Terracotta
val md_theme_light_errorContainer     = Terracotta50
val md_theme_light_onError            = Color.White
val md_theme_light_onErrorContainer   = Terracotta
val md_theme_light_background         = Paper50
val md_theme_light_onBackground       = Soil900
val md_theme_light_surface            = Paper50
val md_theme_light_onSurface          = Soil900
val md_theme_light_surfaceVariant     = Paper100
val md_theme_light_onSurfaceVariant   = Soil700
val md_theme_light_outline            = Soil300
val md_theme_light_outlineVariant     = Paper200
val md_theme_light_inverseSurface     = Soil900
val md_theme_light_inverseOnSurface   = Paper50
val md_theme_light_inversePrimary     = Evergreen200
