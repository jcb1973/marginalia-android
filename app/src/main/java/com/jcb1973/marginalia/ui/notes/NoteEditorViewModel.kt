package com.jcb1973.marginalia.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcb1973.marginalia.domain.model.Note
import com.jcb1973.marginalia.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteEditorUiState(
    val content: String = "",
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: 0L
    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    init {
        if (noteId != 0L) {
            viewModelScope.launch {
                val note = noteRepository.getNoteById(noteId)
                if (note != null) {
                    _uiState.value = _uiState.value.copy(
                        content = note.content,
                        isEditing = true
                    )
                }
            }
        }
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun save() {
        val content = _uiState.value.content.trim()
        if (content.isBlank()) return

        viewModelScope.launch {
            if (_uiState.value.isEditing) {
                val note = noteRepository.getNoteById(noteId) ?: return@launch
                noteRepository.updateNote(note.copy(content = content))
            } else {
                noteRepository.saveNote(
                    Note(content = content, bookId = bookId)
                )
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun delete() {
        if (!_uiState.value.isEditing) return
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }
}
