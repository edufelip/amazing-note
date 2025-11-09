package com.edufelip.shared.ui.app.state

import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import com.edufelip.shared.data.auth.AuthUser
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.domain.repository.AuthRepository
import com.edufelip.shared.domain.usecase.buildAuthUseCases
import com.edufelip.shared.ui.app.core.AmazingNoteAppEnvironment
import com.edufelip.shared.ui.components.organisms.notes.FolderLayout
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.settings.AppPreferences
import com.edufelip.shared.ui.settings.InMemorySettings
import com.edufelip.shared.ui.settings.Settings
import com.edufelip.shared.ui.vm.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AmazingNoteAppStateTest {

    @Test
    fun navigateAddsDestination() {
        val state = createState(initialRoute = AppRoutes.Notes)

        state.navigate(AppRoutes.Folders)

        assertEquals(listOf(AppRoutes.Notes, AppRoutes.Folders), state.backStack.toList())
    }

    @Test
    fun popBackRemovesLastRoute() {
        val state = createState(initialRoute = AppRoutes.Notes)
        state.navigate(AppRoutes.Folders)

        val popped = state.popBack()

        assertTrue(popped)
        assertEquals(listOf(AppRoutes.Notes), state.backStack.toList())
    }

    @Test
    fun setRootClearsBackStack() {
        val state = createState(initialRoute = AppRoutes.Notes)
        state.navigate(AppRoutes.Folders)
        state.navigate(AppRoutes.Settings)

        state.setRoot(AppRoutes.Settings)

        assertEquals(listOf(AppRoutes.Settings), state.backStack.toList())
    }

    @Test
    fun toggleThemePersistsPreference() {
        val settings = InMemorySettings()
        val preferences = TestAppPreferences(settings)
        val state = createState(
            settings = settings,
            appPreferences = preferences,
        )

        state.toggleTheme(false)

        assertFalse(state.darkTheme)
        assertFalse(preferences.isDarkTheme())

        state.toggleTheme()

        assertTrue(state.darkTheme)
        assertTrue(preferences.isDarkTheme())
    }

    private fun createState(
        initialRoute: AppRoutes = AppRoutes.Notes,
        showBottomBar: Boolean = true,
        settings: Settings = InMemorySettings(),
        appPreferences: AppPreferences = TestAppPreferences(settings),
    ): AmazingNoteAppState {
        val authRepository = TestAuthRepository()
        val authUseCases = buildAuthUseCases(authRepository)
        val driver = NoOpSqlDriver
        val noteDatabase = NoteDatabase(driver)
        val syncManager = NotesSyncManager(
            db = noteDatabase,
            scope = CoroutineScope(SupervisorJob()),
            cloud = TestCloudNotesDataSource,
            currentUser = TestCurrentUserProvider,
        )
        val environment = AmazingNoteAppEnvironment(
            settings = settings,
            appPreferences = appPreferences,
            authRepository = authRepository,
            authUseCases = authUseCases,
            noteDatabase = noteDatabase,
            notesSyncManager = syncManager,
            attachmentPicker = null,
            googleSignInLauncher = null,
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val authViewModel = AuthViewModel(authUseCases, scope)
        return AmazingNoteAppState(
            environment = environment,
            initialRoute = initialRoute,
            showBottomBar = showBottomBar,
            coroutineScope = scope,
            authViewModel = authViewModel,
        )
    }

    private class TestAuthRepository : AuthRepository {
        private val userState = MutableStateFlow<AuthUser?>(null)
        override val currentUser: Flow<AuthUser?> = userState

        override suspend fun signInWithEmailPassword(email: String, password: String) {}

        override suspend fun signUpWithEmailPassword(email: String, password: String) {}

        override suspend fun sendPasswordResetEmail(email: String) {}

        override suspend fun signInWithGoogle(idToken: String) {}

        override suspend fun signOut() {}
    }

    private class TestAppPreferences(private val settings: Settings) : AppPreferences {
        private val state = MutableStateFlow(settings.getBool(KEY, true))
        private val layoutState = MutableStateFlow(FolderLayout.Grid)

        override fun isDarkTheme(): Boolean = state.value

        override fun setDarkTheme(enabled: Boolean) {
            if (state.value != enabled) {
                state.value = enabled
            }
            settings.setBool(KEY, enabled)
        }

        override val darkThemeFlow = state

        override fun isDateModeUpdated(): Boolean = true

        override fun setDateModeUpdated(updated: Boolean) {}

        override fun folderLayout(): FolderLayout = layoutState.value

        override fun setFolderLayout(layout: FolderLayout) {
            layoutState.value = layout
        }

        override val folderLayoutFlow: StateFlow<FolderLayout> = layoutState

        companion object {
            private const val KEY = "dark_theme"
        }
    }

    private object TestCloudNotesDataSource : com.edufelip.shared.data.cloud.CloudNotesDataSource {
        override fun observe(uid: String) = emptyFlow<List<com.edufelip.shared.domain.model.Note>>()
        override suspend fun getAll(uid: String) = emptyList<com.edufelip.shared.domain.model.Note>()
        override suspend fun upsert(uid: String, note: com.edufelip.shared.domain.model.Note) {}
        override suspend fun delete(uid: String, id: Int) {}
        override suspend fun upsertPreserveUpdatedAt(uid: String, note: com.edufelip.shared.domain.model.Note) {}
    }

    private object TestCurrentUserProvider : com.edufelip.shared.data.cloud.CurrentUserProvider {
        override val uid: Flow<String?> = MutableStateFlow(null)
    }

    private object EmptySqlCursor : SqlCursor {
        override fun next(): QueryResult<Boolean> = QueryResult.Value(false)
        override fun getString(index: Int): String? = null
        override fun getLong(index: Int): Long? = null
        override fun getBytes(index: Int): ByteArray? = null
        override fun getDouble(index: Int): Double? = null
        override fun getBoolean(index: Int): Boolean? = null
    }

    private object EmptyPreparedStatement : SqlPreparedStatement {
        override fun bindBytes(index: Int, bytes: ByteArray?) {}
        override fun bindLong(index: Int, long: Long?) {}
        override fun bindDouble(index: Int, double: Double?) {}
        override fun bindString(index: Int, string: String?) {}
        override fun bindBoolean(index: Int, boolean: Boolean?) {}
    }

    private object NoOpSqlDriver : SqlDriver {
        override fun <R> executeQuery(
            identifier: Int?,
            sql: String,
            mapper: (SqlCursor) -> QueryResult<R>,
            parameters: Int,
            binders: (SqlPreparedStatement.() -> Unit)?,
        ): QueryResult<R> {
            binders?.invoke(EmptyPreparedStatement)
            return mapper(EmptySqlCursor)
        }

        override fun execute(
            identifier: Int?,
            sql: String,
            parameters: Int,
            binders: (SqlPreparedStatement.() -> Unit)?,
        ): QueryResult<Long> {
            binders?.invoke(EmptyPreparedStatement)
            return QueryResult.Value(0L)
        }

        override fun newTransaction(): QueryResult<Transacter.Transaction> = QueryResult.Value(object : Transacter.Transaction() {
            override val enclosingTransaction: Transacter.Transaction? = null
            override fun endTransaction(successful: Boolean): QueryResult<Unit> = QueryResult.Value(Unit)
        })

        override fun currentTransaction(): Transacter.Transaction? = null

        override fun addListener(vararg queryKeys: String, listener: Query.Listener) {}

        override fun removeListener(vararg queryKeys: String, listener: Query.Listener) {}

        override fun notifyListeners(vararg queryKeys: String) {}

        override fun close() {}
    }
}
