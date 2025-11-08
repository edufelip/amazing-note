package com.edufelip.shared.di

import com.edufelip.shared.data.db.DatabaseDriverFactory
import com.edufelip.shared.data.db.createDatabase
import com.edufelip.shared.data.repository.SqlDelightNoteRepository
import com.edufelip.shared.domain.repository.NoteRepository
import com.edufelip.shared.ui.settings.AppPreferences
import com.edufelip.shared.ui.settings.DefaultAppPreferences
import com.edufelip.shared.ui.settings.IosSettings
import com.edufelip.shared.ui.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { createDatabase(DatabaseDriverFactory()) }
    single<NoteRepository> { SqlDelightNoteRepository(get()) }
    single<Settings> { IosSettings }
    single<AppPreferences> { DefaultAppPreferences(get()) }
}
