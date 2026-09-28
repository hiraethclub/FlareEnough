package club.hiraeth.flareenough.ui.dayedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.DoseStatus
import club.hiraeth.flareenough.ui.support.formatEpochDay
import club.hiraeth.flareenough.ui.support.formatInstantTime
import club.hiraeth.flareenough.ui.symptoms.BodyMapSection
import club.hiraeth.flareenough.ui.symptoms.FlareRow
import club.hiraeth.flareenough.ui.symptoms.NotesSection
import club.hiraeth.flareenough.ui.symptoms.PeriodSection
import club.hiraeth.flareenough.ui.symptoms.SectionTitle
import club.hiraeth.flareenough.ui.symptoms.TagsSection
import club.hiraeth.flareenough.ui.symptoms.TrackerCard
import club.hiraeth.flareenough.ui.symptoms.defaultLevelLabels
import club.hiraeth.flareenough.ui.today.DoseSlot
import club.hiraeth.flareenough.ui.today.SlotStatus

/**
 * Edit a whole day. Reached by tapping a day in History. It reuses the same logging
 * controls as the Today and Log screens, so nothing here behaves differently, it is
 * just pointed at the chosen date.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEditScreen(viewModel: DayEditViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val doses by viewModel.doseSlots.collectAsStateWithLifecycle()
    val rows by viewModel.trackerRows.collectAsStateWithLifecycle()
    val flare by viewModel.flare.collectAsStateWithLifecycle()
    val bodyMap by viewModel.bodyMap.collectAsStateWithLifecycle()
    val hidden by viewModel.hiddenBodyRegions.collectAsStateWithLifecycle()
    val periodEnabled by viewModel.periodTrackingEnabled.collectAsStateWithLifecycle()
    val periodFlow by viewModel.periodFlow.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val tags by viewModel.dayTags.collectAsStateWithLifecycle()
    val defaultLabels = defaultLevelLabels()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(formatEpochDay(context, viewModel.epochDay)) },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.day_edit_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item { SectionTitle(stringResource(R.string.day_edit_medications)) }
            if (doses.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.day_edit_no_medications),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(doses, key = { "${it.medicationId}-${it.scheduledTimeMillis}" }) { slot ->
                    DoseEditRow(slot = slot, onSet = { viewModel.setDoseStatus(slot, it) })
                }
            }

            item { FlareRow(flare = flare, onToggle = { viewModel.toggleFlare() }) }

            if (periodEnabled) {
                item { PeriodSection(flow = periodFlow, onSelect = { viewModel.setPeriodFlow(it) }) }
            }

            items(rows, key = { it.tracker.id }) { row ->
                TrackerCard(
                    tracker = row.tracker,
                    value = row.entry?.valueInt,
                    defaultLabels = defaultLabels,
                    onValue = { v -> viewModel.setValue(row.tracker.id, v) },
                )
            }

            item {
                BodyMapSection(
                    marks = bodyMap,
                    hidden = hidden,
                    onCycle = { viewModel.cycleRegion(it) },
                )
            }

            item { NotesSection(notes = notes, onAdd = { viewModel.addNote(it) }, onDelete = { viewModel.deleteNote(it) }) }
            item { TagsSection(tags = tags, onAdd = { viewModel.addTag(it) }, onRemove = { viewModel.removeTag(it) }) }
        }
    }
}

@Composable
private fun DoseEditRow(slot: DoseSlot, onSet: (DoseStatus?) -> Unit) {
    val context = LocalContext.current
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column {
                Text(slot.medicationName, style = MaterialTheme.typography.titleMedium)
                Text(
                    formatInstantTime(context, slot.scheduledTimeMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = slot.status == SlotStatus.TAKEN,
                    onClick = { onSet(if (slot.status == SlotStatus.TAKEN) null else DoseStatus.TAKEN) },
                    label = { Text(stringResource(R.string.action_taken)) },
                )
                FilterChip(
                    selected = slot.status == SlotStatus.SKIPPED,
                    onClick = { onSet(if (slot.status == SlotStatus.SKIPPED) null else DoseStatus.SKIPPED) },
                    label = { Text(stringResource(R.string.action_skip)) },
                )
            }
        }
    }
}
