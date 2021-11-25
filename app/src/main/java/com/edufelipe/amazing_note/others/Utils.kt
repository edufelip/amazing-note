package com.edufelipe.amazing_note.others
import android.content.res.Resources
import com.edufelipe.amazing_note.R
import com.edufelipe.amazing_note.data.models.Priority

fun checkEmptyInput(vararg inputs: String): Boolean {
    for(input in inputs) {
        if (input.isEmpty()) return true;
    }
    return false
}

fun checkTooLong(input: String, limit: Int): Boolean {
    return input.length > limit
}