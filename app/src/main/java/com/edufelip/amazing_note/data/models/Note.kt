package com.edufelip.amazing_note.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Note (
    var id: Int,
    var title: String,
    var priority: Priority,
    var description: String,
    var deleted: Boolean
): Parcelable
