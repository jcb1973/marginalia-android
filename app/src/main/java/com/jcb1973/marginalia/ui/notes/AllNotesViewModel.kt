package com.jcb1973.marginalia.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcb1973.marginalia.domain.model.Book
import com.jcb1973.marginalia.domain.model.Note
import com.jcb1973.marginalia.domain.repository.BookRepository
import com.jcb1973.marginalia.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllNotesUiState(
    val book: Book? = null,
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val sortNewestFirst: Boolean = true,
    val isLoading: Boolean = true
)

@HiltViewModel
class AllNotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L

    private val _uiState = MutableStateFlow(AllNotesUiState())
    val uiState: StateFlow<AllNotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(book = bookRepository.getBookById(bookId))
        }
        loadNotes()
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadNotes()
    }

    fun toggleSort() {
        _uiState.value = _uiState.value.copy(sortNewestFirst = !_uiState.value.sortNewestFirst)
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            val query = _uiState.value.searchQuery
            val flow = if (query.isBlank()) {
                noteRepository.getNotesForBook(bookId)
            } else {
                noteRepository.searchNotesForBook(bookId, query)
            }
            flow.collect { notes ->
                val sorted = if (_uiState.value.sortNewestFirst) {
                    notes.sortedByDescending { it.createdAt }
                } else {
                    notes.sortedBy { it.createdAt }
                }
                _uiState.value = _uiState.value.copy(notes = sorted, isLoading = false)
            }
        }
    }
}
