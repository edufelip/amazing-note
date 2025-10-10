package com.edufelip.shared.model

import com.edufelip.shared.util.StringEnum
import com.edufelip.shared.util.fromString

enum class Priority(override val serialized: String, val value: Int) : StringEnum {
    HIGH("high", 2),
    MEDIUM("medium", 1),
    LOW("low", 0),
    ;

    override fun toString(): String = serialized

    companion object {
        fun fromString(value: String?): Priority? = if (value == null) null else fromString<Priority>(value)
    }
}
