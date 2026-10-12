package club.hiraeth.flareenough.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import club.hiraeth.flareenough.data.db.entity.BodyPartCategoryEntity
import club.hiraeth.flareenough.data.db.entity.BodyPartEntity
import club.hiraeth.flareenough.data.db.relation.CategoryWithParts
import kotlinx.coroutines.flow.Flow

/** Reads and writes the editable body parts and their categories. */
@Dao
interface BodyPartDao {

    // Categories with their parts, for the body map and the editor.

    @Transaction
    @Query("SELECT * FROM body_part_categories ORDER BY sortOrder ASC, id ASC")
    fun observeCategoriesWithParts(): Flow<List<CategoryWithParts>>

    @Query("SELECT COUNT(*) FROM body_part_categories")
    suspend fun categoryCount(): Int

    @Query("SELECT * FROM body_part_categories ORDER BY sortOrder ASC, id ASC")
    suspend fun getCategories(): List<BodyPartCategoryEntity>

    @Query("SELECT * FROM body_parts ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllParts(): List<BodyPartEntity>

    @Query("SELECT COUNT(*) FROM body_parts WHERE categoryId = :categoryId")
    suspend fun partCountInCategory(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM body_map_entries WHERE region = :key")
    suspend fun markCountForPart(key: String): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM body_part_categories")
    suspend fun maxCategorySortOrder(): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM body_parts WHERE categoryId = :categoryId")
    suspend fun maxPartSortOrder(categoryId: Long): Int

    // Writes.

    @Insert
    suspend fun insertCategory(category: BodyPartCategoryEntity): Long

    @Update
    suspend fun updateCategory(category: BodyPartCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: BodyPartCategoryEntity)

    @Insert
    suspend fun insertPart(part: BodyPartEntity): Long

    @Update
    suspend fun updatePart(part: BodyPartEntity)

    @Delete
    suspend fun deletePart(part: BodyPartEntity)

    @Query("UPDATE body_parts SET visible = :visible WHERE id = :id")
    suspend fun setPartVisible(id: Long, visible: Boolean)

    @Query("DELETE FROM body_map_entries WHERE region = :key")
    suspend fun deleteMarksForPart(key: String)

    /** Remove a part and every mark recorded against it, in one transaction. */
    @Transaction
    suspend fun deletePartWithMarks(part: BodyPartEntity) {
        deleteMarksForPart(part.key)
        deletePart(part)
    }
}
