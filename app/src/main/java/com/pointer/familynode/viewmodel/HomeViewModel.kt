package com.pointer.familynode.viewmodel

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.lifecycle.ViewModel
import com.pointer.familynode.model.*
import com.pointer.familynode.ui.theme.AvatarGreen
import com.pointer.familynode.ui.theme.AvatarPurple
import com.pointer.familynode.ui.theme.AvatarYellow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import kotlin.math.max
import kotlin.math.min
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointer.familynode.data.SettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class AppUiState(
    val groups: List<Group> = emptyList(),
    val selectedGroupId: String? = null,
    val focusRequest: FocusRequest? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()
    private val settingsRepository = SettingsRepository(application)


    init {
        loadSampleData()

        viewModelScope.launch {
            val savedGroupId = settingsRepository.selectedGroupIdFlow.firstOrNull()
            if (savedGroupId != null) {
                _uiState.update { it.copy(selectedGroupId = savedGroupId) }
            }
        }
    }

    private fun updateNoteInState(noteId: String, updateAction: (Note) -> Note) {
        _uiState.update { currentState ->
            val newGroups = currentState.groups.map { group ->
                if (group.id == currentState.selectedGroupId) {
                    val newNotes = group.notes.map { note ->
                        if (note.id == noteId) updateAction(note) else note
                    }
                    group.copy(notes = newNotes)
                } else {
                    group
                }
            }
            currentState.copy(groups = newGroups)
        }
    }

    fun updateNoteTitle(noteId: String, newTitle: String) {
        updateNoteInState(noteId) { it.copy(title = newTitle) }
    }

    fun updateNoteItemText(noteId: String, itemId: String, newText: String) {
        updateNoteInState(noteId) { note ->
            note.copy(items = note.items.map { item ->
                if (item.id == itemId) {
                    val adjustedSpans = item.textSpans.mapNotNull { span ->
                        val newEnd = span.end.coerceAtMost(newText.length)
                        if (span.start < newEnd) span.copy(end = newEnd) else null
                    }
                    item.copy(text = newText, textSpans = adjustedSpans)
                } else item
            })
        }
    }

    fun toggleNoteItemChecked(noteId: String, itemId: String) {
        updateNoteInState(noteId) { note ->
            note.copy(items = note.items.map {
                if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it
            })
        }
    }

    fun addImageItemAfter(noteId: String, currentItemId: String, imageUri: Uri) {
        val currentUser = _uiState.value.groups
            .find { it.id == _uiState.value.selectedGroupId }
            ?.members?.firstOrNull() ?: return

        val newItem = NoteItem(
            type = NoteItemType.IMAGE,
            imageUri = imageUri.toString(),
            authorId = currentUser.id
        )

        updateNoteInState(noteId) { note ->
            val newItems = mutableListOf<NoteItem>()
            note.items.forEach { item ->
                newItems.add(item)
                if (item.id == currentItemId) {
                    newItems.add(newItem)
                }
            }
            note.copy(items = newItems)
        }
    }

    fun splitNoteItem(noteId: String, itemId: String, textBefore: String, textAfter: String) {
        val currentUser = uiState.value.groups.find { it.id == uiState.value.selectedGroupId }?.members?.firstOrNull() ?: return
        val newItem = NoteItem(type = NoteItemType.TEXT, text = textAfter, authorId = currentUser.id)
        updateNoteInState(noteId) { note ->
            val item = note.items.find { it.id == itemId }
            val newItems = mutableListOf<NoteItem>()
            note.items.forEach { currentItem ->
                if (currentItem.id == itemId && item != null) {
                    val spansForBefore = mutableListOf<TextSpan>()
                    val spansForAfter = mutableListOf<TextSpan>()
                    val cursorPos = textBefore.length
                    item.textSpans.forEach { span ->
                        when {
                            span.end <= cursorPos -> spansForBefore.add(span)
                            span.start >= cursorPos -> spansForAfter.add(span.copy(start = span.start - cursorPos, end = span.end - cursorPos))
                            else -> {
                                spansForBefore.add(span.copy(end = cursorPos))
                                spansForAfter.add(span.copy(start = 0, end = span.end - cursorPos))
                            }
                        }
                    }
                    newItems.add(item.copy(text = textBefore, textSpans = spansForBefore))
                    newItems.add(newItem.copy(textSpans = spansForAfter))
                } else {
                    newItems.add(currentItem)
                }
            }
            note.copy(items = newItems)
        }
        _uiState.update { it.copy(focusRequest = FocusRequest(newItem.id, 0)) }
    }

    fun mergeNoteItems(noteId: String, fromItemId: String, toItemId: String) {
        var cursorPosition = -1
        updateNoteInState(noteId) { note ->
            val fromItem = note.items.find { it.id == fromItemId }
            val toItem = note.items.find { it.id == toItemId }
            if (fromItem != null && toItem != null) {
                cursorPosition = (toItem.text ?: "").length
                val mergedText = (toItem.text ?: "") + (fromItem.text ?: "")
                val mergedSpans = toItem.textSpans.toMutableList()
                fromItem.textSpans.forEach { span ->
                    mergedSpans.add(span.copy(start = span.start + cursorPosition, end = span.end + cursorPosition))
                }
                val updatedToItem = toItem.copy(text = mergedText, textSpans = mergedSpans)
                val newItems = note.items.map { if (it.id == toItemId) updatedToItem else it }.filterNot { it.id == fromItemId }
                note.copy(items = newItems)
            } else {
                note
            }
        }
        if (cursorPosition != -1) {
            _uiState.update { it.copy(focusRequest = FocusRequest(toItemId, cursorPosition)) }
        }
    }

    fun addChecklistItemAfter(noteId: String, currentItemId: String) {
        val currentUser = uiState.value.groups.find { it.id == uiState.value.selectedGroupId }?.members?.firstOrNull() ?: return
        val newItem = NoteItem(type = NoteItemType.CHECKLIST, text = "", authorId = currentUser.id)
        updateNoteInState(noteId) { note ->
            val newItems = mutableListOf<NoteItem>()
            note.items.forEach { item ->
                newItems.add(item)
                if (item.id == currentItemId) {
                    newItems.add(newItem)
                }
            }
            note.copy(items = newItems)
        }
        _uiState.update { it.copy(focusRequest = FocusRequest(newItem.id, 0)) }
    }

    fun consumeFocusRequest() {
        _uiState.update { it.copy(focusRequest = null) }
    }

    fun applyFormatToRange(noteId: String, itemId: String, format: TextFormat, start: Int, end: Int) {
        updateNoteInState(noteId) { note ->
            note.copy(items = note.items.map { item ->
                if (item.id == itemId) {
                    val newSpans = mutableListOf<TextSpan>()
                    val spansInSelection = item.textSpans.filter { it.format == format && it.start < end && it.end > start }
                    var coveredRange = 0
                    spansInSelection.forEach { span ->
                        val overlapStart = max(span.start, start)
                        val overlapEnd = min(span.end, end)
                        coveredRange += (overlapEnd - overlapStart)
                    }
                    val isFullyCovered = coveredRange >= (end - start)
                    if (isFullyCovered) {
                        item.textSpans.forEach { span ->
                            if (span.format == format && span.start < end && span.end > start) {
                                if (span.start < start) newSpans.add(span.copy(end = start))
                                if (span.end > end) newSpans.add(span.copy(start = end))
                            } else {
                                newSpans.add(span)
                            }
                        }
                    } else {
                        val tempSpans = item.textSpans.toMutableList()
                        tempSpans.removeAll { it.format == format && it.start < end && it.end > start }
                        tempSpans.add(TextSpan(start, end, format))
                        val sortedSpans = tempSpans.sortedWith(compareBy({ it.format.ordinal }, { it.start }))
                        val mergedSpans = mutableListOf<TextSpan>()
                        sortedSpans.forEach { span ->
                            val lastSpan = mergedSpans.lastOrNull()
                            if (lastSpan != null && lastSpan.format == span.format && lastSpan.end >= span.start) {
                                mergedSpans[mergedSpans.lastIndex] = lastSpan.copy(end = max(lastSpan.end, span.end))
                            } else {
                                mergedSpans.add(span)
                            }
                        }
                        newSpans.addAll(mergedSpans)
                    }
                    item.copy(textSpans = newSpans.sortedBy { it.start })
                } else item
            })
        }
    }

    fun selectGroup(groupId: String) {
        _uiState.update { it.copy(selectedGroupId = groupId) }

        viewModelScope.launch {
            settingsRepository.saveSelectedGroupId(groupId)
        }
    }

    fun createNewNote(title: String): String {
        val currentUser = _uiState.value.groups
            .find { it.id == _uiState.value.selectedGroupId }
            ?.members?.firstOrNull() ?: return ""

        val newNote = Note(
            title = title,
            items = listOf(
                NoteItem(
                    type = NoteItemType.TEXT,
                    text = "",
                    authorId = currentUser.id
                )
            ),
            authorId = currentUser.id
        )

        _uiState.update { currentState ->
            val newGroups = currentState.groups.map {
                if (it.id == currentState.selectedGroupId) {
                    it.copy(notes = it.notes + newNote)
                } else it
            }
            currentState.copy(groups = newGroups)
        }
        return newNote.id
    }

    private fun loadSampleData() {
        val user1 = User(id = "1", name = "Tú", color = AvatarGreen)
        val user2 = User(id = "2", name = "Ana", color = AvatarYellow)
        val user3 = User(id = "3", name = "Pedro", color = AvatarPurple)

        val groupFamilia = Group(
            id = "familia",
            name = "Familia",
            icon = Icons.Default.Home,
            members = listOf(user1, user2, user3),
            notes = listOf(
                Note(
                    id = "nota1",
                    title = "Planes de Vacaciones",
                    authorId = "1",
                    items = listOf(
                        NoteItem(
                            type = NoteItemType.TEXT,
                            text = "Destino: la playa.\nActividades:\n- Surf\n- Voleibol",
                            authorId = "1",
                            textSpans = listOf(
                                TextSpan(0, 7, TextFormat.BOLD),
                                TextSpan(9, 18, TextFormat.ITALIC)
                            )
                        ),
                        NoteItem(
                            type = NoteItemType.TEXT,
                            text = "¡No olviden el protector solar!",
                            authorId = "2"
                        )
                    )
                ),
                Note(
                    id = "nota2",
                    title = "Lista de Compras",
                    authorId = "1",
                    items = listOf(
                        NoteItem(type = NoteItemType.CHECKLIST, text = "Leche", authorId = "1"),
                        NoteItem(type = NoteItemType.CHECKLIST, text = "Huevos", authorId = "2", isChecked = true),
                        NoteItem(type = NoteItemType.CHECKLIST, text = "Pan", authorId = "1")
                    )
                )
            )
        )

        val groupAmigos = Group(
            id = "amigos",
            name = "Amigos",
            icon = Icons.Default.Face,
            members = listOf(user1, user2),
            notes = listOf(
                Note(
                    id = "nota_amigos_1",
                    title = "Ideas para el finde",
                    authorId = "2",
                    items = listOf(
                        NoteItem(type = NoteItemType.CHECKLIST, text = "Ir al cine", authorId = "2"),
                        NoteItem(type = NoteItemType.CHECKLIST, text = "Caminata por el monte", authorId = "1", isChecked = true),
                        NoteItem(type = NoteItemType.CHECKLIST, text = "Probar el nuevo restaurante", authorId = "2")
                    )
                ),
                Note(
                    id = "nota_amigos_2",
                    title = "Películas pendientes",
                    authorId = "1",
                    items = listOf(
                        NoteItem(
                            type = NoteItemType.TEXT,
                            text = "Tenemos que ver la última de ciencia ficción y la comedia que recomendó Ana.",
                            authorId = "1"
                        )
                    )
                )
            )
        )

        val groupTrabajo = Group(
            id = "trabajo",
            name = "Trabajo",
            icon = Icons.Default.Work,
            members = listOf(user1, user3),
            notes = listOf(
                Note(
                    id = "nota_trabajo_1",
                    title = "Reunión de Proyecto",
                    authorId = "3",
                    items = listOf(
                        NoteItem(
                            type = NoteItemType.TEXT,
                            text = "Puntos a tratar: revisión de hitos, próximos pasos y asignación de tareas.",
                            authorId = "3"
                        ),
                        NoteItem(
                            type = NoteItemType.TEXT,
                            text = "Yo me encargo de la presentación inicial.",
                            authorId = "1"
                        )
                    )
                ),
                Note(
                    id = "nota_trabajo_2",
                    title = "Tareas Trimestre",
                    authorId = "1",
                    items = listOf(
                        NoteItem(type = NoteItemType.CHECKLIST, text = "Informe de resultados", authorId = "1", isChecked = true),
                        NoteItem(type = NoteItemType.CHECKLIST, text = "Planificación Q4", authorId = "3"),
                        NoteItem(type = NoteItemType.CHECKLIST, text = "Actualizar documentación", authorId = "1")
                    )
                )
            )
        )

        _uiState.value = AppUiState(
            groups = listOf(groupFamilia, groupAmigos, groupTrabajo),
            selectedGroupId = groupFamilia.id
        )
    }
}