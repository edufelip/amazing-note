package com.example.amazing_note.di

import android.content.Context
import androidx.room.Room
import com.example.amazing_note.data.db.NoteDao
import com.example.amazing_note.data.db.NoteDatabase
import com.example.amazing_note.data.repositories.NoteRepository
import com.example.amazing_note.others.Constants.DATABASE_NAME
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