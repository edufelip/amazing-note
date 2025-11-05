package com.edufelip.shared.ui.editor

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Smoke test that prevents regressions: on Apple platforms the simple editor must be selected.
 */
class EditorSelectorTest {
    @Test
    fun `ios uses simple editor`() {
        assertTrue(
            shouldUseSimpleEditor(),
            message = "iOS must use SimpleIosNoteEditor to avoid the crashy rich editor path"
        )
    }
}
