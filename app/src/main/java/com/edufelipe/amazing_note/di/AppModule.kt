package com.edufelipe.amazing_note.di

import android.content.Context
import androidx.room.Room
import com.edufelipe.amazing_note.data.db.NoteDatabase
import com.edufelipe.amazing_note.others.Constants.DATABASE_NAME
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
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, NoteDatabase::class.java, DATABASE_NAME).build()

    @Singleton
    @Provides
    fun provideNoteDao(
        database: NoteDatabase
    ) = database.noteDao()
}