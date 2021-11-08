package com.example.amazing_note.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.example.amazing_note.data.repositories.FakeNoteRepositoryAndroidTest
import com.example.amazing_note.ui.adapters.ListAdapter
import com.example.amazing_note.ui.adapters.TrashAdapter
import com.example.amazing_note.ui.fragments.ListFragment
import com.example.amazing_note.ui.fragments.TrashFragment
import com.example.amazing_note.ui.viewmodels.NoteViewModel
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
            TrashFragment::class.java.name -> {
                TrashFragment(trashAdapter)
            }
            else -> super.instantiate(classLoader, className)
        }
    }

}