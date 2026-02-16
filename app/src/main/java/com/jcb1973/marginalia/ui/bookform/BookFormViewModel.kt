package com.jcb1973.marginalia.ui.bookform

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcb1973.marginalia.data.remote.BookLookupService
import com.jcb1973.marginalia.data.service.IsbnValidator
import com.jcb1973.marginalia.domain.model.Book
import com.jcb1973.marginalia.domain.model.ReadingStatus
import com.jcb1973.marginalia.domain.model.Tag
import com.jcb1973.marginalia.domain.repository.BookRepository
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

data class BookFormUiState(
    val isbn: String = "",
    val title: String = "",
    val authorInput: String = "",
    val authors: List<String> = emptyList(),
    val tagInput: String = "",
    val selectedTags: List<Tag> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val coverImageUrl: String? = null,
    val coverImagePath: String? = null,
    val publisher: String = "",
    val publishedDate: String = "",
    val description: String = "",
    val pageCount: String = "",
    val status: ReadingStatus = ReadingStatus.TO_READ,
    val rating: Int? = null,
    val isEditing: Boolean = false,
    val isLooking: Boolean = false,
    val isSaved: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class BookFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val tagRepository: TagRepository,
    private val lookupService: BookLookupService,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L

    private val _uiState = MutableStateFlow(BookFormUiState())
    val uiState: StateFlow<BookFormUiState> = _uiState.asStateFlow()

    init {
        if (bookId != 0L) {
            viewModelScope.launch {
                val book = bookRepository.getBookById(bookId) ?: return@launch
                _uiState.value = _uiState.value.copy(
                    isbn = book.isbn ?: "",
                    title = book.title,
                    authors = book.authors,
                    selectedTags = book.tags,
                    coverImageUrl = book.coverImageUrl,
                    coverImagePath = book.coverImagePath,
                    publisher = book.publisher ?: "",
                    publishedDate = book.publishedDate ?: "",
                    description = book.description ?: "",
                    pageCount = book.pageCount?.toString() ?: "",
                    status = book.status,
                    rating = book.rating,
                    isEditing = true
                )
            }
        }
        viewModelScope.launch {
            tagRepository.getAllTags().collect { tags ->
                _uiState.value = _uiState.value.copy(allTags = tags)
            }
        }
    }

    fun updateIsbn(isbn: String) { _uiState.value = _uiState.value.copy(isbn = isbn) }
    fun updateTitle(title: String) { _uiState.value = _uiState.value.copy(title = title) }
    fun updateAuthorInput(input: String) { _uiState.value = _uiState.value.copy(authorInput = input) }
    fun updateTagInput(input: String) { _uiState.value = _uiState.value.copy(tagInput = input) }
    fun updatePublisher(pub: String) { _uiState.value = _uiState.value.copy(publisher = pub) }
    fun updatePublishedDate(date: String) { _uiState.value = _uiState.value.copy(publishedDate = date) }
    fun updateDescription(desc: String) { _uiState.value = _uiState.value.copy(description = desc) }
    fun updatePageCount(count: String) { _uiState.value = _uiState.value.copy(pageCount = count) }
    fun setStatus(status: ReadingStatus) { _uiState.value = _uiState.value.copy(status = status) }
    fun setRating(rating: Int?) { _uiState.value = _uiState.value.copy(rating = rating) }
    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null) }

    fun addAuthor() {
        val name = _uiState.value.authorInput.trim()
        if (name.isBlank() || _uiState.value.authors.contains(name)) return
        _uiState.value = _uiState.value.copy(
            authors = _uiState.value.authors + name,
            authorInput = ""
        )
    }

    fun removeAuthor(name: String) {
        _uiState.value = _uiState.value.copy(
            authors = _uiState.value.authors.filter { it != name }
        )
    }

    fun addTag(tag: Tag) {
        if (_uiState.value.selectedTags.any { it.id == tag.id }) return
        _uiState.value = _uiState.value.copy(
            selectedTags = _uiState.value.selectedTags + tag,
            tagInput = ""
        )
    }

    fun removeTag(tag: Tag) {
        _uiState.value = _uiState.value.copy(
            selectedTags = _uiState.value.selectedTags.filter { it.id != tag.id }
        )
    }

    fun setScannedIsbn(isbn: String) {
        _uiState.value = _uiState.value.copy(isbn = isbn)
        lookupIsbn()
    }

    fun lookupIsbn() {
        val isbn = _uiState.value.isbn.trim()
        if (!IsbnValidator.isValid(isbn)) {
            _uiState.value = _uiState.value.copy(message = "Invalid ISBN")
            return
        }
        _uiState.value = _uiState.value.copy(isLooking = true)
        viewModelScope.launch {
            val result = lookupService.lookupByIsbn(isbn)
            result.onSuccess { lookup ->
                _uiState.value = _uiState.value.copy(
                    title = lookup.title,
                    authors = lookup.authors,
                    isbn = lookup.isbn,
                    coverImageUrl = lookup.coverImageUrl,
                    publisher = lookup.publisher ?: "",
                    publishedDate = lookup.publishedDate ?: "",
                    description = lookup.description ?: "",
                    pageCount = lookup.pageCount?.toString() ?: "",
                    isLooking = false,
                    message = "Book found!"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLooking = false,
                    message = "Lookup failed: ${error.message}"
                )
            }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = state.copy(message = "Title is required")
            return
        }

        val now = System.currentTimeMillis()
        viewModelScope.launch {
            val book = Book(
                id = if (state.isEditing) bookId else 0L,
                title = state.title.trim(),
                isbn = state.isbn.trim().takeIf { it.isNotBlank() },
                coverImageUrl = state.coverImageUrl,
                coverImagePath = state.coverImagePath,
                publisher = state.publisher.trim().takeIf { it.isNotBlank() },
                publishedDate = state.publishedDate.trim().takeIf { it.isNotBlank() },
                description = state.description.trim().takeIf { it.isNotBlank() },
                pageCount = state.pageCount.trim().toIntOrNull(),
                status = state.status,
                rating = state.rating,
                authors = state.authors,
                tags = state.selectedTags,
                dateAdded = if (state.isEditing) {
                    bookRepository.getBookById(bookId)?.dateAdded ?: now
                } else now,
                dateStarted = if (state.status == ReadingStatus.READING) now else null,
                dateFinished = if (state.status == ReadingStatus.READ) now else null
            )

            if (state.isEditing) {
                bookRepository.updateBook(book)
            } else {
                bookRepository.saveBook(book)
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
