package com.edufelipe.amazing_note.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.edufelipe.amazing_note.data.repositories.FakeNoteRepositoryAndroidTest
import com.edufelipe.amazing_note.ui.adapters.ListAdapter
import com.edufelipe.amazing_note.ui.adapters.TrashAdapter
import com.edufelipe.amazing_note.ui.fragments.*
import com.edufelipe.amazing_note.ui.viewmodels.NoteViewModel
import com.edufelipe.amazing_note.ui.viewmodels.TrashViewModel
import javax.inject.Inject

class TestMainFragmentFactory @Inject constructor(
    private val listAdapter: ListAdapter,
    private val trashAdapter: TrashAdapter
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className) {
            ListFragment::class.java.name -> {
                ListFragment(listAdapter, NoteViewModel(FakeNoteRepositoryAndroidTest()))
            }
            AddFragment::class.java.name -> {
                AddFragment(listAdapter, NoteViewModel(FakeNoteRepositoryAndroidTest()))
            }
            TrashFragment::class.java.name -> {
                TrashFragment(trashAdapter, TrashViewModel(FakeNoteRepositoryAndroidTest()))
            }
            TrashNoteFragment::class.java.name -> {
                TrashNoteFragment(listAdapter, TrashViewModel(FakeNoteRepositoryAndroidTest()))
            }
            UpdateFragment::class.java.name -> {
                UpdateFragment(listAdapter, NoteViewModel(FakeNoteRepositoryAndroidTest()))
            }
            else -> super.instantiate(classLoader, className)
        }
    }

}