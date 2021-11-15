package com.example.amazing_note.others
import com.example.amazing_note.data.models.Priority

fun checkEmptyInput(vararg inputs: String): Boolean {
    for(input in inputs) {
        if (input.isEmpty()) return true;
    }
    return false
}

fun checkTooLong(input: String, limit: Int): Boolean {
    return input.length > limit
}

fun parsePriority(priority: String): Priority {
    return when(priority) {
        "High Priority" -> {
            Priority.HIGH}
        "Medium Priority" -> {
            Priority.MEDIUM}
        "Low Priority" -> {
            Priority.LOW}
        else -> Priority.LOW
    }
}