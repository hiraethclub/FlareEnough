package club.hiraeth.flareenough.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import club.hiraeth.flareenough.data.db.entity.BodyPartCategoryEntity
import club.hiraeth.flareenough.data.db.entity.BodyPartEntity

/** A body part category together with its parts, for the body map and the editor. */
data class CategoryWithParts(
    @Embedded val category: BodyPartCategoryEntity,
    @Relation(parentColumn = "id", entityColumn = "categoryId")
    val parts: List<BodyPartEntity>,
)
