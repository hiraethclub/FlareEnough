package club.hiraeth.flareenough.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.BodyPartCategoryEntity
import club.hiraeth.flareenough.data.db.entity.BodyPartEntity
import club.hiraeth.flareenough.data.db.relation.CategoryWithParts
import club.hiraeth.flareenough.ui.support.rememberAppContainer
import club.hiraeth.flareenough.ui.symptoms.bodyPartDisplayName
import club.hiraeth.flareenough.ui.symptoms.categoryDisplayName
import kotlinx.coroutines.launch

/**
 * The body parts editor. Categories and the parts inside them can be added, renamed,
 * reordered, hidden, or removed, so someone can track exactly the places they need, a
 * single finger or toe included.
 *
 * Hiding a part keeps everything recorded on it. Removing a part, or a category with
 * parts in it, deletes the marks recorded there, so those actions ask first and say
 * how many marks would go.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyPartsScreen(onBack: () -> Unit) {
    val container = rememberAppContainer()
    val viewModel: BodyPartsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { BodyPartsViewModel(container.bodyPartRepository) }
        },
    )
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    // Which dialog, if any, is open. Only one at a time.
    var showAddCategory by remember { mutableStateOf(false) }
    var renameCategoryTarget by remember { mutableStateOf<BodyPartCategoryEntity?>(null) }
    var deleteCategoryTarget by remember { mutableStateOf<CategoryWithParts?>(null) }
    var addPartTarget by remember { mutableStateOf<BodyPartCategoryEntity?>(null) }
    var renamePartTarget by remember { mutableStateOf<BodyPartEntity?>(null) }
    var deletePartTarget by remember { mutableStateOf<Pair<BodyPartEntity, Int>?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.body_parts_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.body_parts_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )

            if (categories.isEmpty()) {
                Text(
                    stringResource(R.string.body_parts_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            categories.forEachIndexed { index, group ->
                CategoryCard(
                    group = group,
                    isFirst = index == 0,
                    isLast = index == categories.lastIndex,
                    categoryName = categoryDisplayName(context, group.category.key, group.category.name),
                    partName = { part -> bodyPartDisplayName(context, part.key, part.name) },
                    onMoveCategory = { up -> viewModel.moveCategory(group.category, up) },
                    onRenameCategory = { renameCategoryTarget = group.category },
                    onDeleteCategory = { deleteCategoryTarget = group },
                    onAddPart = { addPartTarget = group.category },
                    onMovePart = { part, up -> viewModel.movePart(group.category.id, part, up) },
                    onRenamePart = { part -> renamePartTarget = part },
                    onSetVisible = { part, visible -> viewModel.setPartVisible(part, visible) },
                    onDeletePart = { part ->
                        scope.launch {
                            val count = viewModel.markCountForPart(part.key)
                            deletePartTarget = part to count
                        }
                    },
                )
            }

            OutlinedButton(
                onClick = { showAddCategory = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    stringResource(R.string.body_parts_add_category),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            OutlinedButton(
                onClick = { viewModel.restoreDefaults() },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Icon(Icons.Filled.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    stringResource(R.string.body_parts_restore_defaults),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Spacer(modifier = Modifier.heightIn(min = 12.dp))
        }
    }

    if (showAddCategory) {
        TextEntryDialog(
            title = stringResource(R.string.body_parts_new_category_title),
            label = stringResource(R.string.body_parts_category_name_hint),
            initial = "",
            onConfirm = { name -> viewModel.addCategory(name); showAddCategory = false },
            onDismiss = { showAddCategory = false },
        )
    }

    renameCategoryTarget?.let { category ->
        TextEntryDialog(
            title = stringResource(R.string.body_parts_rename_category_title),
            label = stringResource(R.string.body_parts_category_name_hint),
            initial = categoryDisplayName(context, category.key, category.name),
            onConfirm = { name -> viewModel.renameCategory(category, name); renameCategoryTarget = null },
            onDismiss = { renameCategoryTarget = null },
        )
    }

    addPartTarget?.let { category ->
        TextEntryDialog(
            title = stringResource(R.string.body_parts_new_part_title),
            label = stringResource(R.string.body_parts_part_name_hint),
            initial = "",
            onConfirm = { name -> viewModel.addPart(category.id, name); addPartTarget = null },
            onDismiss = { addPartTarget = null },
        )
    }

    renamePartTarget?.let { part ->
        TextEntryDialog(
            title = stringResource(R.string.body_parts_rename_part_title),
            label = stringResource(R.string.body_parts_part_name_hint),
            initial = bodyPartDisplayName(context, part.key, part.name),
            onConfirm = { name -> viewModel.renamePart(part, name); renamePartTarget = null },
            onDismiss = { renamePartTarget = null },
        )
    }

    deletePartTarget?.let { (part, markCount) ->
        val message = if (markCount > 0) {
            pluralStringResource(R.plurals.body_parts_delete_part_marks, markCount, markCount)
        } else {
            stringResource(R.string.body_parts_delete_part_message)
        }
        ConfirmDialog(
            title = stringResource(R.string.body_parts_delete_part_title),
            message = message,
            onConfirm = { viewModel.deletePart(part); deletePartTarget = null },
            onDismiss = { deletePartTarget = null },
        )
    }

    deleteCategoryTarget?.let { group ->
        val partCount = group.parts.size
        val message = if (partCount > 0) {
            pluralStringResource(R.plurals.body_parts_delete_category_parts, partCount, partCount)
        } else {
            stringResource(R.string.body_parts_delete_category_message)
        }
        ConfirmDialog(
            title = stringResource(R.string.body_parts_delete_category_title),
            message = message,
            onConfirm = { viewModel.deleteCategory(group.category); deleteCategoryTarget = null },
            onDismiss = { deleteCategoryTarget = null },
        )
    }
}

@Composable
private fun CategoryCard(
    group: CategoryWithParts,
    isFirst: Boolean,
    isLast: Boolean,
    categoryName: String,
    partName: (BodyPartEntity) -> String,
    onMoveCategory: (up: Boolean) -> Unit,
    onRenameCategory: () -> Unit,
    onDeleteCategory: () -> Unit,
    onAddPart: () -> Unit,
    onMovePart: (BodyPartEntity, up: Boolean) -> Unit,
    onRenamePart: (BodyPartEntity) -> Unit,
    onSetVisible: (BodyPartEntity, Boolean) -> Unit,
    onDeletePart: (BodyPartEntity) -> Unit,
) {
    val parts = group.parts.sortedBy { it.sortOrder }
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                RowMenu(
                    targetName = categoryName,
                    canMoveUp = !isFirst,
                    canMoveDown = !isLast,
                    onMoveUp = { onMoveCategory(true) },
                    onMoveDown = { onMoveCategory(false) },
                    onRename = onRenameCategory,
                    onDelete = onDeleteCategory,
                )
            }

            parts.forEachIndexed { index, part ->
                PartRow(
                    name = partName(part),
                    visible = part.visible,
                    canMoveUp = index != 0,
                    canMoveDown = index != parts.lastIndex,
                    onMoveUp = { onMovePart(part, true) },
                    onMoveDown = { onMovePart(part, false) },
                    onRename = { onRenamePart(part) },
                    onSetVisible = { visible -> onSetVisible(part, visible) },
                    onDelete = { onDeletePart(part) },
                )
            }

            TextButton(
                onClick = onAddPart,
                modifier = Modifier.heightIn(min = 56.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    stringResource(R.string.body_parts_add_part),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PartRow(
    name: String,
    visible: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRename: () -> Unit,
    onSetVisible: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val shownLabel = stringResource(R.string.body_parts_shown)
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = visible,
            onCheckedChange = onSetVisible,
            modifier = Modifier.semantics { contentDescription = "$name, $shownLabel" },
        )
        RowMenu(
            targetName = name,
            canMoveUp = canMoveUp,
            canMoveDown = canMoveDown,
            onMoveUp = onMoveUp,
            onMoveDown = onMoveDown,
            onRename = onRename,
            onDelete = onDelete,
        )
    }
}

/** The shared up, down, rename, remove menu used by both category and part rows. */
@Composable
private fun RowMenu(
    targetName: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(
        onClick = { expanded = true },
        modifier = Modifier.size(56.dp),
    ) {
        Icon(
            Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.body_parts_more_options, targetName),
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        if (canMoveUp) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.body_parts_move_up)) },
                leadingIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null) },
                onClick = { expanded = false; onMoveUp() },
            )
        }
        if (canMoveDown) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.body_parts_move_down)) },
                leadingIcon = { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null) },
                onClick = { expanded = false; onMoveDown() },
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.body_parts_rename)) },
            onClick = { expanded = false; onRename() },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_delete)) },
            onClick = { expanded = false; onDelete() },
        )
    }
}

@Composable
private fun TextEntryDialog(
    title: String,
    label: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text.trim()) },
                enabled = text.isNotBlank(),
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
