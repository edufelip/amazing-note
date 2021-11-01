package com.example.amazing_note.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.example.amazing_note.ui.adapters.ListAdapter
import com.example.amazing_note.ui.adapters.TrashAdapter
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
            else -> super.instantiate(classLoader, className)
        }
    }
}