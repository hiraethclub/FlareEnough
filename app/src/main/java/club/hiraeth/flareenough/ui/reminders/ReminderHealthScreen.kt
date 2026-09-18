package club.hiraeth.flareenough.ui.reminders

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.ui.support.appContainer
import club.hiraeth.flareenough.ui.support.formatMinutesOfDay
import java.time.Instant
import java.time.ZoneId

/**
 * Shows, with simple ticks and crosses, whether reminders can reach the person:
 * notifications allowed, exact alarms allowed, and battery limits off. It also
 * shows the next scheduled reminder and offers a one minute test.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderHealthScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { context.appContainer() }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-read the live states whenever the screen resumes, for example after the
    // person returns from a system settings page.
    var refresh by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationsOk = remember(refresh) {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    val exactOk = remember(refresh) { container.alarmScheduler.canScheduleExact() }
    val batteryOk = remember(refresh) {
        val pm = context.getSystemService<PowerManager>()
        pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
    }
    val nextReminder by produceState<Instant?>(initialValue = null, refresh) {
        value = container.reminderManager.nextReminderTime()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { refresh++ }

    var testScheduled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.reminder_health_title)) },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.reminder_health_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            CheckRow(
                label = stringResource(R.string.check_notifications),
                ok = notificationsOk,
                actionLabel = stringResource(R.string.action_allow),
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        context.startActivity(appNotificationSettings(context.packageName))
                    }
                },
            )

            CheckRow(
                label = stringResource(R.string.check_exact_alarms),
                ok = exactOk,
                actionLabel = stringResource(R.string.action_fix),
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM),
                        )
                    }
                },
            )

            CheckRow(
                label = stringResource(R.string.check_battery),
                ok = batteryOk,
                actionLabel = stringResource(R.string.action_fix),
                onAction = {
                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                },
            )

            NextReminderRow(nextReminder)

            Button(
                onClick = {
                    container.alarmScheduler.scheduleTest(System.currentTimeMillis() + 60_000L)
                    testScheduled = true
                    refresh++
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Text(stringResource(R.string.test_reminder_button))
            }
            if (testScheduled) {
                Text(
                    stringResource(R.string.test_reminder_scheduled),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                stringResource(R.string.battery_advice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://dontkillmyapp.com")),
                )
            }) {
                Text(stringResource(R.string.battery_advice_link))
            }
        }
    }
}

@Composable
private fun CheckRow(
    label: String,
    ok: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val statusRes = if (ok) R.string.status_ok else R.string.status_needs_attention
        Icon(
            imageVector = if (ok) Icons.Filled.CheckCircle else Icons.Filled.Error,
            contentDescription = stringResource(statusRes),
            tint = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        if (!ok) {
            OutlinedButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
private fun NextReminderRow(next: Instant?) {
    val context = LocalContext.current
    val text = if (next == null) {
        stringResource(R.string.check_next_none)
    } else {
        val zoned = next.atZone(ZoneId.systemDefault())
        val minutes = zoned.hour * 60 + zoned.minute
        formatMinutesOfDay(context, minutes)
    }
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.clearAndSetSemantics { },
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Text(
            text = stringResource(R.string.check_next_reminder),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun appNotificationSettings(packageName: String): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
