package club.hiraeth.flareenough.ui.stillness

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.ui.common.PlaceholderScreen

@Composable
fun StillnessScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.nav_stillness),
        subtitle = stringResource(R.string.placeholder_stillness),
        icon = Icons.Filled.SelfImprovement,
        modifier = modifier,
    )
}
