package club.hiraeth.flareenough.ui.log

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.ui.common.PlaceholderScreen

@Composable
fun LogScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.nav_log),
        subtitle = stringResource(R.string.placeholder_log),
        icon = Icons.Filled.EditNote,
        modifier = modifier,
    )
}
