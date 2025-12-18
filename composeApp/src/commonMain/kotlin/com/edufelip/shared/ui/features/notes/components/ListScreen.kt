package com.edufelip.shared.ui.features.notes.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.edufelip.shared.domain.model.Note
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
import com.edufelip.shared.ui.components.molecules.common.MaterialSearchBar
import com.edufelip.shared.ui.components.molecules.notes.NoteRow
import com.edufelip.shared.ui.components.organisms.common.NotesEmptyState
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import com.edufelip.shared.ui.settings.LocalAppPreferences
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

private enum class Bucket { TODAY, THIS_WEEK, THIS_MONTH, EARLIER }

@ExperimentalMaterial3Api
@Composable
fun ListScreen(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onAddClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    title: String? = null,
    showTopAppBar: Boolean = true,
    searchVisible: Boolean = true,
    hasAnyNotes: Boolean = true,
    headerContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
    showFab: Boolean = true,
) {
    val appPrefs = LocalAppPreferences.current
    var showFilters by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(searchVisible) {
        if (!searchVisible) showFilters = false
    }
    val tokens = designTokens()

    @Composable
    fun ContentScaffold() {
        val scaffoldContainerColor = if (hasAnyNotes) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(tokens.elevation.sheet)
        } else {
            tokens.colors.canvas
        }
        Scaffold(
            modifier = Modifier.fillMaxSize().background(scaffoldContainerColor),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(),
            topBar = if (showTopAppBar) {
                (
                    {
                        LargeTopAppBar(
                            title = {
                                val resolvedTitle = title ?: stringResource(Res.string.your_notes)
                                Text(text = resolvedTitle)
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
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
                if (showFab && hasAnyNotes) {
                    FloatingActionButton(
                        modifier = Modifier.padding(bottom = tokens.spacing.xxxl),
                        onClick = onAddClick,
                        containerColor = tokens.colors.accent,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(tokens.radius.lg),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(Res.string.cd_add),
                        )
                    }
                }
            },
        ) { _ ->
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
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
                    notes.maxOfOrNull { if (useUpdated.value) it.updatedAt else it.createdAt } ?: 0L

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
                    notes.groupBy { bucket(if (useUpdated.value) it.updatedAt else it.createdAt) }
                val listBottomPadding = tokens.spacing.zero
                val searchHorizontalPadding = tokens.spacing.md
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = listBottomPadding),
                ) {
                    headerContent?.let { content ->
                        item(key = "list_header") {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(
                                    horizontal = searchHorizontalPadding,
                                    vertical = tokens.spacing.lg,
                                ),
                            ) {
                                content()
                            }
                        }
                    }
                    stickyHeader {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(tokens.colors.surface)
                                .animateContentSize(animationSpec = tween(durationMillis = 220, easing = LinearEasing)),
                        ) {

                            AnimatedVisibility(
                                visible = searchVisible,
                                enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(durationMillis = 220, easing = LinearEasing)) + fadeIn(tween(durationMillis = 220, easing = LinearEasing)),
                                exit = shrinkVertically(animationSpec = tween(durationMillis = 220, easing = LinearEasing)) + fadeOut(tween(durationMillis = 220, easing = LinearEasing)),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = searchHorizontalPadding,
                                            vertical = tokens.spacing.sm,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    MaterialSearchBar(
                                        query = searchQuery,
                                        onQueryChange = onSearchQueryChange,
                                        onFiltersClick = { showFilters = !showFilters },
                                        filtersActive = showFilters,
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = searchVisible && showFilters,
                                enter = expandVertically(animationSpec = tween(durationMillis = 220, easing = LinearEasing)) + fadeIn(tween(durationMillis = 220, easing = LinearEasing)),
                                exit = shrinkVertically(animationSpec = tween(durationMillis = 220, easing = LinearEasing)) + fadeOut(tween(durationMillis = 220, easing = LinearEasing)),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Label for order mode
                                    Text(
                                        text = stringResource(Res.string.order_by),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(
                                            horizontal = tokens.spacing.lg,
                                            vertical = tokens.spacing.xs,
                                        ),
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(
                                            horizontal = tokens.spacing.lg,
                                            vertical = tokens.spacing.xs,
                                        ),
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
                                            modifier = Modifier.padding(end = tokens.spacing.sm),
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

                    if (searchVisible) {
                        item(key = "list_header_spacing") {
                            Spacer(modifier = Modifier.height(tokens.spacing.md))
                        }
                    }

                    // If search is active and nothing matches, show a search-specific message.
                    if (searchQuery.isNotBlank() && notes.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(
                                    horizontal = tokens.spacing.xl,
                                    vertical = tokens.spacing.lg,
                                ),
                                verticalArrangement = Center,
                                horizontalAlignment = CenterHorizontally,
                            ) {
                                Text(
                                    text = stringResource(Res.string.no_notes_match_search),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.colors.onSurface,
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
                            Text(
                                text = when (section) {
                                    Bucket.TODAY -> stringResource(Res.string.today)
                                    Bucket.THIS_WEEK -> stringResource(Res.string.this_week)
                                    Bucket.THIS_MONTH -> stringResource(Res.string.this_month)
                                    Bucket.EARLIER -> stringResource(Res.string.earlier)
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = tokens.colors.onSurface,
                                modifier = Modifier.padding(
                                    horizontal = tokens.spacing.xl,
                                    vertical = tokens.spacing.sm,
                                ),
                            )
                        }
                        items(itemsInGroup, key = { it.id }) { note ->
                            NoteRow(
                                note = note,
                                onClick = onNoteClick,
                                showUpdated = useUpdated.value,
                                modifier = Modifier.padding(
                                    horizontal = tokens.spacing.lg,
                                    vertical = tokens.spacing.sm,
                                ).animateItem(),
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(tokens.spacing.xxxl))
                    }
                }
            }
        }
    }

    ContentScaffold()
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "List")
@DevicePreviews
@Composable
internal fun ListScreenPreview(
    @PreviewParameter(ListScreenPreviewProvider::class) state: ListPreviewState,
) {
    DevicePreviewContainer(
        isDarkTheme = state.isDarkTheme,
        localized = state.localized,
    ) {
        ListScreen(
            notes = state.notes,
            onNoteClick = {},
            onAddClick = {},
            searchQuery = state.searchQuery,
            onSearchQueryChange = {},
            hasAnyNotes = state.hasAnyNotes,
            showTopAppBar = true,
        )
    }
}

internal data class ListPreviewState(
    val notes: List<Note>,
    val searchQuery: String = "",
    val hasAnyNotes: Boolean = true,
    val isDarkTheme: Boolean = false,
    val localized: Boolean = false,
)

internal object ListPreviewSamples {
    private val sampleNotes: List<Note> = List(10) { index ->
        Note(
            id = index + 1,
            title = "Note #${index + 1}",
            description = "This is a sample note to preview different layouts and sizes.",
            deleted = false,
            createdAt = 1_700_000_000_000L + index * 3_600_000L,
            updatedAt = 1_700_000_000_000L + index * 3_600_000L,
        )
    }

    val populated = ListPreviewState(notes = sampleNotes)
    val empty = ListPreviewState(
        notes = emptyList(),
        hasAnyNotes = false,
    )
    val dark = ListPreviewState(
        notes = sampleNotes,
        isDarkTheme = true,
        localized = true,
    )

    val states: List<ListPreviewState> = listOf(populated, empty, dark)
}

internal class ListScreenPreviewProvider : PreviewParameterProvider<ListPreviewState> {
    override val values: Sequence<ListPreviewState> = ListPreviewSamples.states.asSequence()
}
