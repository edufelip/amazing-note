package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Priority
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_back
import com.edufelip.shared.resources.cd_delete
import com.edufelip.shared.resources.cd_save
import com.edufelip.shared.resources.description
import com.edufelip.shared.resources.high_priority
import com.edufelip.shared.resources.low_priority
import com.edufelip.shared.resources.medium_priority
import com.edufelip.shared.resources.title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    title: String,
    onTitleChange: (String) -> Unit,
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    titleError: String? = null,
    descriptionError: String? = null,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.cd_back), tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = onSave) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = stringResource(Res.string.cd_save), tint = MaterialTheme.colorScheme.onSurface)
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(Res.string.cd_delete), tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                isError = titleError != null,
                label = { Text(stringResource(Res.string.title)) },
                supportingText = {
                    if (titleError != null) Text(titleError)
                },
            )
            Spacer(Modifier.height(8.dp))
            PriorityDropdown(priority = priority, onPriorityChange = onPriorityChange)
            Spacer(Modifier.height(8.dp))
            TextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                isError = descriptionError != null,
                label = { Text(stringResource(Res.string.description)) },
                supportingText = {
                    if (descriptionError != null) Text(descriptionError)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityDropdown(
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
) {
    val priorities = listOf(Priority.HIGH, Priority.MEDIUM, Priority.LOW)
    val expanded = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded.value, onExpandedChange = { expanded.value = it }) {
        val label = when (priority) {
            Priority.HIGH -> stringResource(Res.string.high_priority)
            Priority.MEDIUM -> stringResource(Res.string.medium_priority)
            Priority.LOW -> stringResource(Res.string.low_priority)
        }
        TextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            priorities.forEach { item ->
                val itemLabel = when (item) {
                    Priority.HIGH -> stringResource(Res.string.high_priority)
                    Priority.MEDIUM -> stringResource(Res.string.medium_priority)
                    Priority.LOW -> stringResource(Res.string.low_priority)
                }
                DropdownMenuItem(text = { Text(itemLabel) }, onClick = {
                    onPriorityChange(item)
                    expanded.value = false
                })
            }
        }
    }
}
