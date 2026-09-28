package club.hiraeth.flareenough.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.dao.DayDao
import club.hiraeth.flareenough.data.db.dao.DoseDao
import club.hiraeth.flareenough.data.db.dao.MedicationDao
import club.hiraeth.flareenough.data.db.dao.SymptomDao
import club.hiraeth.flareenough.data.db.entity.SymptomEntryEntity
import club.hiraeth.flareenough.ui.support.formatEpochDay
import club.hiraeth.flareenough.ui.symptoms.labelRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Builds a plain PDF summary of the log, for the person to keep or show to a clinician.
 * It is a record only: it lists what was logged, day by day, with no charts, no
 * interpretation, no prediction, and no correlation. All words come from resources.
 */
class PdfReporter(
    private val context: Context,
    private val medicationDao: MedicationDao,
    private val doseDao: DoseDao,
    private val symptomDao: SymptomDao,
    private val dayDao: DayDao,
) {

    suspend fun writeReport(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val meds = medicationDao.getAllMedications()
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
            }.sortedDescending()

            val doc = PdfDocument()
            val page = PagedText(doc)

            page.title(context.getString(R.string.pdf_report_title, context.getString(R.string.app_name)))
            val today = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
            page.body(context.getString(R.string.pdf_generated, today))
            page.body(context.getString(R.string.pdf_not_medical))
            page.gap()

            page.heading(context.getString(R.string.pdf_medications))
            if (meds.isEmpty()) {
                page.body(context.getString(R.string.pdf_none))
            } else {
                for (med in meds) {
                    val strength = med.strength?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
                    page.body("- ${med.name}$strength")
                }
            }
            page.gap()

            page.heading(context.getString(R.string.pdf_daily_log))
            if (days.isEmpty()) {
                page.body(context.getString(R.string.pdf_none))
            } else {
                for (day in days) {
                    page.subheading(formatEpochDay(context, day))

                    if (day in flareDays) {
                        page.body("${context.getString(R.string.csv_flare)}: ${context.getString(R.string.yes)}")
                    }
                    periodByDay[day]?.let {
                        page.body("${context.getString(R.string.period_title)}: ${periodFlowLabel(context, it)}")
                    }

                    val entriesForDay = entriesByDay[day].orEmpty().associateBy { it.trackerId }
                    for (tracker in trackers) {
                        val entry = entriesForDay[tracker.id] ?: continue
                        val value = trackerValueLabel(context, tracker, entry)
                        if (value.isNotBlank()) page.body("${tracker.name}: $value")
                    }

                    val joints = bodyByDay[day].orEmpty().joinToString("; ") { entry ->
                        "${context.getString(entry.region.labelRes())} (${bodyStateLabel(context, entry.state)})"
                    }
                    if (joints.isNotBlank()) {
                        page.body("${context.getString(R.string.csv_joints)}: $joints")
                    }

                    notesByDay[day].orEmpty().forEach { note ->
                        page.body("${context.getString(R.string.csv_notes)}: ${note.text}")
                    }

                    val tags = tagIdsByDay[day].orEmpty().mapNotNull { tagsById[it] }.joinToString(", ")
                    if (tags.isNotBlank()) {
                        page.body("${context.getString(R.string.csv_tags)}: $tags")
                    }

                    page.gap()
                }
            }

            page.finish()

            val out = context.contentResolver.openOutputStream(uri)
                ?: error("Could not open the chosen file to write the report.")
            out.use { doc.writeTo(it) }
            doc.close()
        }
    }

    /**
     * A tiny paged text layout for the report. It writes lines down an A4 page and
     * starts a new page when it runs out of room, wrapping long lines to the width.
     */
    private class PagedText(private val doc: PdfDocument) {
        private val titlePaint = Paint().apply { textSize = 22f; isFakeBoldText = true; isAntiAlias = true }
        private val headingPaint = Paint().apply { textSize = 15f; isFakeBoldText = true; isAntiAlias = true }
        private val subheadingPaint = Paint().apply { textSize = 13f; isFakeBoldText = true; isAntiAlias = true }
        private val bodyPaint = Paint().apply { textSize = 11f; isAntiAlias = true }

        private var pageNumber = 1
        private var page: PdfDocument.Page = newPage()
        private var y = MARGIN + 24f

        private fun newPage(): PdfDocument.Page {
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            return doc.startPage(info)
        }

        private fun ensureRoom(lineHeight: Float) {
            if (y + lineHeight > PAGE_HEIGHT - MARGIN) {
                doc.finishPage(page)
                pageNumber += 1
                page = newPage()
                y = MARGIN + 24f
            }
        }

        private fun draw(text: String, paint: Paint) {
            val lineHeight = paint.textSize + 6f
            for (line in wrap(text, paint)) {
                ensureRoom(lineHeight)
                page.canvas.drawText(line, MARGIN.toFloat(), y, paint)
                y += lineHeight
            }
        }

        fun title(text: String) = draw(text, titlePaint)
        fun heading(text: String) = draw(text, headingPaint)
        fun subheading(text: String) = draw(text, subheadingPaint)
        fun body(text: String) = draw(text, bodyPaint)
        fun gap() {
            y += 8f
        }

        fun finish() {
            doc.finishPage(page)
        }

        /** Break a line into pieces that each fit the page width. */
        private fun wrap(text: String, paint: Paint): List<String> {
            val maxWidth = PAGE_WIDTH - 2 * MARGIN.toFloat()
            if (text.isEmpty()) return listOf("")
            if (paint.measureText(text) <= maxWidth) return listOf(text)
            val words = text.split(' ')
            val lines = mutableListOf<String>()
            var current = StringBuilder()
            for (word in words) {
                val candidate = if (current.isEmpty()) word else "$current $word"
                if (paint.measureText(candidate) <= maxWidth) {
                    current = StringBuilder(candidate)
                } else {
                    if (current.isNotEmpty()) lines.add(current.toString())
                    // A single word longer than the line is placed as is rather than lost.
                    current = StringBuilder(word)
                }
            }
            if (current.isNotEmpty()) lines.add(current.toString())
            return lines
        }
    }

    private companion object {
        // A4 at 72 points per inch.
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN = 40
    }
}
