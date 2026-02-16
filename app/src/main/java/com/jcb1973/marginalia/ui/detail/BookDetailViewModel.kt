package com.jcb1973.marginalia.ui.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcb1973.marginalia.domain.model.Book
import com.jcb1973.marginalia.domain.model.Note
import com.jcb1973.marginalia.domain.model.Quote
import com.jcb1973.marginalia.domain.model.ReadingStatus
import com.jcb1973.marginalia.domain.model.Tag
import com.jcb1973.marginalia.domain.repository.BookRepository
import com.jcb1973.marginalia.domain.repository.NoteRepository
import com.jcb1973.marginalia.domain.repository.QuoteRepository
import com.jcb1973.marginalia.domain.repository.TagRepository
import com.jcb1973.marginalia.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class BookDetailUiState(
    val book: Book? = null,
    val notes: List<Note> = emptyList(),
    val quotes: List<Quote> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val noteRepository: NoteRepository,
    private val quoteRepository: QuoteRepository,
    private val tagRepository: TagRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        loadBook()
        viewModelScope.launch {
            noteRepository.getNotesForBook(bookId).collect { notes ->
                _uiState.value = _uiState.value.copy(notes = notes)
            }
        }
        viewModelScope.launch {
            quoteRepository.getQuotesForBook(bookId).collect { quotes ->
                _uiState.value = _uiState.value.copy(quotes = quotes)
            }
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(allTags = tagRepository.getAllTagsOnce())
        }
    }

    private fun loadBook() {
        viewModelScope.launch {
            val book = bookRepository.getBookById(bookId)
            if (book != null && book.coverImagePath == null && book.coverImageUrl != null) {
                val path = withContext(Dispatchers.IO) {
                    ImageUtils.downloadAndCompress(context, book.coverImageUrl, "cover_${book.id}")
                }
                if (path != null) {
                    val updated = book.copy(coverImagePath = path)
                    bookRepository.updateBook(updated)
                    _uiState.value = _uiState.value.copy(book = updated, isLoading = false)
                    return@launch
                }
            }
            _uiState.value = _uiState.value.copy(book = book, isLoading = false)
        }
    }

    fun cycleStatus() {
        val book = _uiState.value.book ?: return
        val newStatus = when (book.status) {
            ReadingStatus.TO_READ -> ReadingStatus.READING
            ReadingStatus.READING -> ReadingStatus.READ
            ReadingStatus.READ -> ReadingStatus.TO_READ
        }
        val now = System.currentTimeMillis()
        val updated = book.copy(
            status = newStatus,
            dateStarted = when (newStatus) {
                ReadingStatus.READING -> book.dateStarted ?: now
                else -> book.dateStarted
            },
            dateFinished = when (newStatus) {
                ReadingStatus.READ -> book.dateFinished ?: now
                else -> book.dateFinished
            }
        )
        viewModelScope.launch {
            bookRepository.updateBook(updated)
            _uiState.value = _uiState.value.copy(book = updated)
        }
    }

    fun setRating(rating: Int) {
        val book = _uiState.value.book ?: return
        val updated = book.copy(rating = rating)
        viewModelScope.launch {
            bookRepository.updateBook(updated)
            _uiState.value = _uiState.value.copy(book = updated)
        }
    }

    fun addTag(tag: Tag) {
        val book = _uiState.value.book ?: return
        if (book.tags.any { it.id == tag.id }) return
        val updated = book.copy(tags = book.tags + tag)
        viewModelScope.launch {
            bookRepository.updateBook(updated)
            _uiState.value = _uiState.value.copy(book = updated)
        }
    }

    fun createAndAddTag(name: String) {
        val book = _uiState.value.book ?: return
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val tagName = trimmed.lowercase().replace(Regex("\\s+"), "-")
            val newTag = Tag(name = tagName, displayName = trimmed)
            val id = tagRepository.saveTag(newTag)
            val saved = newTag.copy(id = id)
            val updated = book.copy(tags = book.tags + saved)
            bookRepository.updateBook(updated)
            _uiState.value = _uiState.value.copy(
                book = updated,
                allTags = tagRepository.getAllTagsOnce()
            )
        }
    }

    fun removeTag(tag: Tag) {
        val book = _uiState.value.book ?: return
        val updated = book.copy(tags = book.tags.filter { it.id != tag.id })
        viewModelScope.launch {
            bookRepository.updateBook(updated)
            _uiState.value = _uiState.value.copy(book = updated)
        }
    }

    fun deleteBook() {
        viewModelScope.launch {
            bookRepository.deleteBook(bookId)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
