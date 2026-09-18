package club.hiraeth.flareenough.ui.symptoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.BodyRegion
import club.hiraeth.flareenough.data.db.entity.BodyState
import club.hiraeth.flareenough.data.db.entity.SymptomTrackerEntity
import club.hiraeth.flareenough.data.db.entity.TrackerType

@Composable
fun SymptomLogScreen(
    viewModel: SymptomLogViewModel,
    modifier: Modifier = Modifier,
) {
    val rows by viewModel.trackerRows.collectAsStateWithLifecycle()
    val noTrackers by viewModel.noTrackersAtAll.collectAsStateWithLifecycle()
    val flare by viewModel.flare.collectAsStateWithLifecycle()
    val bodyMap by viewModel.bodyMap.collectAsStateWithLifecycle()
    val dayTags by viewModel.dayTags.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    val defaultLabels = defaultLevelLabels()
    var showAddTracker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (noTrackers) {
            item { StarterSetCard(defaultLabels) { trackers -> viewModel.addTrackers(trackers) } }
        }

        item { FlareRow(flare = flare, onToggle = { viewModel.toggleFlare() }) }
        item {
            OutlinedButton(
                onClick = { viewModel.sameAsYesterday() },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Text(stringResourceCompat(R.string.symptom_same_as_yesterday))
            }
        }

        items(rows, key = { it.tracker.id }) { row ->
            TrackerCard(
                tracker = row.tracker,
                value = row.entry?.valueInt,
                defaultLabels = defaultLabels,
                onValue = { v -> viewModel.setValue(row.tracker.id, v, null) },
            )
        }

        item {
            OutlinedButton(
                onClick = { showAddTracker = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(
                    stringResourceCompat(R.string.symptom_add_tracker),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        item { BodyMapSection(marks = bodyMap, onCycle = { viewModel.cycleRegion(it) }) }
        item { NotesSection(notes = notes, onAdd = { viewModel.addNote(it) }, onDelete = { viewModel.deleteNote(it) }) }
        item { TagsSection(tags = dayTags, onAdd = { viewModel.addTag(it) }, onRemove = { viewModel.removeTag(it) }) }
    }

    if (showAddTracker) {
        AddTrackerDialog(
            defaultLabels = defaultLabels,
            onDismiss = { showAddTracker = false },
            onAdd = { tracker ->
                viewModel.addTracker(tracker)
                showAddTracker = false
            },
        )
    }
}

// A tiny wrapper so the file reads cleanly. Just stringResource.
@Composable
private fun stringResourceCompat(id: Int): String = androidx.compose.ui.res.stringResource(id)

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
private fun FlareRow(flare: Boolean, onToggle: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResourceCompat(R.string.symptom_flare),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            Switch(checked = flare, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun TrackerCard(
    tracker: SymptomTrackerEntity,
    value: Int?,
    defaultLabels: List<String>,
    onValue: (Int?) -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(tracker.name, style = MaterialTheme.typography.titleMedium)
            when (tracker.type) {
                TrackerType.FIVE_LEVEL -> FiveLevelInput(
                    labels = tracker.levelLabels?.takeIf { it.size == 5 } ?: defaultLabels,
                    selected = value,
                    onSelect = { onValue(it) },
                )
                TrackerType.YES_NO -> YesNoInput(selected = value, onSelect = { onValue(it) })
                TrackerType.DURATION -> DurationInput(
                    options = tracker.durationOptions.orEmpty(),
                    selected = value,
                    onSelect = { onValue(it) },
                )
                TrackerType.NUMBER -> NumberInput(
                    value = value,
                    unit = tracker.unit,
                    onChange = { onValue(it) },
                )
            }
        }
    }
}

private val levelColors = listOf(
    Color(0xFFDDE7DC),
    Color(0xFFDCE6F0),
    Color(0xFFE6DFF0),
    Color(0xFFF3E7CF),
    Color(0xFFF0DCD2),
)
private val levelTextColor = Color(0xFF2B2B2E)

@Composable
private fun FiveLevelInput(
    labels: List<String>,
    selected: Int?,
    onSelect: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        for (level in 1..5) {
            val isSelected = selected == level
            Surface(
                onClick = { onSelect(level) },
                modifier = Modifier.weight(1f).heightIn(min = 64.dp),
                shape = RoundedCornerShape(14.dp),
                color = levelColors[level - 1],
                contentColor = levelTextColor,
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                } else {
                    null
                },
            ) {
                Column(
                    modifier = Modifier.padding(4.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(level.toString(), style = MaterialTheme.typography.titleMedium)
                    Text(
                        labels.getOrElse(level - 1) { level.toString() },
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun YesNoInput(selected: Int?, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        FilterChip(
            selected = selected == 1,
            onClick = { onSelect(1) },
            label = { Text(stringResourceCompat(R.string.yes)) },
        )
        FilterChip(
            selected = selected == 0,
            onClick = { onSelect(0) },
            label = { Text(stringResourceCompat(R.string.no)) },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DurationInput(options: List<String>, selected: Int?, onSelect: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = selected == index,
                onClick = { onSelect(index) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun NumberInput(value: Int?, unit: String?, onChange: (Int?) -> Unit) {
    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = { raw ->
            val digits = raw.filter { it.isDigit() }
            onChange(if (digits.isEmpty()) null else digits.toIntOrNull())
        },
        label = { Text(unit ?: stringResourceCompat(R.string.type_number)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BodyMapSection(
    marks: Map<BodyRegion, BodyState>,
    onCycle: (BodyRegion) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResourceCompat(R.string.symptom_body_map))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (region in bodyRegionOrder) {
                val state = marks[region]
                val stateLabel = when (state) {
                    BodyState.SORE -> stringResourceCompat(R.string.body_state_sore)
                    BodyState.SWOLLEN -> stringResourceCompat(R.string.body_state_swollen)
                    null -> stringResourceCompat(R.string.body_state_none)
                }
                FilterChip(
                    selected = state != null,
                    onClick = { onCycle(region) },
                    label = {
                        Text(stringResourceCompat(region.labelRes()) + ": " + stateLabel)
                    },
                )
            }
        }
    }
}

@Composable
private fun NotesSection(
    notes: List<club.hiraeth.flareenough.data.db.entity.DayNoteEntity>,
    onAdd: (String) -> Unit,
    onDelete: (club.hiraeth.flareenough.data.db.entity.DayNoteEntity) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResourceCompat(R.string.symptom_notes))
        notes.forEach { note ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(note.text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = { onDelete(note) }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResourceCompat(R.string.action_delete))
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResourceCompat(R.string.symptom_add_note)) },
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { onAdd(text); text = "" },
                enabled = text.isNotBlank(),
                modifier = Modifier.heightIn(min = 56.dp),
            ) { Text(stringResourceCompat(R.string.action_add)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(
    tags: List<club.hiraeth.flareenough.data.db.entity.TagEntity>,
    onAdd: (String) -> Unit,
    onRemove: (Long) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResourceCompat(R.string.symptom_tags))
        if (tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = { },
                        label = { Text(tag.name) },
                        trailingIcon = {
                            IconButton(onClick = { onRemove(tag.id) }) {
                                Icon(Icons.Filled.Close, contentDescription = stringResourceCompat(R.string.action_delete))
                            }
                        },
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResourceCompat(R.string.symptom_add_tag)) },
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { onAdd(text); text = "" },
                enabled = text.isNotBlank(),
                modifier = Modifier.heightIn(min = 56.dp),
            ) { Text(stringResourceCompat(R.string.action_add)) }
        }
    }
}
