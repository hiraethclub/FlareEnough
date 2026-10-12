package club.hiraeth.flareenough.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A category of body parts, for example "Hands" or "Legs and feet". The person can
 * add, rename, reorder, and remove categories.
 *
 * [key] is a stable identifier. The built in categories keep a fixed key (head, arms,
 * hands, back, legs) so their name can still come from translated string resources.
 * [name] is null for a built in category that has not been renamed, in which case the
 * app shows the translated default name; it holds the person's own text once set or
 * for any category they added.
 */
@Entity(
    tableName = "body_part_categories",
    indices = [Index(value = ["key"], unique = true)],
)
data class BodyPartCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val name: String? = null,
    val sortOrder: Int = 0,
    val createdAtMillis: Long,
)

/**
 * A single body part the person can mark on the body map, belonging to a category.
 * They can add their own (for example a specific finger or toe), rename, reorder, hide,
 * or delete them.
 *
 * [key] is the stable identifier stored against each logged mark (in body_map_entries),
 * so renaming a part never loses its history. Built in parts keep their original key
 * (the old region name, for example HAND_LEFT), which is why upgrading never disturbs
 * marks that were made before this feature. [name] works like the category name: null
 * means show the translated default, otherwise the person's own text.
 *
 * [visible] controls whether the part is offered when logging. Hiding is non
 * destructive: a hidden part keeps everything already recorded against it.
 */
@Entity(
    tableName = "body_parts",
    foreignKeys = [
        ForeignKey(
            entity = BodyPartCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("categoryId"), Index(value = ["key"], unique = true)],
)
data class BodyPartEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val key: String,
    val name: String? = null,
    val visible: Boolean = true,
    val sortOrder: Int = 0,
    val createdAtMillis: Long,
)
