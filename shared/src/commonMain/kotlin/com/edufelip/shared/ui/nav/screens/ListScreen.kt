package com.edufelip.shared.ui.nav.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_add
import com.edufelip.shared.resources.cd_open_drawer
import com.edufelip.shared.resources.created
import com.edufelip.shared.resources.earlier
import com.edufelip.shared.resources.empty_notes_hint
import com.edufelip.shared.resources.empty_notes_title
import com.edufelip.shared.resources.high_priority
import com.edufelip.shared.resources.low_priority
import com.edufelip.shared.resources.medium_priority
import com.edufelip.shared.resources.no_notes_match_filter
import com.edufelip.shared.resources.no_notes_match_search
import com.edufelip.shared.resources.order_by
import com.edufelip.shared.resources.priority
import com.edufelip.shared.resources.this_month
import com.edufelip.shared.resources.this_week
import com.edufelip.shared.resources.today
import com.edufelip.shared.resources.updated
import com.edufelip.shared.resources.your_notes
import com.edufelip.shared.ui.gadgets.DismissibleNoteRow
import com.edufelip.shared.ui.gadgets.MaterialSearchBar
import com.edufelip.shared.ui.gadgets.RailContent
import com.edufelip.shared.ui.settings.LocalAppPreferences
import kotlinx.coroutines.launch
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
    drawerState: DrawerState,
    drawerContent: (@Composable () -> Unit)? = null,
    onDelete: (Note) -> Unit,
    // For large screens with NavigationRail
    darkTheme: Boolean = false,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onOpenTrash: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
    managedByShell: Boolean = false,
    showTopAppBar: Boolean = true,
    hasAnyNotes: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    val appPrefs = LocalAppPreferences.current
    val selectedFilter =
        remember { mutableStateOf(appPrefs.getPriorityFilter()) }
    var showFilters by rememberSaveable { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLarge = maxWidth > 600.dp
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        @Composable
        fun ContentScaffold(showNavIcon: Boolean) {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                snackbarHost = {
                    if (snackbarHostState != null) SnackbarHost(snackbarHostState)
                },
                topBar = if (showTopAppBar) {
                    (
                        {
                            LargeTopAppBar(
                                title = { Text(text = stringResource(Res.string.your_notes)) },
                                navigationIcon = {
                                    if (showNavIcon) {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = stringResource(Res.string.cd_open_drawer),
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.largeTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                ),
                                scrollBehavior = scrollBehavior,
                            )
                        }
                        )
                } else {
                    ({})
                },
                floatingActionButton = {
                    FloatingActionButton(
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
                },
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    val filtered = when (val f = selectedFilter.value) {
                        null -> notes
                        else -> notes.filter { it.priority == f }
                    }
                    // Only show empty state if user truly has no notes at all
                    if (selectedFilter.value == null && !hasAnyNotes) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Center,
                            horizontalAlignment = CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(Res.string.empty_notes_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = stringResource(Res.string.empty_notes_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        return@Box
                    }

                    val useUpdated =
                        remember { mutableStateOf(appPrefs.isDateModeUpdated()) }
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
                        contentPadding = PaddingValues(bottom = 96.dp),
                    ) {
                        stickyHeader {
                            Surface(
                                tonalElevation = 3.dp,
                                shadowElevation = 1.dp,
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                                                modifier = Modifier
                                                    .fillMaxWidth()
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
                                            // Label for priority filter
                                            Text(
                                                text = stringResource(Res.string.priority),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp,
                                                    vertical = 4.dp,
                                                ),
                                            )
                                            @OptIn(ExperimentalLayoutApi::class)
                                            FlowRow(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                val chips =
                                                    listOf<Pair<String, Priority?>>("All" to null) + listOf(
                                                        stringResource(Res.string.high_priority) to Priority.HIGH,
                                                        stringResource(Res.string.medium_priority) to Priority.MEDIUM,
                                                        stringResource(Res.string.low_priority) to Priority.LOW,
                                                    )
                                                chips.forEach { (label, value) ->
                                                    val selected = selectedFilter.value == value
                                                    FilterChip(
                                                        selected = selected,
                                                        onClick = {
                                                            selectedFilter.value = value
                                                            appPrefs.setPriorityFilter(value)
                                                        },
                                                        label = { Text(label) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        ),
                                                        modifier = Modifier,
                                                    )
                                                }
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
                                    modifier = Modifier
                                        .fillMaxWidth()
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
                        } else if (selectedFilter.value != null && filtered.isEmpty()) {
                            // If a specific priority filter is selected and nothing matches,
                            // show the empty message without hiding the header/filters.
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                    verticalArrangement = Center,
                                    horizontalAlignment = CenterHorizontally,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.no_notes_match_filter),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }

                        grouped.forEach { (section, itemsInGroup) ->
                            stickyHeader {
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
                                DismissibleNoteRow(
                                    note = note,
                                    onClick = onNoteClick,
                                    onDismiss = onDelete,
                                    isRestore = false,
                                    showUpdated = useUpdated.value,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isLarge && !managedByShell) {
            Row(modifier = Modifier.fillMaxSize()) {
                RailContent(
                    onYourNotesClick = { /* already on home */ },
                    onTrashClick = { onOpenTrash?.invoke() },
                    darkTheme = darkTheme,
                    onToggleDarkTheme = onToggleDarkTheme,
                    selectedHome = true,
                    selectedTrash = false,
                )
                Box(modifier = Modifier.weight(1f)) {
                    ContentScaffold(showNavIcon = false)
                }
            }
        } else if (!managedByShell) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = { if (drawerContent != null) drawerContent() },
            ) {
                ContentScaffold(showNavIcon = true)
            }
        } else {
            ContentScaffold(showNavIcon = false)
        }
    }
}
