package club.hiraeth.flareenough.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A completed stillness session, logged to the timeline with duration only.
 * No streaks, no targets.
 */
@Entity(tableName = "meditation_sessions", indices = [Index("startTimeMillis")])
data class MeditationSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTimeMillis: Long,
    val durationSeconds: Int,
    val type: SessionType,
    val createdAtMillis: Long,
)
