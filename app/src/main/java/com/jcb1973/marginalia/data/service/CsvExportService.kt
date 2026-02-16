package com.jcb1973.marginalia.data.service

import com.jcb1973.marginalia.domain.model.Book
import com.jcb1973.marginalia.domain.model.Note
import com.jcb1973.marginalia.domain.model.Quote
import com.jcb1973.marginalia.util.DateUtils
import java.io.OutputStream
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExportService @Inject constructor() {

    private val headers = listOf(
        "Title", "Authors", "Status", "Rating", "Tags", "Pages",
        "Publisher", "Published Date", "Date Added", "Date Started",
        "Date Finished", "ISBN", "Cover Image URL", "Description",
        "Notes", "Quotes"
    )

    fun export(
        outputStream: OutputStream,
        books: List<Book>,
        notesMap: Map<Long, List<Note>>,
        quotesMap: Map<Long, List<Quote>>
    ) {
        val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)
        writer.write(headers.joinToString(",") { escapeField(it) })
        writer.write("\r\n")

        for (book in books) {
            val notes = notesMap[book.id] ?: emptyList()
            val quotes = quotesMap[book.id] ?: emptyList()
            val row = listOf(
                book.title,
                book.authors.joinToString(", "),
                book.status.displayName,
                book.rating?.toString() ?: "",
                book.tags.joinToString("; ") { it.displayName },
                book.pageCount?.toString() ?: "",
                book.publisher ?: "",
                book.publishedDate ?: "",
                DateUtils.formatShort(book.dateAdded),
                book.dateStarted?.let { DateUtils.formatShort(it) } ?: "",
                book.dateFinished?.let { DateUtils.formatShort(it) } ?: "",
                book.isbn ?: "",
                book.coverImageUrl ?: "",
                book.description ?: "",
                notes.joinToString("; ") { it.content },
                quotes.joinToString("; ") { formatQuote(it) }
            )
            writer.write(row.joinToString(",") { escapeField(it) })
            writer.write("\r\n")
        }
        writer.flush()
    }

    private fun formatQuote(quote: Quote): String {
        val sb = StringBuilder(quote.text)
        if (!quote.comment.isNullOrBlank()) {
            sb.append(" [Comment: ${quote.comment}]")
        }
        if (quote.pageNumber != null) {
            sb.append(" (p. ${quote.pageNumber})")
        }
        return sb.toString()
    }

    private fun escapeField(field: String): String {
        return if (field.contains(',') || field.contains('"') || field.contains('\n') || field.contains('\r')) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
}
