package club.hiraeth.flareenough.data.repository

import club.hiraeth.flareenough.data.db.BodyPartDefaults
import club.hiraeth.flareenough.data.db.dao.BodyPartDao
import club.hiraeth.flareenough.data.db.entity.BodyPartCategoryEntity
import club.hiraeth.flareenough.data.db.entity.BodyPartEntity
import club.hiraeth.flareenough.data.db.relation.CategoryWithParts
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * The editable body parts and categories. Everything a person can do in the body parts
 * editor goes through here: add, rename, reorder, hide, and delete. Hiding is always
 * non destructive; deleting a part with marks removes those marks, so callers confirm
 * first.
 */
class BodyPartRepository(private val dao: BodyPartDao) {

    fun observeCategoriesWithParts(): Flow<List<CategoryWithParts>> =
        dao.observeCategoriesWithParts()

    suspend fun getAllParts(): List<BodyPartEntity> = dao.getAllParts()

    suspend fun markCountForPart(key: String): Int = dao.markCountForPart(key)

    suspend fun partCountInCategory(categoryId: Long): Int = dao.partCountInCategory(categoryId)

    // Categories.

    suspend fun addCategory(name: String, nowMillis: Long): Long {
        val sortOrder = dao.maxCategorySortOrder() + 1
        return dao.insertCategory(
            BodyPartCategoryEntity(
                key = "cat_${UUID.randomUUID()}",
                name = name.trim(),
                sortOrder = sortOrder,
                createdAtMillis = nowMillis,
            ),
        )
    }

    suspend fun renameCategory(category: BodyPartCategoryEntity, name: String) {
        dao.updateCategory(category.copy(name = name.trim()))
    }

    suspend fun deleteCategory(category: BodyPartCategoryEntity) {
        dao.deleteCategory(category)
    }

    /** Rewrite category order to match the given id order. */
    suspend fun reorderCategories(orderedCategories: List<BodyPartCategoryEntity>) {
        orderedCategories.forEachIndexed { index, category ->
            if (category.sortOrder != index) dao.updateCategory(category.copy(sortOrder = index))
        }
    }

    // Parts.

    suspend fun addPart(categoryId: Long, name: String, nowMillis: Long): Long {
        val sortOrder = dao.maxPartSortOrder(categoryId) + 1
        return dao.insertPart(
            BodyPartEntity(
                categoryId = categoryId,
                key = "part_${UUID.randomUUID()}",
                name = name.trim(),
                visible = true,
                sortOrder = sortOrder,
                createdAtMillis = nowMillis,
            ),
        )
    }

    suspend fun renamePart(part: BodyPartEntity, name: String) {
        dao.updatePart(part.copy(name = name.trim()))
    }

    suspend fun setPartVisible(part: BodyPartEntity, visible: Boolean) {
        dao.setPartVisible(part.id, visible)
    }

    /** Delete a part. If it has marks, they are removed too (callers confirm first). */
    suspend fun deletePart(part: BodyPartEntity) {
        dao.deletePartWithMarks(part)
    }

    suspend fun reorderParts(orderedParts: List<BodyPartEntity>) {
        orderedParts.forEachIndexed { index, part ->
            if (part.sortOrder != index) dao.updatePart(part.copy(sortOrder = index))
        }
    }

    /**
     * Put back any default category or part the person has removed, leaving everything
     * they still have untouched. A way out if someone deletes a default and wants it
     * back.
     */
    suspend fun restoreDefaults(nowMillis: Long) {
        val existingCategories = dao.getCategories()
        val categoryIdByKey = existingCategories.associate { it.key to it.id }.toMutableMap()
        var nextCategoryOrder = dao.maxCategorySortOrder() + 1
        val existingPartKeys = dao.getAllParts().map { it.key }.toSet()

        for (defaultCategory in BodyPartDefaults.categories) {
            var categoryId = categoryIdByKey[defaultCategory.key]
            if (categoryId == null) {
                categoryId = dao.insertCategory(
                    BodyPartCategoryEntity(
                        key = defaultCategory.key,
                        name = null,
                        sortOrder = nextCategoryOrder++,
                        createdAtMillis = nowMillis,
                    ),
                )
                categoryIdByKey[defaultCategory.key] = categoryId
            }
            var nextPartOrder = dao.maxPartSortOrder(categoryId) + 1
            for (partKey in defaultCategory.partKeys) {
                if (partKey !in existingPartKeys) {
                    dao.insertPart(
                        BodyPartEntity(
                            categoryId = categoryId,
                            key = partKey,
                            name = null,
                            visible = true,
                            sortOrder = nextPartOrder++,
                            createdAtMillis = nowMillis,
                        ),
                    )
                }
            }
        }
    }
}
