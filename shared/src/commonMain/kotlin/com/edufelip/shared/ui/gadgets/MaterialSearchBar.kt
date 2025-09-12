package com.edufelip.shared.ui.gadgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_clear_search
import com.edufelip.shared.resources.cd_search
import com.edufelip.shared.resources.filters
import com.edufelip.shared.resources.search
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onFiltersClick: () -> Unit = {},
    filtersActive: Boolean = false,
) {
    // Keep the search bar non-expanding to avoid measuring issues inside stickyHeader/LazyColumn.
    SearchBar(
        modifier = modifier,
        expanded = false,
        onExpandedChange = {},
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = stringResource(Res.string.cd_search))
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
                                tint = if (filtersActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
