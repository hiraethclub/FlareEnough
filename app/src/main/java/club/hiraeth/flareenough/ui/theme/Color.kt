package club.hiraeth.flareenough.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The Flare Enough palette. Calm, soft, muted pastels.
 *
 * Rules from the brief, kept here on purpose:
 * - No bright red anywhere. Attention uses muted terracotta or amber.
 * - Text is deep slate or charcoal, never grey on pastel, so it meets WCAG AA
 *   contrast on the soft surfaces.
 * These pairings are chosen so text on each surface stays comfortably readable.
 */

// Core pastels.
val Sage = Color(0xFF7E9B84)          // primary, muted green
val SageDark = Color(0xFF52705A)
val DustyLavender = Color(0xFF9285AE) // secondary
val PowderBlue = Color(0xFF7FA0B8)    // tertiary
val PalePeach = Color(0xFFF3D9C4)
val WarmCream = Color(0xFFF6F1E7)     // light background

// Attention, muted, never bright red.
val Terracotta = Color(0xFFB56A4E)
val Amber = Color(0xFFC9A24B)

// Text and neutrals for the light theme.
val Charcoal = Color(0xFF2B2B2E)      // main text on light surfaces
val Slate = Color(0xFF3E4348)
val CreamSurface = Color(0xFFFBF8F1)
val CreamSurfaceVariant = Color(0xFFE9E2D4)
val OutlineLight = Color(0xFF7C766A)

// Dark theme. Deep blue grey backgrounds with desaturated pastel accents.
val DeepBlueGrey = Color(0xFF1E2328)  // dark background
val DeepBlueGreySurface = Color(0xFF262C33)
val DeepBlueGreySurfaceVariant = Color(0xFF39414A)
val Mist = Color(0xFFE6E7E9)          // main text on dark surfaces
val OutlineDark = Color(0xFF8A8F96)

// Desaturated pastel accents for dark.
val SageMuted = Color(0xFFA9C4AF)
val LavenderMuted = Color(0xFFBCB0D6)
val PowderMuted = Color(0xFFA7C3D8)
val TerracottaMuted = Color(0xFFD79A82)
