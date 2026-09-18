package club.hiraeth.flareenough.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import club.hiraeth.flareenough.R

/**
 * Typography, using Quicksand, a soft rounded font under the Open Font License
 * (see licenses/Quicksand-OFL.txt). It is bundled, so it renders the same offline
 * on every device and needs no network. Quicksand is a variable font, so each
 * weight is the one file with a different weight axis value.
 *
 * Body text is set a little larger than the Material default for easier reading.
 * Layouts still work with system font scaling up to 200 percent.
 */
@OptIn(ExperimentalTextApi::class)
private fun quicksand(weight: Int): Font =
    Font(
        resId = R.font.quicksand,
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    )

val Quicksand: FontFamily = FontFamily(
    quicksand(400),
    quicksand(500),
    quicksand(600),
    quicksand(700),
)

private val Base = Typography()

private fun TextStyle.q(): TextStyle = copy(fontFamily = Quicksand)

val FlareTypography = Typography(
    displayLarge = Base.displayLarge.q(),
    displayMedium = Base.displayMedium.q(),
    displaySmall = Base.displaySmall.q(),
    headlineLarge = Base.headlineLarge.q(),
    headlineMedium = Base.headlineMedium.q(),
    headlineSmall = Base.headlineSmall.q(),
    titleLarge = Base.titleLarge.copy(fontFamily = Quicksand, fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = Base.titleMedium.copy(fontFamily = Quicksand, fontWeight = FontWeight.SemiBold),
    titleSmall = Base.titleSmall.q(),
    bodyLarge = Base.bodyLarge.copy(fontFamily = Quicksand, fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = Base.bodyMedium.copy(fontFamily = Quicksand, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = Base.bodySmall.q(),
    labelLarge = Base.labelLarge.copy(fontFamily = Quicksand, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = Base.labelMedium.q(),
    labelSmall = Base.labelSmall.q(),
)

// A large style for the primary "next dose" text on the Today screen.
val EmphasisLarge: TextStyle = Base.headlineMedium.copy(fontFamily = Quicksand, fontSize = 30.sp)
