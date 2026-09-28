package club.hiraeth.flareenough.di

import android.content.Context
import club.hiraeth.flareenough.data.backup.BackupManager
import club.hiraeth.flareenough.data.db.FlareDatabase
import club.hiraeth.flareenough.data.repository.DayRepository
import club.hiraeth.flareenough.data.repository.DoseRepository
import club.hiraeth.flareenough.data.repository.MedicationRepository
import club.hiraeth.flareenough.data.repository.MeditationRepository
import club.hiraeth.flareenough.data.repository.StillnessContentRepository
import club.hiraeth.flareenough.data.repository.SymptomRepository
import club.hiraeth.flareenough.data.settings.SettingsRepository
import club.hiraeth.flareenough.export.LogExporter
import club.hiraeth.flareenough.export.PdfReporter
import club.hiraeth.flareenough.reminders.AlarmScheduler
import club.hiraeth.flareenough.reminders.ReminderManager

/**
 * Manual dependency injection container.
 *
 * This is the whole wiring of the app in one readable place. There is no Hilt and
 * no generated code. Screens reach these through their ViewModels, and background
 * components (alarm receivers, boot receiver) reach them with:
 *
 *     (context.applicationContext as FlareApp).container
 *
 * Everything is created lazily, so nothing is built until it is first used.
 */
class AppContainer(private val appContext: Context) {

    private val database: FlareDatabase by lazy { FlareDatabase.build(appContext) }

    val medicationRepository: MedicationRepository by lazy {
        MedicationRepository(database.medicationDao())
    }

    val doseRepository: DoseRepository by lazy {
        DoseRepository(database.doseDao(), database.medicationDao())
    }

    val symptomRepository: SymptomRepository by lazy {
        SymptomRepository(database.symptomDao())
    }

    val dayRepository: DayRepository by lazy {
        DayRepository(database.dayDao())
    }

    val meditationRepository: MeditationRepository by lazy {
        MeditationRepository(database.meditationDao())
    }

    val stillnessContentRepository: StillnessContentRepository by lazy {
        StillnessContentRepository(database.stillnessContentDao())
    }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }

    // Backup, restore, and exports. All are user driven and stay on the device: the
    // person chooses the file through the system picker every time.
    val backupManager: BackupManager by lazy { BackupManager(appContext, database) }

    val logExporter: LogExporter by lazy {
        LogExporter(
            appContext,
            database.medicationDao(),
            database.doseDao(),
            database.symptomDao(),
            database.dayDao(),
        )
    }

    val pdfReporter: PdfReporter by lazy {
        PdfReporter(
            appContext,
            database.medicationDao(),
            database.doseDao(),
            database.symptomDao(),
            database.dayDao(),
        )
    }

    val alarmScheduler: AlarmScheduler by lazy { AlarmScheduler(appContext) }

    val reminderManager: ReminderManager by lazy {
        ReminderManager(appContext, medicationRepository, alarmScheduler)
    }
}
