package club.hiraeth.flareenough.di

import android.content.Context

/**
 * Manual dependency injection container.
 *
 * This is the whole wiring of the app in one readable place. There is no Hilt and
 * no generated code. When a repository or service is added, create it here and
 * expose it as a property. Screens reach it through their ViewModels, and
 * background components (alarm receivers, boot receiver) reach it with:
 *
 *     (context.applicationContext as FlareApp).container
 *
 * Everything is created lazily, so nothing is built until it is first used.
 */
class AppContainer(private val appContext: Context) {

    // Repositories, the database, the alarm scheduler, and settings are added
    // here in later milestones. Kept empty on purpose for the setup milestone.
}
