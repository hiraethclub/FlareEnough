package club.hiraeth.flareenough.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector
import club.hiraeth.flareenough.R

/**
 * The four bottom navigation destinations, in order. Each has a route, a one word
 * label (from strings.xml, so it can be translated), and an icon. Icon plus word
 * together, never icon alone, so the meaning is never carried by a picture only.
 */
enum class TopDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    TODAY("today", R.string.nav_today, Icons.Filled.Today),
    LOG("log", R.string.nav_log, Icons.Filled.EditNote),
    HISTORY("history", R.string.nav_history, Icons.Filled.CalendarMonth),
    STILLNESS("stillness", R.string.nav_stillness, Icons.Filled.SelfImprovement),
}

/** Route names that are reached outside the bottom bar. */
object Routes {
    const val SETTINGS = "settings"

    /** The list of medications. */
    const val MEDICATIONS = "medications"

    /** The reminder health check screen. */
    const val REMINDER_HEALTH = "reminder_health"

    /** Add or edit a medication. medId 0 means add a new one. */
    const val MEDICATION_EDIT = "medication_edit"
    const val MEDICATION_EDIT_ARG = "medId"
    const val MEDICATION_EDIT_PATTERN = "$MEDICATION_EDIT/{$MEDICATION_EDIT_ARG}"

    fun medicationEdit(medId: Long): String = "$MEDICATION_EDIT/$medId"
}
