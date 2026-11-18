package com.carexpress.bizdev.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.carexpress.bizdev.data.local.Segment
import com.carexpress.bizdev.data.local.Stage

// ---- Exact brand palette from the source tracker ----
val BrandTeal = Color(0xFF005046)
val AccentTeal = Color(0xFF026B5D)
val MintSurface = Color(0xFFF5FBF7)
val AppBackground = Color(0xFFF8FAF9)
val White = Color(0xFFFFFFFF)
val Divider = Color(0xFFE0EAE5)
val InkPrimary = Color(0xFF0F2A25)
val InkSecondary = Color(0xFF5B6F6A)
val Emerald = Color(0xFF166534)

data class ChipColors(val bg: Color, val fg: Color)

val Stage.colors: ChipColors
    get() = when (this) {
        Stage.INITIAL_CONTACT -> ChipColors(Color(0xFFE2E8F0), Color(0xFF334155))
        Stage.PROPOSAL_SENT -> ChipColors(Color(0xFFFEF3C7), Color(0xFF92400E))
        Stage.NEGOTIATION_PRAZ -> ChipColors(Color(0xFFCCFBF1), Color(0xFF115E59))
        Stage.CLOSED_WON -> ChipColors(Color(0xFFDCFCE7), Color(0xFF166534))
        Stage.ON_HOLD_LOST -> ChipColors(Color(0xFFFEE2E2), Color(0xFF991B1B))
    }

val Segment.colors: ChipColors
    get() = when (this) {
        Segment.PRIVATE_HOSPITAL -> ChipColors(Color(0xFFE6F4F1), BrandTeal)
        Segment.CORPORATE_BANK -> ChipColors(Color(0xFFEAF0FB), Color(0xFF1E3A8A))
        Segment.GOVERNMENT -> ChipColors(Color(0xFFF3EEFB), Color(0xFF5B21B6))
        Segment.PARASTATAL -> ChipColors(Color(0xFFFFF1E6), Color(0xFF9A3412))
        Segment.EMBASSY_NGO -> ChipColors(Color(0xFFEAF6FD), Color(0xFF075985))
    }

fun leadScoreColors(score: Int): ChipColors = when (score) {
    5 -> ChipColors(BrandTeal, White)
    4 -> ChipColors(Emerald, White)
    3 -> ChipColors(Color(0xFFFEF3C7), Color(0xFF92400E))
    else -> ChipColors(Color(0xFFE5E7EB), Color(0xFF4B5563))
}

// ---- Typography: geometric sans + tabular figures for numbers ----
private val Sans = FontFamily.SansSerif // Roboto on Android

/** Apply to any currency / count / percentage text. */
val TabularNums = TextStyle(fontFeatureSettings = "tnum")

private val AppTypography = Typography(
    headlineMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 26.sp, fontFeatureSettings = "tnum"),
    headlineSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFeatureSettings = "tnum"),
    titleLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = Sans, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, fontFeatureSettings = "tnum"),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, fontFeatureSettings = "tnum"),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 11.sp, fontFeatureSettings = "tnum"),
)

private val Scheme = lightColorScheme(
    primary = BrandTeal,
    onPrimary = White,
    primaryContainer = Color(0xFFCDEBE3),
    onPrimaryContainer = BrandTeal,
    secondary = AccentTeal,
    onSecondary = White,
    secondaryContainer = Color(0xFFD7EFE8),
    onSecondaryContainer = BrandTeal,
    background = AppBackground,
    onBackground = InkPrimary,
    surface = White,
    onSurface = InkPrimary,
    surfaceVariant = MintSurface,
    onSurfaceVariant = InkSecondary,
    surfaceContainerLow = MintSurface,
    surfaceContainer = MintSurface,
    surfaceContainerHigh = White,
    outline = Color(0xFFB9CCC5),
    outlineVariant = Divider,
)

@Composable
fun BizdevTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = AppTypography, content = content)
}
