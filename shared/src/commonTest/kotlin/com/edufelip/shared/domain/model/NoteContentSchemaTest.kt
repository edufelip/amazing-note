package com.edufelip.shared.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NoteContentSchemaTest {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    @Test
    fun toJsonIncludesSchemaVersion() {
        val content = NoteContent(blocks = listOf(TextBlock(text = "Hello")))
        val element = jsonParser.parseToJsonElement(content.toJson()).jsonObject
        val schemaValue = element["schemaVersion"]?.jsonPrimitive?.content?.toIntOrNull()
        assertNotNull(schemaValue)
        assertEquals(NOTE_CONTENT_SCHEMA_VERSION, schemaValue)
    }

    @Test
    fun fromJsonMissingSchemaVersionUsesLegacyVersion() {
        val legacyJson = """{"blocks":[{"type":"text","id":"legacy-1","text":"Hi","spans":[]}]}"""
        val content = noteContentFromJson(legacyJson)
        assertEquals(LEGACY_NOTE_CONTENT_SCHEMA_VERSION, content.schemaVersion)
        assertEquals(1, content.blocks.size)
        assertTrue(content.blocks.first() is TextBlock)
        assertEquals("Hi", (content.blocks.first() as TextBlock).text)
    }

    @Test
    fun migrateToLatestSchemaUpdatesVersion() {
        val legacy = NoteContent(
            schemaVersion = LEGACY_NOTE_CONTENT_SCHEMA_VERSION,
            blocks = listOf(TextBlock(text = "Legacy")),
        )
        val migrated = legacy.migrateToLatestSchema()
        assertEquals(NOTE_CONTENT_SCHEMA_VERSION, migrated.schemaVersion)
        assertEquals("Legacy", (migrated.blocks.first() as TextBlock).text)
    }

    @Test
    fun normalizedForEditorEnsuresTrailingTextBlock() {
        val content = NoteContent(blocks = listOf(ImageBlock(localUri = "file://image")))
        val normalized = content.normalizedForEditor()
        assertTrue(normalized.blocks.last() is TextBlock)
        assertEquals(2, normalized.blocks.size)
    }
}
