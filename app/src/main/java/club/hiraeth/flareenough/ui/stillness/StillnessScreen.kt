package club.hiraeth.flareenough.ui.stillness

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import club.hiraeth.flareenough.R

private enum class StillnessMode { TIMER, BREATHING, PROMPTS, READINGS }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StillnessScreen(
    viewModel: StillnessViewModel,
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(StillnessMode.TIMER) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(mode == StillnessMode.TIMER, { mode = StillnessMode.TIMER }, { Text(stringResource(R.string.stillness_timer_tab)) })
            FilterChip(mode == StillnessMode.BREATHING, { mode = StillnessMode.BREATHING }, { Text(stringResource(R.string.stillness_breathing_tab)) })
            FilterChip(mode == StillnessMode.PROMPTS, { mode = StillnessMode.PROMPTS }, { Text(stringResource(R.string.stillness_prompts_tab)) })
            FilterChip(mode == StillnessMode.READINGS, { mode = StillnessMode.READINGS }, { Text(stringResource(R.string.stillness_readings_tab)) })
        }

        when (mode) {
            StillnessMode.TIMER -> TimerSection(viewModel)
            StillnessMode.BREATHING -> BreathingSection(viewModel)
            StillnessMode.PROMPTS -> PromptsSection(viewModel)
            StillnessMode.READINGS -> ReadingsSection(viewModel)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimerSection(vm: StillnessViewModel) {
    if (vm.timerRunning) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(R.string.stillness_remaining, vm.remainingSeconds / 60, vm.remainingSeconds % 60),
                    style = MaterialTheme.typography.displayMedium,
                )
                Button(
                    onClick = { vm.stopTimer() },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                ) { Text(stringResource(R.string.stillness_stop)) }
            }
        }
        return
    }

    Text(stringResource(R.string.stillness_length), style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(3, 5, 10, 15, 20, 30).forEach { minutes ->
            FilterChip(
                selected = vm.durationMinutes == minutes,
                onClick = { vm.setDuration(minutes) },
                label = { Text(stringResource(R.string.stillness_minutes, minutes)) },
            )
        }
    }

    Text(stringResource(R.string.stillness_interval_label), style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(0, 5, 10).forEach { minutes ->
            FilterChip(
                selected = vm.intervalMinutes == minutes,
                onClick = { vm.setInterval(minutes) },
                label = {
                    Text(
                        if (minutes == 0) stringResource(R.string.stillness_off)
                        else stringResource(R.string.stillness_minutes, minutes),
                    )
                },
            )
        }
    }

    BellToggle(stringResource(R.string.stillness_start_bell), vm.startBell) { vm.toggleStartBell() }
    BellToggle(stringResource(R.string.stillness_interval_bell), vm.intervalBell) { vm.toggleIntervalBell() }
    BellToggle(stringResource(R.string.stillness_end_bell), vm.endBell) { vm.toggleEndBell() }

    Button(
        onClick = { vm.startTimer() },
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
    ) { Text(stringResource(R.string.stillness_start)) }
}

@Composable
private fun BellToggle(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BreathingSection(vm: StillnessViewModel) {
    val scale by animateFloatAsState(
        targetValue = vm.breathTargetScale,
        animationSpec = tween(durationMillis = vm.breathPhaseMillis),
        label = "breath",
    )
    val phaseLabel = when (vm.breathPhase) {
        BreathPhase.IN -> stringResource(R.string.breathing_in)
        BreathPhase.HOLD -> stringResource(R.string.breathing_hold)
        BreathPhase.OUT -> stringResource(R.string.breathing_out)
    }

    if (!vm.breathingRunning) {
        Text(stringResource(R.string.breathing_preset), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(vm.breathPatternKey == "46", { vm.setBreathPattern("46") }, { Text(stringResource(R.string.breathing_46)) })
            FilterChip(vm.breathPatternKey == "478", { vm.setBreathPattern("478") }, { Text(stringResource(R.string.breathing_478)) })
            FilterChip(vm.breathPatternKey == "box", { vm.setBreathPattern("box") }, { Text(stringResource(R.string.breathing_box)) })
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (vm.breathingRunning) {
                Text(phaseLabel, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }

    Button(
        onClick = { if (vm.breathingRunning) vm.stopBreathing() else vm.startBreathing() },
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
    ) {
        Text(stringResource(if (vm.breathingRunning) R.string.stillness_stop else R.string.stillness_start))
    }
}

@Composable
private fun PromptsSection(vm: StillnessViewModel) {
    val builtIn = stringArrayResource(R.array.stillness_prompts).toList()
    val custom by vm.customPrompts.collectAsStateWithLifecycle()
    val all = remember(custom) { builtIn + custom.map { it.text } }

    var index by remember { mutableStateOf(0) }
    var newPrompt by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    all.getOrElse(index.coerceIn(0, (all.size - 1).coerceAtLeast(0))) { "" },
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = { if (all.isNotEmpty()) index = (index + 1) % all.size },
                    modifier = Modifier.heightIn(min = 56.dp),
                ) { Text(stringResource(R.string.prompt_next)) }
            }
        }

        Text(stringResource(R.string.prompts_your_title), style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newPrompt,
                onValueChange = { newPrompt = it },
                label = { Text(stringResource(R.string.prompts_add)) },
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { vm.addPrompt(newPrompt); newPrompt = "" },
                enabled = newPrompt.isNotBlank(),
                modifier = Modifier.heightIn(min = 56.dp),
            ) { Text(stringResource(R.string.action_add)) }
        }
        custom.forEach { prompt ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(prompt.text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = { vm.deletePrompt(prompt) }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_delete))
                }
            }
        }
    }
}

@Composable
private fun ReadingsSection(vm: StillnessViewModel) {
    val readings by vm.readings.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf<Long?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (readings.isEmpty() && !showAdd) {
            Text(
                stringResource(R.string.readings_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        readings.forEach { reading ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            reading.title,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { expandedId = if (expandedId == reading.id) null else reading.id },
                            style = MaterialTheme.typography.titleMedium,
                        )
                        IconButton(onClick = { vm.deleteReading(reading) }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                    if (expandedId == reading.id) {
                        Text(reading.body, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        if (showAdd) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.reading_title_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text(stringResource(R.string.reading_body_label)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
            )
            Button(
                onClick = {
                    vm.addReading(title, body)
                    title = ""; body = ""; showAdd = false
                },
                enabled = title.isNotBlank() && body.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) { Text(stringResource(R.string.reading_save)) }
        } else {
            OutlinedButton(
                onClick = { showAdd = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) { Text(stringResource(R.string.reading_add)) }
        }
    }
}
