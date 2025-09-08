package com.edufelip.amazing_note.di

import android.content.Context
import com.edufelip.shared.data.NoteRepository
import com.edufelip.shared.data.SqlDelightNoteRepository
import com.edufelip.shared.db.DatabaseDriverFactory
import com.edufelip.shared.db.createDatabase
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
        com.edufelip.shared.db.AndroidContextHolder.appContext = context.applicationContext
        val db = createDatabase(DatabaseDriverFactory())
        return SqlDelightNoteRepository(db)
    }
}
