package com.jcb1973.marginalia.ui.quotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcb1973.marginalia.domain.model.Book
import com.jcb1973.marginalia.domain.model.Quote
import com.jcb1973.marginalia.domain.repository.BookRepository
import com.jcb1973.marginalia.domain.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllQuotesUiState(
    val book: Book? = null,
    val quotes: List<Quote> = emptyList(),
    val searchQuery: String = "",
    val sortNewestFirst: Boolean = true,
    val filterHasComment: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class AllQuotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L

    private val _uiState = MutableStateFlow(AllQuotesUiState())
    val uiState: StateFlow<AllQuotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(book = bookRepository.getBookById(bookId))
        }
        loadQuotes()
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadQuotes()
    }

    fun toggleSort() {
        _uiState.value = _uiState.value.copy(sortNewestFirst = !_uiState.value.sortNewestFirst)
        loadQuotes()
    }

    fun toggleHasCommentFilter() {
        _uiState.value = _uiState.value.copy(filterHasComment = !_uiState.value.filterHasComment)
        loadQuotes()
    }

    private fun loadQuotes() {
        viewModelScope.launch {
            val state = _uiState.value
            val flow = when {
                state.filterHasComment -> quoteRepository.getQuotesWithCommentForBook(bookId)
                state.searchQuery.isNotBlank() -> quoteRepository.searchQuotesForBook(bookId, state.searchQuery)
                else -> quoteRepository.getQuotesForBook(bookId)
            }
            flow.collect { quotes ->
                val sorted = if (state.sortNewestFirst) {
                    quotes.sortedByDescending { it.createdAt }
                } else {
                    quotes.sortedBy { it.createdAt }
                }
                _uiState.value = _uiState.value.copy(quotes = sorted, isLoading = false)
            }
        }
    }
}
