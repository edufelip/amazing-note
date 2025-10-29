package com.edufelip.shared.ui.components.molecules.notes

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.edufelip.shared.preview.Preview
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.folder_note_count_one
import com.edufelip.shared.resources.folder_note_count_other
import org.jetbrains.compose.resources.stringResource

@Composable
fun folderCountLabel(count: Int): String = if (count == 1) {
    stringResource(Res.string.folder_note_count_one)
} else {
    stringResource(Res.string.folder_note_count_other, count)
}

@Preview
@Composable
private fun FolderCountLabelPreview() {
    Text(text = folderCountLabel(count = 3))
}
