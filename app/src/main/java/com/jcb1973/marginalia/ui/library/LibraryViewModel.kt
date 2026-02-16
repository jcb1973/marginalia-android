package com.jcb1973.marginalia.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcb1973.marginalia.domain.model.Book
import com.jcb1973.marginalia.domain.model.ReadingStatus
import com.jcb1973.marginalia.domain.model.SortOrder
import com.jcb1973.marginalia.domain.model.Tag
import com.jcb1973.marginalia.domain.repository.BookRepository
import com.jcb1973.marginalia.domain.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val books: List<Book> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val selectedTags: Set<Long> = emptySet(),
    val searchQuery: String = "",
    val statusFilter: ReadingStatus? = null,
    val ratingFilter: Int? = null,
    val sortOrder: SortOrder = SortOrder.DATE_ADDED_DESC,
    val isLoading: Boolean = true,
    val currentPage: Int = 0,
    val hasMore: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val pageSize = 10

    init {
        loadBooks()
        viewModelScope.launch {
            tagRepository.getAllTags().collect { tags ->
                _uiState.value = _uiState.value.copy(allTags = tags)
            }
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, currentPage = 0, books = emptyList())
        loadBooks()
    }

    fun setStatusFilter(status: ReadingStatus?) {
        _uiState.value = _uiState.value.copy(statusFilter = status, currentPage = 0, books = emptyList())
        loadBooks()
    }

    fun setRatingFilter(rating: Int?) {
        _uiState.value = _uiState.value.copy(ratingFilter = rating, currentPage = 0, books = emptyList())
        loadBooks()
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = order, currentPage = 0, books = emptyList())
        loadBooks()
    }

    fun toggleTag(tagId: Long) {
        val current = _uiState.value.selectedTags.toMutableSet()
        if (current.contains(tagId)) current.remove(tagId) else current.add(tagId)
        _uiState.value = _uiState.value.copy(selectedTags = current, currentPage = 0, books = emptyList())
        loadBooks()
    }

    fun loadMore() {
        if (!_uiState.value.hasMore || _uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(currentPage = _uiState.value.currentPage + 1)
        loadBooks(append = true)
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            bookRepository.deleteBook(bookId)
            _uiState.value = _uiState.value.copy(
                books = _uiState.value.books.filter { it.id != bookId }
            )
        }
    }

    private fun loadBooks(append: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val state = _uiState.value
            val offset = if (append) state.currentPage * pageSize else 0
            val rawBooks = bookRepository.getBooksPaginated(pageSize, offset)

            val filtered = rawBooks
                .filter { book ->
                    if (state.searchQuery.isBlank()) true
                    else book.title.contains(state.searchQuery, ignoreCase = true) ||
                            book.authors.any { it.contains(state.searchQuery, ignoreCase = true) } ||
                            book.isbn?.contains(state.searchQuery, ignoreCase = true) == true
                }
                .filter { book ->
                    state.statusFilter == null || book.status == state.statusFilter
                }
                .filter { book ->
                    state.ratingFilter == null || book.rating == state.ratingFilter
                }
                .filter { book ->
                    state.selectedTags.isEmpty() ||
                            book.tags.any { it.id in state.selectedTags }
                }

            val sorted = sortBooks(filtered, state.sortOrder)

            val updatedBooks = if (append) state.books + sorted else sorted
            _uiState.value = state.copy(
                books = updatedBooks,
                hasMore = rawBooks.size == pageSize,
                isLoading = false
            )
        }
    }

    private fun sortBooks(books: List<Book>, order: SortOrder): List<Book> = when (order) {
        SortOrder.TITLE_ASC -> books.sortedBy { it.title.lowercase() }
        SortOrder.TITLE_DESC -> books.sortedByDescending { it.title.lowercase() }
        SortOrder.DATE_ADDED_DESC -> books.sortedByDescending { it.dateAdded }
        SortOrder.DATE_ADDED_ASC -> books.sortedBy { it.dateAdded }
        SortOrder.RATING_DESC -> books.sortedByDescending { it.rating ?: 0 }
        SortOrder.RATING_ASC -> books.sortedBy { it.rating ?: 0 }
        SortOrder.AUTHOR_ASC -> books.sortedBy { it.authors.firstOrNull()?.lowercase() ?: "" }
        SortOrder.AUTHOR_DESC -> books.sortedByDescending { it.authors.firstOrNull()?.lowercase() ?: "" }
    }
}
