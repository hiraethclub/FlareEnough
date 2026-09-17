package club.hiraeth.flareenough.ui.today

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.ui.common.PlaceholderScreen

@Composable
fun TodayScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.nav_today),
        subtitle = stringResource(R.string.placeholder_today),
        icon = Icons.Filled.Today,
        modifier = modifier,
    )
}
