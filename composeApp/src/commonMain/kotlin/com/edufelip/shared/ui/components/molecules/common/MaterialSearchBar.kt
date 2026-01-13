package com.edufelip.shared.ui.components.molecules.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_clear_search
import com.edufelip.shared.resources.cd_search
import com.edufelip.shared.resources.filters
import com.edufelip.shared.resources.search
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onFiltersClick: () -> Unit = {},
    filtersActive: Boolean = false,
) {
    val tokens = designTokens()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    DockedSearchBar(
        modifier = modifier,
        expanded = false,
        onExpandedChange = {},
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                expanded = false,
                onExpandedChange = {},
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(tokens.spacing.xxl),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(Res.string.cd_search),
                        )
                    }
                },
                trailingIcon = {
                    androidx.compose.foundation.layout.Row {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.cd_clear_search))
                            }
                        }
                        IconButton(onClick = onFiltersClick) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = stringResource(Res.string.filters),
                                tint = if (filtersActive) {
                                    tokens.colors.accent
                                } else {
                                    tokens.colors.muted
                                },
                            )
                        }
                    }
                },
                placeholder = { Text(stringResource(Res.string.search)) },
            )
        },
        content = {},
    )
}

@Preview
@DevicePreviews
@Composable
private fun MaterialSearchBarPreview() {
    val (query, setQuery) = remember { mutableStateOf("Folders") }
    DevicePreviewContainer {
        MaterialSearchBar(
            query = query,
            onQueryChange = setQuery,
            filtersActive = true,
        )
    }
}
