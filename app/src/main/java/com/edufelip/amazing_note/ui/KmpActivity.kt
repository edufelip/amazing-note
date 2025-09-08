package com.edufelip.amazing_note.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.edufelip.shared.data.NoteRepository
import com.edufelip.shared.ui.AmazingNoteApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class KmpActivity : ComponentActivity() {

    @Inject
    lateinit var noteRepository: NoteRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AmazingNoteApp(noteRepository = noteRepository)
        }
    }
}
