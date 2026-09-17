package club.hiraeth.flareenough.ui.support

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import club.hiraeth.flareenough.FlareApp
import club.hiraeth.flareenough.di.AppContainer

/** Get the app's dependency container from any Context. */
fun Context.appContainer(): AppContainer =
    (applicationContext as FlareApp).container

/** Remember the app container inside a composable, for building ViewModels. */
@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current
    return remember(context) { context.appContainer() }
}
