package club.hiraeth.flareenough.ui.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.ui.support.rememberAppContainer
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Backup, restore, and export. Everything here is driven by the person and stays on
 * the device: each action opens the system file picker so they choose exactly where a
 * file is saved or which file to read. There is no network, no account, no upload.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(onBack: () -> Unit) {
    val container = rememberAppContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel: DataViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                DataViewModel(container.backupManager, container.logExporter, container.pdfReporter)
            }
        },
    )

    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun report(result: Result<Unit>, successMessage: String) {
        if (result.isSuccess) {
            scope.launch { snackbarHostState.showSnackbar(successMessage) }
        } else {
            val e = result.exceptionOrNull()
            errorText = e?.let { "${it::class.java.simpleName}: ${it.message}" }
                ?: context.getString(R.string.data_unknown_error)
        }
    }

    // Opening the system file picker can throw straight away on some devices, for
    // example if there is no files app to handle it. Catch it so the app shows the
    // reason plainly instead of closing.
    fun safeLaunch(block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            errorText = "${t::class.java.simpleName}: ${t.message}"
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri -> uri?.let { viewModel.backUp(it) { r -> report(r, context.getString(R.string.data_backup_done)) } } }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> pendingRestoreUri = uri }

    val medicationCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri -> uri?.let { viewModel.exportMedicationCsv(it) { r -> report(r, context.getString(R.string.data_export_done)) } } }

    val dailyCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri -> uri?.let { viewModel.exportDailyCsv(it) { r -> report(r, context.getString(R.string.data_export_done)) } } }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri -> uri?.let { viewModel.exportPdf(it) { r -> report(r, context.getString(R.string.data_export_done)) } } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.data_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                stringResource(R.string.data_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            DataRow(
                icon = Icons.Filled.Backup,
                title = stringResource(R.string.data_backup),
                summary = stringResource(R.string.data_backup_summary),
                onClick = { safeLaunch { backupLauncher.launch(suggestedName("backup", "fenbackup")) } },
            )
            DataRow(
                icon = Icons.Filled.SettingsBackupRestore,
                title = stringResource(R.string.data_restore),
                summary = stringResource(R.string.data_restore_summary),
                onClick = { safeLaunch { restoreLauncher.launch(arrayOf("*/*")) } },
            )
            DataRow(
                icon = Icons.Filled.Medication,
                title = stringResource(R.string.data_export_medication),
                summary = stringResource(R.string.data_export_medication_summary),
                onClick = { safeLaunch { medicationCsvLauncher.launch(suggestedName("medication", "csv")) } },
            )
            DataRow(
                icon = Icons.Filled.EditNote,
                title = stringResource(R.string.data_export_daily),
                summary = stringResource(R.string.data_export_daily_summary),
                onClick = { safeLaunch { dailyCsvLauncher.launch(suggestedName("daily-log", "csv")) } },
            )
            DataRow(
                icon = Icons.Filled.PictureAsPdf,
                title = stringResource(R.string.data_export_pdf),
                summary = stringResource(R.string.data_export_pdf_summary),
                onClick = { safeLaunch { pdfLauncher.launch(suggestedName("report", "pdf")) } },
            )
        }
    }

    val restoreUri = pendingRestoreUri
    if (restoreUri != null) {
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text(stringResource(R.string.data_restore_confirm_title)) },
            text = { Text(stringResource(R.string.data_restore_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestoreUri = null
                    viewModel.restore(restoreUri) { r ->
                        if (r.isSuccess) {
                            restartApp(context)
                        } else {
                            report(r, "")
                        }
                    }
                }) {
                    Text(stringResource(R.string.data_restore_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    val error = errorText
    if (error != null) {
        AlertDialog(
            onDismissRequest = { errorText = null },
            title = { Text(stringResource(R.string.data_error_title)) },
            text = { SelectionContainer { Text(error) } },
            confirmButton = {
                TextButton(onClick = { errorText = null }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
        )
    }
}

@Composable
private fun DataRow(
    icon: ImageVector,
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.clearAndSetSemantics { },
            tint = MaterialTheme.colorScheme.primary,
        )
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A friendly default file name, dated, that the person can change in the picker. */
private fun suggestedName(part: String, extension: String): String =
    "flare-enough-$part-${LocalDate.now()}.$extension"

/**
 * Fully restart the app after a restore, so Room reopens the freshly written database
 * from a clean process rather than the one that held the old file open.
 */
private fun restartApp(context: Context) {
    val appContext = context.applicationContext
    val intent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
    if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        appContext.startActivity(intent)
    }
    Runtime.getRuntime().exit(0)
}
