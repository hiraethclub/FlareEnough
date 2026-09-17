package club.hiraeth.flareenough.ui.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.ui.common.PlaceholderScreen

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.nav_history),
        subtitle = stringResource(R.string.placeholder_history),
        icon = Icons.Filled.CalendarMonth,
        modifier = modifier,
    )
}
