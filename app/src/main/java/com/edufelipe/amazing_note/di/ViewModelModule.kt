package com.edufelipe.amazing_note.di

import com.edufelipe.amazing_note.data.db.NoteDao
import com.edufelipe.amazing_note.data.repositories.INoteRepository
import com.edufelipe.amazing_note.data.repositories.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    @Provides
    @ViewModelScoped
    fun provideNoteRepository(
        dao: NoteDao
    ): INoteRepository = NoteRepository(dao)
}