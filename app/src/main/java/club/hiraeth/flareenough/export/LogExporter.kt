package club.hiraeth.flareenough.export

import android.content.Context
import android.net.Uri
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.dao.DayDao
import club.hiraeth.flareenough.data.db.dao.DoseDao
import club.hiraeth.flareenough.data.db.dao.MedicationDao
import club.hiraeth.flareenough.data.db.dao.SymptomDao
import club.hiraeth.flareenough.data.db.entity.SymptomEntryEntity
import club.hiraeth.flareenough.ui.symptoms.labelRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Writes plain CSV files of the log, for the person to keep or show to a clinician.
 * These are records only, with no interpretation, prediction, or correlation. Every
 * heading and word comes from string resources, so the export can be translated.
 */
class LogExporter(
    private val context: Context,
    private val medicationDao: MedicationDao,
    private val doseDao: DoseDao,
    private val symptomDao: SymptomDao,
    private val dayDao: DayDao,
) {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val timeOfDay = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    /** One row per logged dose: date, planned time, medication, and outcome. */
    suspend fun exportMedicationLog(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val meds = medicationDao.getAllMedications().associateBy { it.id }
            val doses = doseDao.getAllDoses()

            val out = StringBuilder()
            out.appendRow(
                listOf(
                    context.getString(R.string.csv_date),
                    context.getString(R.string.csv_time),
                    context.getString(R.string.csv_medication),
                    context.getString(R.string.csv_strength),
                    context.getString(R.string.csv_status),
                    context.getString(R.string.csv_taken_at),
                ),
            )
            for (dose in doses) {
                val med = meds[dose.medicationId]
                val stamp = dose.actualTimeMillis ?: dose.scheduledTimeMillis ?: dose.createdAtMillis
                val date = Instant.ofEpochMilli(stamp).atZone(zone).toLocalDate()
                out.appendRow(
                    listOf(
                        date.toString(),
                        dose.scheduledTimeMillis?.let {
                            Instant.ofEpochMilli(it).atZone(zone).format(timeOfDay)
                        } ?: "",
                        med?.name ?: "",
                        med?.strength ?: "",
                        doseStatusLabel(context, dose.status),
                        dose.actualTimeMillis?.let {
                            Instant.ofEpochMilli(it).atZone(zone).format(dateTime)
                        } ?: "",
                    ),
                )
            }
            writeText(uri, out.toString())
        }
    }

    /** One row per day that has any data: flare, period, each symptom, joints, notes, tags. */
    suspend fun exportDailyLog(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val trackers = symptomDao.getAllTrackers()
            val entriesByDay: Map<Long, List<SymptomEntryEntity>> =
                symptomDao.getAllEntries().groupBy { it.epochDay }
            val flareDays = dayDao.getAllFlareDays().map { it.epochDay }.toSet()
            val periodByDay = dayDao.getAllPeriodDays().associate { it.epochDay to it.flow }
            val bodyByDay = dayDao.getAllBodyMap().groupBy { it.epochDay }
            val notesByDay = dayDao.getAllNotes().groupBy { it.epochDay }
            val tagsById = dayDao.getAllTags().associate { it.id to it.name }
            val tagIdsByDay = dayDao.getAllDayTags().groupBy({ it.epochDay }, { it.tagId })

            val days = buildSet {
                addAll(entriesByDay.keys)
                addAll(flareDays)
                addAll(periodByDay.keys)
                addAll(bodyByDay.keys)
                addAll(notesByDay.keys)
                addAll(tagIdsByDay.keys)
            }.sorted()

            val header = mutableListOf(
                context.getString(R.string.csv_date),
                context.getString(R.string.csv_flare),
                context.getString(R.string.period_title),
            )
            trackers.forEach { header.add(it.name) }
            header.add(context.getString(R.string.csv_joints))
            header.add(context.getString(R.string.csv_notes))
            header.add(context.getString(R.string.csv_tags))

            val out = StringBuilder()
            out.appendRow(header)

            for (day in days) {
                val date = LocalDate.ofEpochDay(day)
                val row = mutableListOf<String>()
                row.add(date.toString())
                row.add(if (day in flareDays) context.getString(R.string.yes) else "")
                row.add(periodByDay[day]?.let { periodFlowLabel(context, it) } ?: "")

                val entriesForDay = entriesByDay[day].orEmpty().associateBy { it.trackerId }
                for (tracker in trackers) {
                    row.add(
                        entriesForDay[tracker.id]?.let { trackerValueLabel(context, tracker, it) } ?: "",
                    )
                }

                row.add(
                    bodyByDay[day].orEmpty().joinToString("; ") { entry ->
                        val region = context.getString(entry.region.labelRes())
                        "$region (${bodyStateLabel(context, entry.state)})"
                    },
                )
                row.add(notesByDay[day].orEmpty().joinToString(" | ") { it.text })
                row.add(
                    tagIdsByDay[day].orEmpty()
                        .mapNotNull { tagsById[it] }
                        .joinToString(", "),
                )
                out.appendRow(row)
            }
            writeText(uri, out.toString())
        }
    }

    private fun writeText(uri: Uri, text: String) {
        val out = context.contentResolver.openOutputStream(uri)
            ?: error("Could not open the chosen file to write.")
        out.use { it.write(text.toByteArray(Charsets.UTF_8)) }
    }
}

/** Append one CSV row, escaping any field that needs it, with a trailing newline. */
private fun StringBuilder.appendRow(fields: List<String>) {
    append(fields.joinToString(",") { escapeCsv(it) })
    append("\r\n")
}

private fun escapeCsv(field: String): String =
    if (field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
        "\"" + field.replace("\"", "\"\"") + "\""
    } else {
        field
    }
