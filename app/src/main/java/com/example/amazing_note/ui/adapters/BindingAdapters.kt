package com.example.amazing_note.ui.adapters

import android.widget.Spinner
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import com.example.amazing_note.R
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.data.models.Priority
import com.example.amazing_note.ui.fragments.ListFragmentDirections
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
    fun parsePriorityColor(cardView: CardView, priority: Priority) {
       when(priority) {
            Priority.HIGH -> { cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, R.color.red)) }
            Priority.MEDIUM -> { cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, R.color.yellow)) }
            Priority.LOW -> { cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, R.color.green)) }
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
}