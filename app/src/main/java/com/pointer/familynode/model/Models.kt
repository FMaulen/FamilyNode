package com.pointer.familynode.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

enum class NoteItemType {
    TEXT, CHECKLIST, IMAGE
}

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Color
)

data class FocusRequest(val itemId: String, val cursorPosition: Int)

data class TextSpan(
    val start: Int,
    val end: Int,
    val format: TextFormat
)

data class NoteItem(
    val id: String = UUID.randomUUID().toString(),
    val type: NoteItemType,
    val text: String? = null,
    val imageUri: String? = null,
    val isChecked: Boolean = false,
    val authorId: String,
    val textSpans: List<TextSpan> = emptyList(),
    val formats: Set<TextFormat> = setOf(TextFormat.BODY)
)

enum class TextFormat {
    TITLE, HEADING, SUBHEADING, BODY, BOLD, ITALIC, UNDERLINE, STRIKETHROUGH
}

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val items: List<NoteItem>,
    val authorId: String
)

data class Group(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: ImageVector,
    val members: List<User> = emptyList(),
    val notes: List<Note> = emptyList()
)