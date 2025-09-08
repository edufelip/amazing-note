package com.edufelip.shared.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Priority
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import com.edufelip.shared.i18n.Str
import com.edufelip.shared.i18n.string

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
    descriptionError: String? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = string(Str.CdBack), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onSave) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = string(Str.CdSave), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = string(Str.CdDelete), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                singleLine = true,
                isError = titleError != null,
                placeholder = { Text(string(Str.Title)) },
                supportingText = {
                    if (titleError != null) Text(titleError)
                }
            )
            Spacer(Modifier.height(8.dp))
            PriorityDropdown(priority = priority, onPriorityChange = onPriorityChange)
            Spacer(Modifier.height(8.dp))
            TextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                isError = descriptionError != null,
                placeholder = { Text(string(Str.Description)) },
                supportingText = {
                    if (descriptionError != null) Text(descriptionError)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityDropdown(
    priority: Priority,
    onPriorityChange: (Priority) -> Unit
) {
    val priorities = listOf(Priority.HIGH, Priority.MEDIUM, Priority.LOW)
    val expanded = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded.value, onExpandedChange = { expanded.value = it }) {
        val label = when (priority) {
            Priority.HIGH -> string(Str.HighPriority)
            Priority.MEDIUM -> string(Str.MediumPriority)
            Priority.LOW -> string(Str.LowPriority)
        }
        TextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            priorities.forEach { item ->
                val itemLabel = when (item) {
                    Priority.HIGH -> string(Str.HighPriority)
                    Priority.MEDIUM -> string(Str.MediumPriority)
                    Priority.LOW -> string(Str.LowPriority)
                }
                DropdownMenuItem(text = { Text(itemLabel) }, onClick = {
                    onPriorityChange(item)
                    expanded.value = false
                })
            }
        }
    }
}
