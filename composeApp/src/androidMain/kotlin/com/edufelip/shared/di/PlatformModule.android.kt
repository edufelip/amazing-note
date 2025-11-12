package com.edufelip.shared.di

import com.edufelip.shared.data.auth.AuthService
import com.edufelip.shared.data.auth.GitLiveAuthService
import com.edufelip.shared.data.db.AndroidContextHolder
import com.edufelip.shared.data.db.DatabaseDriverFactory
import com.edufelip.shared.data.db.createDatabase
import com.edufelip.shared.data.repository.SqlDelightNoteRepository
import com.edufelip.shared.domain.repository.NoteRepository
import com.edufelip.shared.ui.settings.AndroidSettings
import com.edufelip.shared.ui.settings.AppPreferences
import com.edufelip.shared.ui.settings.DefaultAppPreferences
import com.edufelip.shared.ui.settings.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single {
        AndroidContextHolder.appContext = androidContext().applicationContext
        createDatabase(DatabaseDriverFactory())
    }
    single<AuthService> { GitLiveAuthService() }
    single<NoteRepository> { SqlDelightNoteRepository(get()) }
    single<Settings> { AndroidSettings(androidContext()) }
    single<AppPreferences> { DefaultAppPreferences(get()) }
}
