package com.edufelip.aqua_note.others

fun checkEmptyInput(vararg inputs: String): Boolean {
    for(input in inputs) {
        if (input.isEmpty()) return true
    }
    return false
}

fun checkTooLong(input: String, limit: Int): Boolean {
    return input.length > limit
}