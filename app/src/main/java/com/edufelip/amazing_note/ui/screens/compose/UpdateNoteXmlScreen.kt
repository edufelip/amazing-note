package com.edufelip.amazing_note.ui.screens.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.edufelip.amazing_note.R
import com.edufelip.amazing_note.data.models.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateNoteXmlScreen(
    title: String,
    onTitleChange: (String) -> Unit,
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painter = painterResource(id = R.drawable.ic_arrow_left), contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onSave) {
                        Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null)
                    }
                }
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
                modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                singleLine = true,
                placeholder = { Text("Title") },
                enabled = !readOnly
            )
            Spacer(Modifier.height(8.dp))
            PriorityDropdownReadOnly(priority = priority, onPriorityChange = onPriorityChange, enabled = !readOnly)
            Spacer(Modifier.height(8.dp))
            TextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                placeholder = { Text("Description") },
                enabled = !readOnly
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityDropdownReadOnly(
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
    enabled: Boolean
) {
    val priorities = listOf(Priority.HIGH, Priority.MEDIUM, Priority.LOW)
    val expanded = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded.value && enabled, onExpandedChange = { if (enabled) expanded.value = it }) {
        androidx.compose.material3.TextField(
            value = priority.name.lowercase().replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) }
        )
        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            priorities.forEach { item ->
                DropdownMenuItem(text = { Text(item.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = {
                    onPriorityChange(item)
                    expanded.value = false
                })
            }
        }
    }
}
