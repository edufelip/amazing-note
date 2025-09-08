package com.edufelip.amazing_note.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.edufelip.amazing_note.ui.viewmodels.KmpNoteViewModel
import com.edufelip.shared.i18n.ProvideAndroidStrings
import com.edufelip.shared.ui.AmazingNoteApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: KmpNoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProvideAndroidStrings {
                AmazingNoteApp(viewModel = vm)
            }
        }
    }
}
