package com.edufelip.shared.ui.gadgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.edufelip.shared.i18n.Str
import com.edufelip.shared.i18n.string

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val active = remember { mutableStateOf(false) }
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { active.value = false },
        active = active.value,
        onActiveChange = { active.value = it },
        leadingIcon = {
            if (active.value) {
                IconButton(onClick = { active.value = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = string(Str.CdBack))
                }
            } else {
                Icon(Icons.Default.Search, contentDescription = string(Str.CdSearch))
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = string(Str.CdClearSearch))
                }
            }
        },
        placeholder = { Text(string(Str.Search)) },
        modifier = modifier
    ) {}
}

