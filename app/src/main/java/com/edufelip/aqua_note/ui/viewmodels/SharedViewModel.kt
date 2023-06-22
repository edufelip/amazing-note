package com.edufelip.aqua_note.ui.viewmodels

import android.app.Application
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.edufelip.aqua_note.R
import com.edufelip.aqua_note.data.models.Priority

class SharedViewModel(application: Application): AndroidViewModel(application) {
    val listener: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            when(position) {
                0 -> {
                    (parent?.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(application, R.color.red))
                }
                1 -> {
                    (parent?.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(application, R.color.yellow))
                }
                2 -> {
                    (parent?.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(application, R.color.green))
                }
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
        }
    }

    fun parsePriority(priority: String): Priority {
        return when(priority) {
            getApplication<Application>().resources.getString(R.string.high_priority) -> {
                Priority.HIGH}
            getApplication<Application>().resources.getString(R.string.medium_priority) -> {
                Priority.MEDIUM}
            getApplication<Application>().resources.getString(R.string.low_priority) -> {
                Priority.LOW}
            else -> Priority.LOW
        }
    }
}