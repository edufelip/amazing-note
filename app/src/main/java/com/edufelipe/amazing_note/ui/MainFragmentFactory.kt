package com.edufelipe.amazing_note.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.edufelipe.amazing_note.ui.adapters.ListAdapter
import com.edufelipe.amazing_note.ui.adapters.TrashAdapter
import com.edufelipe.amazing_note.ui.fragments.*
import javax.inject.Inject

class MainFragmentFactory @Inject constructor(
    private val listAdapter: ListAdapter,
    private val trashAdapter: TrashAdapter
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className) {
            ListFragment::class.java.name -> {
                ListFragment(listAdapter)
            }
            TrashFragment::class.java.name -> {
                TrashFragment(trashAdapter)
            }
            TrashNoteFragment::class.java.name -> {
                TrashNoteFragment(listAdapter)
            }
            UpdateFragment::class.java.name -> {
                UpdateFragment(listAdapter)
            }
            AddFragment::class.java.name -> {
                AddFragment(listAdapter)
            }
            else -> super.instantiate(classLoader, className)
        }
    }
}