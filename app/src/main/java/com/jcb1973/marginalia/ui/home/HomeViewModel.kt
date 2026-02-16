package com.jcb1973.marginalia.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcb1973.marginalia.data.service.CsvExportService
import com.jcb1973.marginalia.data.service.CsvImportService
import com.jcb1973.marginalia.domain.model.Book
import com.jcb1973.marginalia.domain.model.Note
import com.jcb1973.marginalia.domain.model.Quote
import com.jcb1973.marginalia.domain.repository.BookRepository
import com.jcb1973.marginalia.domain.repository.NoteRepository
import com.jcb1973.marginalia.domain.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

data class HomeUiState(
    val currentlyReading: List<Book> = emptyList(),
    val toReadCount: Int = 0,
    val readingCount: Int = 0,
    val readCount: Int = 0,
    val recentNotes: List<Note> = emptyList(),
    val recentQuotes: List<Quote> = emptyList(),
    val totalBooks: Int = 0,
    val isLoading: Boolean = true,
    val message: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val noteRepository: NoteRepository,
    private val quoteRepository: QuoteRepository,
    private val csvExportService: CsvExportService,
    private val csvImportService: CsvImportService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                bookRepository.getBooksByStatus("reading"),
                bookRepository.getCountByStatus("toRead"),
                bookRepository.getCountByStatus("reading"),
                bookRepository.getCountByStatus("read"),
                bookRepository.getTotalCount()
            ) { reading, toReadCount, readingCount, readCount, total ->
                _uiState.value.copy(
                    currentlyReading = reading,
                    toReadCount = toReadCount,
                    readingCount = readingCount,
                    readCount = readCount,
                    totalBooks = total,
                    isLoading = false
                )
            }.collect { state -> _uiState.value = state }
        }

        viewModelScope.launch {
            noteRepository.getRecentNotes(10).collect { notes ->
                _uiState.value = _uiState.value.copy(recentNotes = notes)
            }
        }

        viewModelScope.launch {
            quoteRepository.getRecentQuotes(10).collect { quotes ->
                _uiState.value = _uiState.value.copy(recentQuotes = quotes)
            }
        }
    }

    fun exportCsv(outputStream: OutputStream) {
        viewModelScope.launch {
            try {
                val books = bookRepository.getBooksPaginated(Int.MAX_VALUE, 0)
                val notesMap = mutableMapOf<Long, List<Note>>()
                val quotesMap = mutableMapOf<Long, List<Quote>>()
                for (book in books) {
                    val notes = mutableListOf<Note>()
                    noteRepository.getNotesForBook(book.id).collect { notes.addAll(it); return@collect }
                    notesMap[book.id] = notes

                    val quotes = mutableListOf<Quote>()
                    quoteRepository.getQuotesForBook(book.id).collect { quotes.addAll(it); return@collect }
                    quotesMap[book.id] = quotes
                }
                csvExportService.export(outputStream, books, notesMap, quotesMap)
                _uiState.value = _uiState.value.copy(message = "Export complete")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Export failed: ${e.message}")
            }
        }
    }

    fun importCsv(inputStream: InputStream) {
        viewModelScope.launch {
            try {
                val imported = csvImportService.import(inputStream)
                var count = 0
                for (item in imported) {
                    bookRepository.saveBook(item.book)
                    count++
                }
                _uiState.value = _uiState.value.copy(message = "Imported $count books")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Import failed: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
