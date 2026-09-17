package club.hiraeth.flareenough.core

import androidx.annotation.StringRes
import club.hiraeth.flareenough.R

/**
 * Single source of truth for the app name.
 *
 * The display name lives in [R.string.app_name] so it can be translated. This
 * constant points at that resource so any code that needs the name in one place
 * uses the same value. Change the name in strings.xml and everywhere follows.
 */
object AppInfo {
    @StringRes
    val NAME_RES: Int = R.string.app_name
}
