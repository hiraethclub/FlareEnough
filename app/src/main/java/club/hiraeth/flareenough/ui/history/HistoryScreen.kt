package club.hiraeth.flareenough.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.BodyState
import club.hiraeth.flareenough.data.db.entity.DoseStatus
import club.hiraeth.flareenough.data.db.entity.PeriodFlow
import club.hiraeth.flareenough.data.db.entity.SessionType
import club.hiraeth.flareenough.data.db.entity.SymptomTrackerEntity
import club.hiraeth.flareenough.data.db.entity.TrackerType
import club.hiraeth.flareenough.ui.symptoms.labelRes
import club.hiraeth.flareenough.ui.support.formatEpochDay
import club.hiraeth.flareenough.ui.support.formatInstantTime
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val severityShadesLight = listOf(
    Color(0xFFE7EFE6),
    Color(0xFFE7EEF5),
    Color(0xFFEDE8F5),
    Color(0xFFF6EEDC),
    Color(0xFFF3E0D7),
)
private val severityShadesDark = listOf(
    Color(0xFF2C3730),
    Color(0xFF29333D),
    Color(0xFF322D40),
    Color(0xFF403826),
    Color(0xFF402E27),
)

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MonthHeader(
            yearMonth = state.yearMonth,
            onPrev = { viewModel.showMonth(state.yearMonth.minusMonths(1)) },
            onNext = { viewModel.showMonth(state.yearMonth.plusMonths(1)) },
        )
        CalendarGrid(
            yearMonth = state.yearMonth,
            selectedDay = state.selectedDay,
            summaries = state.summaries,
            onSelect = { viewModel.selectDay(it) },
        )

        Text(
            text = formatEpochDay(context, state.selectedDay.toEpochDay()),
            style = MaterialTheme.typography.titleMedium,
        )
        if (state.timeline.isEmpty()) {
            Text(
                stringResource(R.string.history_nothing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            state.timeline.forEach { item -> TimelineRow(item) }
        }
    }
}

@Composable
private fun MonthHeader(yearMonth: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    val formatter = remember0 { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.history_prev_month))
        }
        Text(
            text = yearMonth.format(formatter),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.history_next_month))
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDay: LocalDate,
    summaries: Map<Long, DaySummary>,
    onSelect: (LocalDate) -> Unit,
) {
    val weekdays = stringArrayResource(R.array.day_abbreviations)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        val firstDay = yearMonth.atDay(1)
        val leadingBlanks = firstDay.dayOfWeek.value - 1 // Monday is 1
        val daysInMonth = yearMonth.lengthOfMonth()
        val cells = buildList {
            repeat(leadingBlanks) { add(null) }
            for (d in 1..daysInMonth) add(yearMonth.atDay(d))
            while (size % 7 != 0) add(null)
        }
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (date != null) {
                            DayCell(
                                date = date,
                                summary = summaries[date.toEpochDay()],
                                selected = date == selectedDay,
                                onClick = { onSelect(date) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    summary: DaySummary?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val shades = if (dark) severityShadesDark else severityShadesLight
    val shade = summary?.maxSymptomLevel?.let { shades.getOrNull(it - 1) }
        ?: MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(shade)
            .then(
                if (selected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                } else if (summary?.flare == true) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(10.dp))
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (summary?.hasActivity == true) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

@Composable
private fun TimelineRow(item: TimelineItem) {
    val context = LocalContext.current
    val (icon, title, subtitle) = timelineContent(item)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item.timeMillis?.let { Text(formatInstantTime(context, it), style = MaterialTheme.typography.labelMedium) }
    }
}

@Composable
private fun timelineContent(item: TimelineItem): Triple<androidx.compose.ui.graphics.vector.ImageVector, String, String?> =
    when (item) {
        is TimelineItem.Dose -> Triple(
            when (item.status) {
                DoseStatus.TAKEN -> Icons.Filled.CheckCircle
                else -> Icons.Filled.RemoveCircleOutline
            },
            item.medicationName,
            stringResource(
                when (item.status) {
                    DoseStatus.TAKEN -> R.string.status_taken
                    DoseStatus.SKIPPED -> R.string.status_skipped
                    DoseStatus.MISSED -> R.string.status_skipped
                },
            ),
        )
        is TimelineItem.Symptom -> Triple(
            Icons.Filled.SentimentSatisfied,
            item.tracker.name,
            symptomValueText(item.tracker, item.valueInt, item.valueText),
        )
        is TimelineItem.Flare -> Triple(
            Icons.Filled.LocalFireDepartment,
            stringResource(R.string.symptom_flare),
            null,
        )
        is TimelineItem.Period -> Triple(
            Icons.Filled.WaterDrop,
            stringResource(R.string.period_title),
            stringResource(
                when (item.flow) {
                    PeriodFlow.SPOTTING -> R.string.period_flow_spotting
                    PeriodFlow.LIGHT -> R.string.period_flow_light
                    PeriodFlow.MEDIUM -> R.string.period_flow_medium
                    PeriodFlow.HEAVY -> R.string.period_flow_heavy
                },
            ),
        )
        is TimelineItem.Body -> Triple(
            Icons.Filled.Healing,
            stringResource(item.region.labelRes()),
            stringResource(
                if (item.state == BodyState.SORE) R.string.body_state_sore else R.string.body_state_swollen,
            ),
        )
        is TimelineItem.Note -> Triple(Icons.Filled.EditNote, item.text, null)
        is TimelineItem.Meditation -> Triple(
            Icons.Filled.SelfImprovement,
            stringResource(R.string.timeline_stillness),
            sessionSubtitle(item.type, item.durationSeconds),
        )
    }

@Composable
private fun symptomValueText(tracker: SymptomTrackerEntity, valueInt: Int?, valueText: String?): String {
    return when (tracker.type) {
        TrackerType.FIVE_LEVEL -> {
            val label = valueInt?.let { tracker.levelLabels?.getOrNull(it - 1) }
            listOfNotNull(valueInt?.toString(), label).joinToString(": ")
        }
        TrackerType.YES_NO -> stringResource(if (valueInt == 1) R.string.yes else R.string.no)
        TrackerType.NUMBER -> listOfNotNull(valueInt?.toString(), tracker.unit).joinToString(" ")
        TrackerType.DURATION -> valueInt?.let { tracker.durationOptions?.getOrNull(it) } ?: (valueText ?: "")
    }
}

@Composable
private fun sessionSubtitle(type: SessionType, durationSeconds: Int): String {
    val typeLabel = stringResource(
        when (type) {
            SessionType.TIMER -> R.string.session_timer
            SessionType.BREATHING -> R.string.session_breathing
            SessionType.BELL -> R.string.session_bell
        },
    )
    val minutes = (durationSeconds / 60).coerceAtLeast(0)
    return typeLabel + ", " + stringResource(R.string.duration_minutes, minutes)
}

// A small remember alias so the formatter is created once.
@Composable
private fun <T> remember0(calculation: () -> T): T = androidx.compose.runtime.remember(calculation)
