package com.edufelip.aqua_note.ui.adapters

import android.widget.LinearLayout
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import com.edufelip.aqua_note.R
import com.edufelip.aqua_note.data.models.Note
import com.edufelip.aqua_note.data.models.Priority
import com.edufelip.aqua_note.ui.fragments.ListFragmentDirections
import com.edufelip.aqua_note.ui.fragments.TrashFragmentDirections
import com.google.android.material.floatingactionbutton.FloatingActionButton

object BindingAdapters {
    @BindingAdapter("android:navigateToAddFragment")
    @JvmStatic
    fun navigateToAddFragment(view: FloatingActionButton, navigate: Boolean) {
        view.setOnClickListener {
            if(navigate) {
                view.findNavController().navigate(ListFragmentDirections.actionListFragmentToAddFragment())
            }
        }
    }

    @BindingAdapter("android:parsePriorityToInt")
    @JvmStatic
    fun parsePriorityToInt(view: Spinner, priority: Priority) {
       when(priority) {
            Priority.HIGH -> {view.setSelection(0)}
            Priority.MEDIUM -> {view.setSelection(1)}
            Priority.LOW -> {view.setSelection(2)}
        }
    }

    @BindingAdapter("android:parsePriorityColor")
    @JvmStatic
    fun parsePriorityColor(statusBar: LinearLayout, priority: Priority) {
       when(priority) {
            Priority.HIGH -> { statusBar.background.setTint(ContextCompat.getColor(statusBar.context, R.color.red)) }
            Priority.MEDIUM -> { statusBar.background.setTint(ContextCompat.getColor(statusBar.context, R.color.yellow)) }
            Priority.LOW -> { statusBar.background.setTint(ContextCompat.getColor(statusBar.context, R.color.green)) }
        }
    }

    @BindingAdapter("android:sendDataToUpdateFragment")
    @JvmStatic
    fun sendDataToUpdateFragment(view: ConstraintLayout, currentNote: Note) {
        view.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(currentNote)
            view.findNavController().navigate(action)
        }
    }

    @BindingAdapter("android:sendDataToTrashNoteFragment")
    @JvmStatic
    fun sendDataToTrashNoteFragment(view: ConstraintLayout, currentNote: Note) {
        view.setOnClickListener {
            val action = TrashFragmentDirections.actionTrashFragmentToTrashNoteFragment(currentNote)
            view.findNavController().navigate(action)
        }
    }
}