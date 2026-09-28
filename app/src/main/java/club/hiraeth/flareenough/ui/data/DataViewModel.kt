package club.hiraeth.flareenough.ui.data

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.flareenough.data.backup.BackupManager
import club.hiraeth.flareenough.export.LogExporter
import club.hiraeth.flareenough.export.PdfReporter
import kotlinx.coroutines.launch

/**
 * Runs the backup, restore, and export jobs off the UI. Each takes the file the person
 * chose in the system picker and reports back through [onResult] on the main thread,
 * so the screen can show a short, plain message. Nothing happens without a file the
 * person picked, and nothing leaves the device on its own.
 */
class DataViewModel(
    private val backupManager: BackupManager,
    private val logExporter: LogExporter,
    private val pdfReporter: PdfReporter,
) : ViewModel() {

    fun backUp(uri: Uri, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch { onResult(backupManager.exportTo(uri)) }
    }

    fun restore(uri: Uri, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch { onResult(backupManager.importFrom(uri)) }
    }

    fun exportMedicationCsv(uri: Uri, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch { onResult(logExporter.exportMedicationLog(uri)) }
    }

    fun exportDailyCsv(uri: Uri, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch { onResult(logExporter.exportDailyLog(uri)) }
    }

    fun exportPdf(uri: Uri, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch { onResult(pdfReporter.writeReport(uri)) }
    }
}
