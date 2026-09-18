package club.hiraeth.flareenough.ui.symptoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.SymptomTrackerEntity
import club.hiraeth.flareenough.data.db.entity.TrackerType

private data class Starter(val labelRes: Int)

private val starters = listOf(
    Starter(R.string.starter_pain),
    Starter(R.string.starter_stiffness),
    Starter(R.string.starter_fatigue),
    Starter(R.string.starter_mood),
    Starter(R.string.starter_anxiety),
    Starter(R.string.starter_sleep),
)

/** The optional starter set offered on first run. Every one is editable later. */
@Composable
fun StarterSetCard(
    defaultLabels: List<String>,
    onAdd: (List<SymptomTrackerEntity>) -> Unit,
) {
    val names = starters.map { stringResource(it.labelRes) }
    val checked = remember { mutableStateOf(names.map { true }) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.symptom_starter_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.symptom_starter_sub),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            names.forEachIndexed { index, name ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Checkbox(
                        checked = checked.value[index],
                        onCheckedChange = { on ->
                            checked.value = checked.value.toMutableList().also { it[index] = on }
                        },
                    )
                    Text(name, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    val selected = names.filterIndexed { index, _ -> checked.value[index] }
                    val trackers = selected.mapIndexed { index, name ->
                        SymptomTrackerEntity(
                            name = name,
                            type = TrackerType.FIVE_LEVEL,
                            levelLabels = defaultLabels,
                            sortOrder = index,
                            createdAtMillis = now,
                        )
                    }
                    if (trackers.isNotEmpty()) onAdd(trackers)
                },
                enabled = checked.value.any { it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Text(stringResource(R.string.symptom_starter_add))
            }
        }
    }
}

/** Add a custom tracker: a name and a type. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTrackerDialog(
    defaultLabels: List<String>,
    onDismiss: () -> Unit,
    onAdd: (SymptomTrackerEntity) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TrackerType.FIVE_LEVEL) }
    val durationOptions = stringArrayResource(R.array.default_duration_options).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.symptom_add_tracker)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.new_tracker_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.new_tracker_type), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrackerType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(stringResource(t.labelRes())) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val now = System.currentTimeMillis()
                    onAdd(
                        SymptomTrackerEntity(
                            name = name.trim(),
                            type = type,
                            levelLabels = if (type == TrackerType.FIVE_LEVEL) defaultLabels else null,
                            durationOptions = if (type == TrackerType.DURATION) durationOptions else null,
                            createdAtMillis = now,
                        ),
                    )
                },
            ) { Text(stringResource(R.string.action_add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
