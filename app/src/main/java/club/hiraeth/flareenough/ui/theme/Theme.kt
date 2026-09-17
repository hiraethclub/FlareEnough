package club.hiraeth.flareenough.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * The Flare Enough Material 3 theme.
 *
 * The calm muted palette is the default. Material You dynamic colour is optional
 * and off by default, so the app looks the same calm way on every phone unless
 * the user turns dynamic colour on.
 */

private val LightColors = lightColorScheme(
    primary = Sage,
    onPrimary = Color.White,
    primaryContainer = PalePeach,
    onPrimaryContainer = Charcoal,
    secondary = DustyLavender,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4DDF0),
    onSecondaryContainer = Charcoal,
    tertiary = PowderBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCE7F0),
    onTertiaryContainer = Charcoal,
    background = WarmCream,
    onBackground = Charcoal,
    surface = CreamSurface,
    onSurface = Charcoal,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = Slate,
    outline = OutlineLight,
    // Attention colour. Muted terracotta, never bright red.
    error = Terracotta,
    onError = Color.White,
    errorContainer = Color(0xFFF2DCD2),
    onErrorContainer = Color(0xFF5C2E1E),
)

private val DarkColors = darkColorScheme(
    primary = SageMuted,
    onPrimary = Color(0xFF16281B),
    primaryContainer = SageDark,
    onPrimaryContainer = Color(0xFFDCEBE0),
    secondary = LavenderMuted,
    onSecondary = Color(0xFF25203A),
    secondaryContainer = Color(0xFF453D5C),
    onSecondaryContainer = Color(0xFFEAE3F7),
    tertiary = PowderMuted,
    onTertiary = Color(0xFF122530),
    tertiaryContainer = Color(0xFF33505F),
    onTertiaryContainer = Color(0xFFDBEAF3),
    background = DeepBlueGrey,
    onBackground = Mist,
    surface = DeepBlueGreySurface,
    onSurface = Mist,
    surfaceVariant = DeepBlueGreySurfaceVariant,
    onSurfaceVariant = Color(0xFFC7CBD0),
    outline = OutlineDark,
    error = TerracottaMuted,
    onError = Color(0xFF3B160A),
    errorContainer = Color(0xFF6B3B29),
    onErrorContainer = Color(0xFFF6DBD0),
)

@Composable
fun FlareEnoughTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Off by default, so the calm palette is what people see unless they choose
    // Material You dynamic colour in settings.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FlareTypography,
        shapes = FlareShapes,
        content = content,
    )
}
