package com.bhumap.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Note: For custom fonts (Plus Jakarta Sans), add .ttf files to
// composeApp/src/commonMain/composeResources/font/ and load with
// FontFamily(Font(Res.font.plus_jakarta_sans_regular))
// Using system default here until font files are added.

val BhumapTypography = Typography(
    // Display
    displayLarge = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.Bold,
        fontSize    = 57.sp,
        lineHeight  = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
