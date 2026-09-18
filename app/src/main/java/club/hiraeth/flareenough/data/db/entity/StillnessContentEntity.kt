package club.hiraeth.flareenough.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A short stillness prompt the person added themselves, for example a favourite
 * line or quote. Shipped prompts are original lines in the app resources; this
 * table holds the person's own, so they can include any words that matter to them.
 */
@Entity(tableName = "stillness_prompts")
data class PromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAtMillis: Long,
)

/**
 * A longer reading the person added, for example the Heart Sutra or a discourse in
 * the translation they use. The app ships no scripture text of its own, so nothing
 * copyrighted is bundled. The person fills this with what they want.
 */
@Entity(tableName = "readings")
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val sortOrder: Int = 0,
    val createdAtMillis: Long,
)
