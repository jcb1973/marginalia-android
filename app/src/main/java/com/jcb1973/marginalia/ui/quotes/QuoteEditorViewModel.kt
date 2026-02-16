package com.jcb1973.marginalia.ui.quotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcb1973.marginalia.domain.model.Quote
import com.jcb1973.marginalia.domain.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuoteEditorUiState(
    val text: String = "",
    val comment: String = "",
    val pageNumber: String = "",
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class QuoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val quoteId: Long = savedStateHandle.get<Long>("quoteId") ?: 0L
    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L

    private val _uiState = MutableStateFlow(QuoteEditorUiState())
    val uiState: StateFlow<QuoteEditorUiState> = _uiState.asStateFlow()

    init {
        if (quoteId != 0L) {
            viewModelScope.launch {
                val quote = quoteRepository.getQuoteById(quoteId)
                if (quote != null) {
                    _uiState.value = _uiState.value.copy(
                        text = quote.text,
                        comment = quote.comment ?: "",
                        pageNumber = quote.pageNumber?.toString() ?: "",
                        isEditing = true
                    )
                }
            }
        }
    }

    fun updateText(text: String) {
        _uiState.value = _uiState.value.copy(text = text)
    }

    fun updateComment(comment: String) {
        _uiState.value = _uiState.value.copy(comment = comment)
    }

    fun updatePageNumber(page: String) {
        _uiState.value = _uiState.value.copy(pageNumber = page)
    }

    fun setOcrText(text: String) {
        _uiState.value = _uiState.value.copy(text = text)
    }

    fun save() {
        val text = _uiState.value.text.trim()
        if (text.isBlank()) return

        val comment = _uiState.value.comment.trim().takeIf { it.isNotBlank() }
        val pageNumber = _uiState.value.pageNumber.trim().toIntOrNull()

        viewModelScope.launch {
            if (_uiState.value.isEditing) {
                val quote = quoteRepository.getQuoteById(quoteId) ?: return@launch
                quoteRepository.updateQuote(
                    quote.copy(text = text, comment = comment, pageNumber = pageNumber)
                )
            } else {
                quoteRepository.saveQuote(
                    Quote(text = text, comment = comment, pageNumber = pageNumber, bookId = bookId)
                )
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun delete() {
        if (!_uiState.value.isEditing) return
        viewModelScope.launch {
            quoteRepository.deleteQuote(quoteId)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }
}
