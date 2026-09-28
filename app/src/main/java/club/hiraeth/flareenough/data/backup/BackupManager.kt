package club.hiraeth.flareenough.data.backup

import android.content.Context
import android.net.Uri
import club.hiraeth.flareenough.data.db.FlareDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Backs up and restores the whole database as a single file.
 *
 * A backup is an exact copy of the on device database, so it holds everything the app
 * records: medications, doses, symptoms, the body map, notes, tags, period days, and
 * stillness sessions. Nothing leaves the device on its own. The person chooses where
 * the file goes through the system file picker, and chooses which file to restore.
 *
 * Restore replaces all current data with the backup, so the app must be restarted
 * right after (the caller does this) to reopen the fresh database cleanly.
 */
class BackupManager(
    private val context: Context,
    private val database: FlareDatabase,
) {

    private fun databaseFile(): File = context.getDatabasePath(FlareDatabase.NAME)

    /** Copy the current database to the chosen file. */
    suspend fun exportTo(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Fold the write ahead log back into the main database file, so the single
            // file we copy is complete and consistent.
            database.openHelper.writableDatabase
                .query("PRAGMA wal_checkpoint(TRUNCATE)")
                .use { it.moveToFirst() }

            val out = context.contentResolver.openOutputStream(uri)
                ?: error("Could not open the chosen file to write the backup.")
            out.use { stream ->
                databaseFile().inputStream().use { it.copyTo(stream) }
            }
            Unit
        }
    }

    /**
     * Replace the current database with the chosen backup file. The file is read fully
     * and checked before anything is overwritten, so a wrong or unreadable file leaves
     * the existing data untouched. After this succeeds the app must be restarted.
     */
    suspend fun importFrom(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Could not open the chosen file.")
            require(looksLikeSqlite(bytes)) {
                "That file is not a Flare Enough backup."
            }

            database.close()

            val dbFile = databaseFile()
            // Remove the write ahead log and shared memory side files, so no leftover
            // pages from the old database are applied on top of the restored one.
            File(dbFile.parentFile, "${dbFile.name}-wal").delete()
            File(dbFile.parentFile, "${dbFile.name}-shm").delete()

            dbFile.outputStream().use { it.write(bytes) }
        }
    }

    /** Every SQLite file begins with this fixed 16 byte header. */
    private fun looksLikeSqlite(bytes: ByteArray): Boolean {
        val magic = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
        if (bytes.size < magic.size) return false
        for (i in magic.indices) {
            if (bytes[i] != magic[i]) return false
        }
        return true
    }
}
