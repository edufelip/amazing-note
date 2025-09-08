package com.edufelip.amazing_note.di

import android.content.Context
import com.edufelip.shared.data.SqlDelightNoteRepository
import com.edufelip.shared.db.AndroidContextHolder
import com.edufelip.shared.db.DatabaseDriverFactory
import com.edufelip.shared.db.createDatabase
import com.edufelip.shared.domain.repository.NoteRepository
import com.edufelip.shared.domain.usecase.NoteUseCases
import com.edufelip.shared.domain.usecase.buildNoteUseCases
import com.edufelip.shared.domain.validation.NoteValidationRules
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
    fun provideSharedNoteRepository(
        @ApplicationContext context: Context
    ): NoteRepository {
        AndroidContextHolder.appContext = context.applicationContext
        val db = createDatabase(DatabaseDriverFactory())
        return SqlDelightNoteRepository(db)
    }

    @Singleton
    @Provides
    fun provideNoteUseCases(repository: NoteRepository): NoteUseCases =
        buildNoteUseCases(repository, NoteValidationRules())
}
