package club.hiraeth.flareenough.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.ui.support.rememberAppContainer
import club.hiraeth.flareenough.ui.symptoms.bodyRegionGroups
import club.hiraeth.flareenough.ui.symptoms.labelRes

/**
 * Grouped checkboxes for choosing which body parts show up when logging symptoms.
 * A ticked box means the part is shown. Unticking hides it from the log, without
 * touching anything already recorded.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyPartsScreen(onBack: () -> Unit) {
    val container = rememberAppContainer()
    val viewModel: BodyPartsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { BodyPartsViewModel(container.settingsRepository) }
        },
    )
    val hidden by viewModel.hiddenRegions.collectAsStateWithLifecycle()

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
        ) {
            Text(
                stringResource(R.string.body_parts_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            bodyRegionGroups.forEach { (areaRes, regions) ->
                Text(
                    stringResource(areaRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
                )
                regions.forEach { region ->
                    BodyPartRow(
                        name = stringResource(region.labelRes()),
                        shown = region !in hidden,
                        onToggle = { shown -> viewModel.setVisible(region, shown) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BodyPartRow(
    name: String,
    shown: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(
                value = shown,
                role = Role.Checkbox,
                onValueChange = onToggle,
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Checkbox(checked = shown, onCheckedChange = null)
        Text(name, style = MaterialTheme.typography.bodyLarge)
    }
}
