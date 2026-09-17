package club.hiraeth.flareenough.ui.medication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.MedicationForm
import club.hiraeth.flareenough.data.db.entity.ScheduleType
import club.hiraeth.flareenough.ui.common.FlareDatePickerDialog
import club.hiraeth.flareenough.ui.common.TimePickerDialog
import club.hiraeth.flareenough.ui.support.formatEpochDay
import club.hiraeth.flareenough.ui.support.formatMinutesOfDay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MedicationEditScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: MedicationEditViewModel,
) {
    val form = viewModel.form

    // Which time is being edited: null none, -1 add new, else the index to change.
    var timeEditIndex by remember { mutableStateOf<Int?>(null) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (form.isNew) R.string.medication_add_title else R.string.medication_edit_title,
                        ),
                    )
                },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Basics.
            OutlinedTextField(
                value = form.name,
                onValueChange = { v -> viewModel.update { it.copy(name = v) } },
                label = { Text(stringResource(R.string.field_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.strength,
                onValueChange = { v -> viewModel.update { it.copy(strength = v) } },
                label = { Text(optionalLabel(R.string.field_strength)) },
                placeholder = { Text(stringResource(R.string.field_strength_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            SectionHeader(stringResource(R.string.field_form))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MedicationForm.entries.forEach { f ->
                    FilterChip(
                        selected = form.form == f,
                        onClick = { viewModel.update { it.copy(form = f) } },
                        label = { Text(stringResource(f.labelRes())) },
                    )
                }
            }

            OutlinedTextField(
                value = form.note,
                onValueChange = { v -> viewModel.update { it.copy(note = v) } },
                label = { Text(optionalLabel(R.string.field_note)) },
                placeholder = { Text(stringResource(R.string.field_note_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )

            // Schedule.
            SectionHeader(stringResource(R.string.section_schedule))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScheduleType.entries.forEach { type ->
                    FilterChip(
                        selected = form.scheduleType == type,
                        onClick = { viewModel.update { it.copy(scheduleType = type) } },
                        label = { Text(stringResource(type.labelRes())) },
                    )
                }
            }

            ScheduleDetails(
                form = form,
                onEditTime = { index -> timeEditIndex = index },
                onAddTime = { timeEditIndex = -1 },
                onRemoveTime = { index ->
                    viewModel.update { it.copy(times = it.times.filterIndexed { i, _ -> i != index }) }
                },
                onToggleDay = { day ->
                    viewModel.update { state ->
                        if (state.scheduleType == ScheduleType.WEEKLY) {
                            state.copy(daysOfWeek = setOf(day))
                        } else {
                            val next = state.daysOfWeek.toMutableSet()
                            if (!next.add(day)) next.remove(day)
                            state.copy(daysOfWeek = next)
                        }
                    }
                },
                onIntervalChange = { n -> viewModel.update { it.copy(intervalDays = n) } },
                onCycleOnChange = { n -> viewModel.update { it.copy(cycleDaysOn = n) } },
                onCycleOffChange = { n -> viewModel.update { it.copy(cycleDaysOff = n) } },
                onGapChange = { n -> viewModel.update { it.copy(asNeededMinGapHours = n) } },
                onMaxChange = { n -> viewModel.update { it.copy(asNeededDailyMax = n) } },
            )

            // Stock.
            SectionHeader(stringResource(R.string.section_stock))
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.stock_toggle), modifier = Modifier.weight(1f))
                Switch(
                    checked = form.stockEnabled,
                    onCheckedChange = { on -> viewModel.update { it.copy(stockEnabled = on) } },
                )
            }
            if (form.stockEnabled) {
                NumberField(
                    label = stringResource(R.string.stock_count_label),
                    value = form.stockCount,
                    onChange = { n -> viewModel.update { it.copy(stockCount = n ?: 0) } },
                )
                NumberField(
                    label = optionalLabel(R.string.refill_label),
                    value = form.refillThreshold,
                    allowEmpty = true,
                    onChange = { n -> viewModel.update { it.copy(refillThreshold = n) } },
                )
            }

            // Dates.
            SectionHeader(stringResource(R.string.section_dates))
            DateRow(
                label = optionalLabel(R.string.start_date_label),
                epochDay = form.startEpochDay,
                onPick = { showStartPicker = true },
                onClear = { viewModel.update { it.copy(startEpochDay = null) } },
            )
            DateRow(
                label = optionalLabel(R.string.end_date_label),
                epochDay = form.endEpochDay,
                onPick = { showEndPicker = true },
                onClear = { viewModel.update { it.copy(endEpochDay = null) } },
            )

            // Actions.
            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = form.canSave,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Text(stringResource(R.string.action_save))
            }
            if (!form.isNew) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            }
        }
    }

    // Dialogs.
    timeEditIndex?.let { index ->
        val initial = if (index >= 0) form.times[index] else 8 * 60
        TimePickerDialog(
            initialMinutes = initial,
            onConfirm = { minutes ->
                viewModel.update { state ->
                    val newTimes = if (index >= 0) {
                        state.times.toMutableList().also { it[index] = minutes }
                    } else {
                        (state.times + minutes)
                    }.distinct().sorted()
                    state.copy(times = newTimes)
                }
                timeEditIndex = null
            },
            onDismiss = { timeEditIndex = null },
        )
    }
    if (showStartPicker) {
        FlareDatePickerDialog(
            initialEpochDay = form.startEpochDay,
            onConfirm = { day -> viewModel.update { it.copy(startEpochDay = day) } ; showStartPicker = false },
            onDismiss = { showStartPicker = false },
        )
    }
    if (showEndPicker) {
        FlareDatePickerDialog(
            initialEpochDay = form.endEpochDay,
            onConfirm = { day -> viewModel.update { it.copy(endEpochDay = day) } ; showEndPicker = false },
            onDismiss = { showEndPicker = false },
        )
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete(onDeleted)
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleDetails(
    form: MedicationFormState,
    onEditTime: (Int) -> Unit,
    onAddTime: () -> Unit,
    onRemoveTime: (Int) -> Unit,
    onToggleDay: (Int) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onCycleOnChange: (Int) -> Unit,
    onCycleOffChange: (Int) -> Unit,
    onGapChange: (Int?) -> Unit,
    onMaxChange: (Int?) -> Unit,
) {
    val context = LocalContext.current
    val dayAbbrev = stringArrayResource(R.array.day_abbreviations)

    // Days of week for the day based schedules.
    if (form.scheduleType == ScheduleType.DAYS_OF_WEEK || form.scheduleType == ScheduleType.WEEKLY) {
        SectionHeader(stringResource(R.string.days_of_week_label))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (day in 0..6) {
                FilterChip(
                    selected = form.daysOfWeek.contains(day),
                    onClick = { onToggleDay(day) },
                    label = { Text(dayAbbrev[day]) },
                )
            }
        }
    }

    if (form.scheduleType == ScheduleType.EVERY_N_DAYS) {
        NumberField(
            label = stringResource(R.string.interval_label),
            value = form.intervalDays,
            onChange = { n -> onIntervalChange((n ?: 1).coerceAtLeast(1)) },
        )
    }

    if (form.scheduleType == ScheduleType.CYCLE) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(
                label = stringResource(R.string.cycle_on_label),
                value = form.cycleDaysOn,
                onChange = { n -> onCycleOnChange((n ?: 1).coerceAtLeast(1)) },
                modifier = Modifier.weight(1f),
            )
            NumberField(
                label = stringResource(R.string.cycle_off_label),
                value = form.cycleDaysOff,
                onChange = { n -> onCycleOffChange((n ?: 0).coerceAtLeast(0)) },
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (form.scheduleType == ScheduleType.AS_NEEDED) {
        NumberField(
            label = optionalLabel(R.string.as_needed_gap_label),
            value = form.asNeededMinGapHours,
            allowEmpty = true,
            onChange = onGapChange,
        )
        NumberField(
            label = optionalLabel(R.string.as_needed_max_label),
            value = form.asNeededDailyMax,
            allowEmpty = true,
            onChange = onMaxChange,
        )
        Text(
            text = stringResource(R.string.as_needed_explainer),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        // Times, for every schedule except as needed.
        SectionHeader(stringResource(R.string.times_label))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            form.times.forEachIndexed { index, minutes ->
                InputChip(
                    selected = false,
                    onClick = { onEditTime(index) },
                    label = { Text(formatMinutesOfDay(context, minutes)) },
                    trailingIcon = {
                        IconButton(onClick = { onRemoveTime(index) }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.remove_time),
                            )
                        }
                    },
                )
            }
            AssistChip(
                onClick = onAddTime,
                label = { Text(stringResource(R.string.add_time)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                },
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun optionalLabel(labelRes: Int): String =
    stringResource(labelRes) + " (" + stringResource(R.string.optional_suffix) + ")"

@Composable
private fun NumberField(
    label: String,
    value: Int?,
    onChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    allowEmpty: Boolean = false,
) {
    val text = value?.toString() ?: ""
    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            val digits = raw.filter { it.isDigit() }
            when {
                digits.isEmpty() -> onChange(if (allowEmpty) null else 0)
                else -> onChange(digits.toIntOrNull())
            }
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun DateRow(
    label: String,
    epochDay: Long?,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = epochDay?.let { formatEpochDay(context, it) }
                    ?: stringResource(R.string.date_not_set),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        TextButton(onClick = onPick) { Text(stringResource(R.string.pick_date)) }
        if (epochDay != null) {
            TextButton(onClick = onClear) { Text(stringResource(R.string.action_clear)) }
        }
    }
}
