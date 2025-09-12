package com.edufelip.amazing_note.others

import com.edufelip.shared.model.Priority
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PrioritySerializationTest {
    @Test
    fun toString_returnsSerializedValue() {
        assertThat(Priority.HIGH.toString()).isEqualTo("high")
        assertThat(Priority.MEDIUM.toString()).isEqualTo("medium")
        assertThat(Priority.LOW.toString()).isEqualTo("low")
    }

    @Test
    fun fromString_parsesCaseInsensitively() {
        assertThat(Priority.fromString("HIGH")).isEqualTo(Priority.HIGH)
        assertThat(Priority.fromString("Medium")).isEqualTo(Priority.MEDIUM)
        assertThat(Priority.fromString("low")).isEqualTo(Priority.LOW)
    }

    @Test
    fun fromString_unknownReturnsNull() {
        assertThat(Priority.fromString("all")).isNull()
        assertThat(Priority.fromString("unknown")).isNull()
        assertThat(Priority.fromString(null)).isNull()
    }
}
