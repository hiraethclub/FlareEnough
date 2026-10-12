package club.hiraeth.flareenough.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.flareenough.data.db.entity.BodyPartCategoryEntity
import club.hiraeth.flareenough.data.db.entity.BodyPartEntity
import club.hiraeth.flareenough.data.db.relation.CategoryWithParts
import club.hiraeth.flareenough.data.repository.BodyPartRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the body parts editor. The person can add their own categories and parts
 * (for example a single finger or toe), rename them, reorder them, hide the ones they
 * do not use, and remove them.
 *
 * Hiding is always safe and keeps every recorded mark. Removing a part deletes the
 * marks recorded on it, so the screen confirms first and says how many marks would go.
 * A category can only be removed once it is empty, so removing one never deletes parts
 * or marks; the screen asks the person to clear its parts out first.
 */
class BodyPartsViewModel(
    private val repository: BodyPartRepository,
) : ViewModel() {

    val categories: StateFlow<List<CategoryWithParts>> =
        repository.observeCategoriesWithParts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun now() = System.currentTimeMillis()

    // Categories.

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addCategory(name, now()) }
    }

    fun renameCategory(category: BodyPartCategoryEntity, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.renameCategory(category, name) }
    }

    /**
     * Remove a category, but only once it is empty. A category that still holds parts is
     * protected, so deleting one can never strand the marks recorded on its parts. The
     * screen stops this case before it reaches here; this guard is a second line.
     */
    fun deleteCategory(category: BodyPartCategoryEntity) {
        val group = categories.value.firstOrNull { it.category.id == category.id }
        if (group != null && group.parts.isNotEmpty()) return
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    fun moveCategory(category: BodyPartCategoryEntity, up: Boolean) {
        val ordered = categories.value.map { it.category }
        val index = ordered.indexOfFirst { it.id == category.id }
        val target = if (up) index - 1 else index + 1
        if (index < 0 || target !in ordered.indices) return
        val reordered = ordered.toMutableList().also {
            val moved = it.removeAt(index)
            it.add(target, moved)
        }
        viewModelScope.launch { repository.reorderCategories(reordered) }
    }

    // Parts.

    fun addPart(categoryId: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addPart(categoryId, name, now()) }
    }

    fun renamePart(part: BodyPartEntity, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.renamePart(part, name) }
    }

    fun setPartVisible(part: BodyPartEntity, visible: Boolean) {
        viewModelScope.launch { repository.setPartVisible(part, visible) }
    }

    fun deletePart(part: BodyPartEntity) {
        viewModelScope.launch { repository.deletePart(part) }
    }

    fun movePart(categoryId: Long, part: BodyPartEntity, up: Boolean) {
        val group = categories.value.firstOrNull { it.category.id == categoryId } ?: return
        val ordered = group.parts.sortedBy { it.sortOrder }
        val index = ordered.indexOfFirst { it.id == part.id }
        val target = if (up) index - 1 else index + 1
        if (index < 0 || target !in ordered.indices) return
        val reordered = ordered.toMutableList().also {
            val moved = it.removeAt(index)
            it.add(target, moved)
        }
        viewModelScope.launch { repository.reorderParts(reordered) }
    }

    fun restoreDefaults() {
        viewModelScope.launch { repository.restoreDefaults(now()) }
    }

    /** How many recorded marks a part has, so the delete prompt can warn before removing them. */
    suspend fun markCountForPart(key: String): Int = repository.markCountForPart(key)
}
