package club.hiraeth.flareenough.ui.symptoms

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.TrackerType

@StringRes
fun TrackerType.labelRes(): Int = when (this) {
    TrackerType.FIVE_LEVEL -> R.string.type_five_level
    TrackerType.YES_NO -> R.string.type_yes_no
    TrackerType.DURATION -> R.string.type_duration
    TrackerType.NUMBER -> R.string.type_number
}

/** The default five level labels, in order 1 to 5. Used for starter and new trackers. */
@Composable
fun defaultLevelLabels(): List<String> = listOf(
    stringResource(R.string.level_none),
    stringResource(R.string.level_mild),
    stringResource(R.string.level_moderate),
    stringResource(R.string.level_bad),
    stringResource(R.string.level_severe),
)
