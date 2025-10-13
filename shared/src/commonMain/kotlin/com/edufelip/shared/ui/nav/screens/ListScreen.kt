package com.edufelip.shared.ui.nav.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_add
import com.edufelip.shared.resources.created
import com.edufelip.shared.resources.earlier
import com.edufelip.shared.resources.no_notes_match_search
import com.edufelip.shared.resources.order_by
import com.edufelip.shared.resources.this_month
import com.edufelip.shared.resources.this_week
import com.edufelip.shared.resources.today
import com.edufelip.shared.resources.updated
import com.edufelip.shared.resources.your_notes
import com.edufelip.shared.ui.gadgets.MaterialSearchBar
import com.edufelip.shared.ui.gadgets.NoteRow
import com.edufelip.shared.ui.nav.components.NotesEmptyState
import com.edufelip.shared.ui.settings.LocalAppPreferences
import org.jetbrains.compose.resources.stringResource

private enum class Bucket { TODAY, THIS_WEEK, THIS_MONTH, EARLIER }

@ExperimentalMaterial3Api
@Composable
fun ListScreen(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onAddClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDelete: (Note) -> Unit,
    snackBarHostState: SnackbarHostState? = null,
    title: String? = null,
    showTopAppBar: Boolean = true,
    hasAnyNotes: Boolean = true,
    headerContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
) {
    val appPrefs = LocalAppPreferences.current
    var showFilters by rememberSaveable { mutableStateOf(false) }

    @Composable
    fun ContentScaffold() {
        val scaffoldContainerColor = if (hasAnyNotes) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
        } else {
            MaterialTheme.colorScheme.background
        }
        Scaffold(
            modifier = Modifier.fillMaxSize().background(scaffoldContainerColor),
            snackbarHost = {
                if (snackBarHostState != null) SnackbarHost(snackBarHostState)
            },
            containerColor = Color.Transparent,
            topBar = if (showTopAppBar) {
                (
                    {
                        LargeTopAppBar(
                            title = {
                                val resolvedTitle = title ?: stringResource(Res.string.your_notes)
                                Text(text = resolvedTitle)
                            },
                            colors = TopAppBarDefaults.largeTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                    )
            } else {
                ({})
            },
            floatingActionButton = {
                if (hasAnyNotes) {
                    FloatingActionButton(
                        modifier = Modifier.padding(bottom = 24.dp),
                        onClick = onAddClick,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(Res.string.cd_add),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                val filtered = notes
                // Only show empty state if user truly has no notes at all
                if (!hasAnyNotes) {
                    if (emptyContent != null) {
                        emptyContent()
                    } else {
                        NotesEmptyState(
                            modifier = Modifier.fillMaxSize(),
                            onCreateNote = onAddClick,
                        )
                    }
                    return@Box
                }

                val useUpdated = remember { mutableStateOf(appPrefs.isDateModeUpdated()) }
                val now =
                    filtered.maxOfOrNull { if (useUpdated.value) it.updatedAt else it.createdAt }
                        ?: 0L

                fun bucket(ts: Long): Bucket {
                    val oneDay = 24L * 60 * 60 * 1000
                    val week = 7 * oneDay
                    val month = 30 * oneDay
                    val delta = now - ts
                    return when {
                        delta < oneDay -> Bucket.TODAY
                        delta < week -> Bucket.THIS_WEEK
                        delta < month -> Bucket.THIS_MONTH
                        else -> Bucket.EARLIER
                    }
                }

                val grouped: Map<Bucket, List<Note>> =
                    filtered.groupBy { bucket(if (useUpdated.value) it.updatedAt else it.createdAt) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    headerContent?.let { content ->
                        item(key = "list_header") {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                content()
                            }
                        }
                    }
                    stickyHeader {
                        Surface(
                            tonalElevation = 3.dp,
                            shadowElevation = 1.dp,
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MaterialSearchBar(
                                        query = searchQuery,
                                        onQueryChange = onSearchQueryChange,
                                        onFiltersClick = { showFilters = !showFilters },
                                        filtersActive = showFilters,
                                    )
                                }
                                AnimatedVisibility(
                                    visible = showFilters,
                                    enter = expandVertically(),
                                    exit = shrinkVertically(),
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Label for order mode
                                        Text(
                                            text = stringResource(Res.string.order_by),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 4.dp,
                                            ),
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 4.dp),
                                        ) {
                                            FilterChip(
                                                selected = useUpdated.value,
                                                onClick = {
                                                    useUpdated.value = true
                                                    appPrefs.setDateModeUpdated(true)
                                                },
                                                label = { Text(stringResource(Res.string.updated)) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                ),
                                                modifier = Modifier.padding(end = 8.dp),
                                            )
                                            FilterChip(
                                                selected = !useUpdated.value,
                                                onClick = {
                                                    useUpdated.value = false
                                                    appPrefs.setDateModeUpdated(false)
                                                },
                                                label = { Text(stringResource(Res.string.created)) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // If search is active and nothing matches, show a search-specific message.
                    if (searchQuery.isNotBlank() && filtered.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalArrangement = Center,
                                horizontalAlignment = CenterHorizontally,
                            ) {
                                Text(
                                    text = stringResource(Res.string.no_notes_match_search),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    val groupOrder = listOf(
                        Bucket.TODAY,
                        Bucket.THIS_WEEK,
                        Bucket.THIS_MONTH,
                        Bucket.EARLIER,
                    )
                    val orderedGroups = groupOrder.mapNotNull { b -> grouped[b]?.let { b to it } }
                    orderedGroups.forEach { (section, itemsInGroup) ->
                        item {
                            Surface(tonalElevation = 2.dp) {
                                Text(
                                    text = when (section) {
                                        Bucket.TODAY -> stringResource(Res.string.today)
                                        Bucket.THIS_WEEK -> stringResource(Res.string.this_week)
                                        Bucket.THIS_MONTH -> stringResource(Res.string.this_month)
                                        Bucket.EARLIER -> stringResource(Res.string.earlier)
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp,
                                    ),
                                )
                            }
                        }
                        items(itemsInGroup, key = { it.id }) { note ->
                            NoteRow(
                                note = note,
                                onClick = onNoteClick,
                                showUpdated = useUpdated.value,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    .animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }

    ContentScaffold()
}
