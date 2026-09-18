package club.hiraeth.flareenough.ui.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.ui.common.TimePickerDialog
import club.hiraeth.flareenough.ui.support.formatInstantTime
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    remindersMayBeLate: Boolean,
    onOpenReminderHealth: () -> Unit,
    onQuickSymptom: () -> Unit,
    onOpenStillness: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val takenLabel = stringResource(R.string.snackbar_taken)
    val skippedLabel = stringResource(R.string.snackbar_skipped)
    val undoLabel = stringResource(R.string.action_undo)

    var takenEarlierFor by remember { mutableStateOf<DoseSlot?>(null) }

    fun logWithUndo(message: String, action: suspend () -> Long) {
        scope.launch {
            val id = action()
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undo(id)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (remindersMayBeLate) {
                item { RemindersLateBanner(onClick = onOpenReminderHealth) }
            }

            item {
                NextDoseCard(
                    nextDose = state.nextDose,
                    hasAnyDoses = state.doses.isNotEmpty(),
                    onTaken = { slot -> logWithUndo(takenLabel) { viewModel.logTaken(slot) } },
                    onTakenEarlier = { slot -> takenEarlierFor = slot },
                    onSkip = { slot -> logWithUndo(skippedLabel) { viewModel.logSkipped(slot) } },
                )
            }

            if (state.doses.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.today_todays_doses),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                items(state.doses, key = { "${it.medicationId}-${it.scheduledTimeMillis}" }) { slot ->
                    DoseRow(
                        slot = slot,
                        onTaken = { logWithUndo(takenLabel) { viewModel.logTaken(slot) } },
                        onSkip = { logWithUndo(skippedLabel) { viewModel.logSkipped(slot) } },
                    )
                }
            }

            item {
                HowAreYouCard(onClick = onQuickSymptom)
            }
            item {
                StillnessShortcut(onClick = onOpenStillness)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    takenEarlierFor?.let { slot ->
        val context = LocalContext.current
        val zoned = Instant.ofEpochMilli(slot.scheduledTimeMillis).atZone(ZoneId.systemDefault())
        TimePickerDialog(
            initialMinutes = zoned.hour * 60 + zoned.minute,
            onConfirm = { minutes ->
                takenEarlierFor = null
                logWithUndo(takenLabel) { viewModel.logTakenAt(slot, minutes) }
            },
            onDismiss = { takenEarlierFor = null },
        )
    }
}

@Composable
private fun RemindersLateBanner(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                modifier = Modifier.clearAndSetSemantics { },
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                stringResource(R.string.reminders_late_banner),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun NextDoseCard(
    nextDose: DoseSlot?,
    hasAnyDoses: Boolean,
    onTaken: (DoseSlot) -> Unit,
    onTakenEarlier: (DoseSlot) -> Unit,
    onSkip: (DoseSlot) -> Unit,
) {
    val context = LocalContext.current
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.today_next_dose),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (nextDose == null) {
                Text(
                    stringResource(
                        if (hasAnyDoses) R.string.today_nothing_left else R.string.today_no_medications,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Text(
                    text = nextDose.medicationName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatInstantTime(context, nextDose.scheduledTimeMillis),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = { onTaken(nextDose) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                ) {
                    Text(stringResource(R.string.action_taken))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onTakenEarlier(nextDose) }) {
                        Text(stringResource(R.string.today_taken_earlier))
                    }
                    TextButton(onClick = { onSkip(nextDose) }) {
                        Text(stringResource(R.string.action_skip))
                    }
                }
            }
        }
    }
}

@Composable
private fun DoseRow(
    slot: DoseSlot,
    onTaken: () -> Unit,
    onSkip: () -> Unit,
) {
    val context = LocalContext.current
    val (icon, tint, statusRes) = when (slot.status) {
        SlotStatus.TAKEN -> Triple(Icons.Filled.CheckCircle, MaterialTheme.colorScheme.primary, R.string.status_taken)
        SlotStatus.SKIPPED -> Triple(Icons.Filled.RemoveCircleOutline, MaterialTheme.colorScheme.onSurfaceVariant, R.string.status_skipped)
        SlotStatus.PLANNED -> Triple(Icons.Filled.RadioButtonUnchecked, MaterialTheme.colorScheme.onSurfaceVariant, R.string.status_planned)
    }
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(statusRes),
            tint = tint,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(slot.medicationName, style = MaterialTheme.typography.bodyLarge)
            Text(
                formatInstantTime(context, slot.scheduledTimeMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (slot.status == SlotStatus.PLANNED) {
            TextButton(onClick = onSkip) { Text(stringResource(R.string.action_skip)) }
            Button(onClick = onTaken) { Text(stringResource(R.string.action_taken)) }
        }
    }
}

@Composable
private fun HowAreYouCard(onClick: () -> Unit) {
    ShortcutCard(
        icon = Icons.Filled.SentimentSatisfied,
        title = stringResource(R.string.today_how_are_you),
        subtitle = stringResource(R.string.today_how_are_you_sub),
        onClick = onClick,
    )
}

@Composable
private fun StillnessShortcut(onClick: () -> Unit) {
    ShortcutCard(
        icon = Icons.Filled.SelfImprovement,
        title = stringResource(R.string.today_stillness),
        subtitle = stringResource(R.string.today_stillness_sub),
        onClick = onClick,
    )
}

@Composable
private fun ShortcutCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
