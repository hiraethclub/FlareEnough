package club.hiraeth.flareenough.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * Typography. Uses the system default font so it respects the user's font choice
 * and scales cleanly with the system font size setting.
 *
 * Body text is set a little larger than the Material default for easier reading,
 * which matters for tired eyes. Layouts must still work with system font scaling
 * up to 200 percent, so nothing here fixes a font size that cannot grow.
 */
private val Default = Typography()

val FlareTypography = Typography(
    bodyLarge = Default.bodyLarge.copy(fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = Default.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
    labelLarge = Default.labelLarge.copy(fontSize = 15.sp),
    titleLarge = Default.titleLarge.copy(fontSize = 24.sp),
    headlineSmall = Default.headlineSmall,
)

// A large style for the primary "next dose" text on the Today screen.
val EmphasisLarge: TextStyle = Default.headlineMedium.copy(fontSize = 30.sp)
