package com.edufelip.amazing_note.di

import android.content.Context
import com.edufelip.amazing_note.others.AndroidSettings
import com.edufelip.shared.data.SqlDelightNoteRepository
import com.edufelip.shared.db.AndroidContextHolder
import com.edufelip.shared.db.DatabaseDriverFactory
import com.edufelip.shared.db.createDatabase
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.domain.repository.NoteRepository
import com.edufelip.shared.domain.usecase.NoteUseCases
import com.edufelip.shared.domain.usecase.buildNoteUseCases
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.ui.settings.AppPreferences
import com.edufelip.shared.ui.settings.DefaultAppPreferences
import com.edufelip.shared.ui.settings.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideNoteDatabase(
        @ApplicationContext context: Context,
    ): NoteDatabase {
        AndroidContextHolder.appContext = context.applicationContext
        return createDatabase(DatabaseDriverFactory())
    }

    @Singleton
    @Provides
    fun provideSharedNoteRepository(
        db: NoteDatabase,
    ): NoteRepository = SqlDelightNoteRepository(db)

    @Singleton
    @Provides
    fun provideNoteUseCases(repository: NoteRepository): NoteUseCases = buildNoteUseCases(repository, NoteValidationRules())

    @Singleton
    @Provides
    fun provideSettings(
        @ApplicationContext context: Context,
    ): Settings = AndroidSettings(context)

    @Singleton
    @Provides
    fun provideAppPreferences(
        settings: Settings,
    ): AppPreferences = DefaultAppPreferences(settings)
}
