package club.hiraeth.flareenough

import android.app.Application
import club.hiraeth.flareenough.di.AppContainer

/**
 * The Application. It owns the single [AppContainer] that holds the app's wiring.
 *
 * Any component can reach the container with:
 *     (context.applicationContext as FlareApp).container
 */
class FlareApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
