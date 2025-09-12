package com.edufelip.shared.model

import com.edufelip.shared.util.StringEnum
import com.edufelip.shared.util.fromString

enum class Priority(override val serialized: String) : StringEnum {
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low"),
    ;

    override fun toString(): String = serialized

    companion object {
        fun fromString(value: String?): Priority? = if (value == null) null else fromString<Priority>(value)
    }
}
