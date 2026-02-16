package com.jcb1973.marginalia.data.service

import com.jcb1973.marginalia.domain.model.Book
import com.jcb1973.marginalia.domain.model.Note
import com.jcb1973.marginalia.domain.model.Quote
import com.jcb1973.marginalia.domain.model.ReadingStatus
import com.jcb1973.marginalia.util.DateUtils
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

data class ImportedBook(
    val book: Book,
    val notes: List<String>,
    val quotes: List<String>
)

@Singleton
class CsvImportService @Inject constructor() {

    fun import(inputStream: InputStream): List<ImportedBook> {
        val reader = InputStreamReader(inputStream, Charsets.UTF_8)
        val lines = parseCsvLines(reader.readText())
        if (lines.isEmpty()) return emptyList()

        val headers = lines.first().map { it.trim().lowercase() }
        val dataLines = lines.drop(1)

        return dataLines.mapNotNull { fields ->
            if (fields.size < headers.size) return@mapNotNull null
            val map = headers.zip(fields).toMap()

            val title = map["title"]?.trim() ?: return@mapNotNull null
            if (title.isBlank()) return@mapNotNull null

            val status = when (map["status"]?.trim()?.lowercase()) {
                "reading" -> ReadingStatus.READING
                "read" -> ReadingStatus.READ
                else -> ReadingStatus.TO_READ
            }

            val book = Book(
                title = title,
                authors = map["authors"]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
                    ?: emptyList(),
                status = status,
                rating = map["rating"]?.trim()?.toIntOrNull()?.coerceIn(1, 5),
                pageCount = map["pages"]?.trim()?.toIntOrNull(),
                publisher = map["publisher"]?.trim()?.takeIf { it.isNotBlank() },
                publishedDate = map["published date"]?.trim()?.takeIf { it.isNotBlank() },
                isbn = map["isbn"]?.trim()?.takeIf { it.isNotBlank() },
                coverImageUrl = map["cover image url"]?.trim()?.takeIf { it.isNotBlank() },
                description = map["description"]?.trim()?.takeIf { it.isNotBlank() },
                dateAdded = map["date added"]?.trim()?.let { DateUtils.parseShort(it) }
                    ?: System.currentTimeMillis(),
                dateStarted = map["date started"]?.trim()?.let { DateUtils.parseShort(it) },
                dateFinished = map["date finished"]?.trim()?.let { DateUtils.parseShort(it) }
            )

            val notes = map["notes"]?.split(";")?.map { it.trim() }?.filter { it.isNotBlank() }
                ?: emptyList()
            val quotes = map["quotes"]?.split(";")?.map { it.trim() }?.filter { it.isNotBlank() }
                ?: emptyList()

            ImportedBook(book, notes, quotes)
        }
    }

    private fun parseCsvLines(text: String): List<List<String>> {
        val result = mutableListOf<List<String>>()
        val currentField = StringBuilder()
        val currentRow = mutableListOf<String>()
        var inQuotes = false
        var i = 0

        while (i < text.length) {
            val c = text[i]
            when {
                inQuotes -> {
                    if (c == '"') {
                        if (i + 1 < text.length && text[i + 1] == '"') {
                            currentField.append('"')
                            i++
                        } else {
                            inQuotes = false
                        }
                    } else {
                        currentField.append(c)
                    }
                }
                c == '"' -> inQuotes = true
                c == ',' -> {
                    currentRow.add(currentField.toString())
                    currentField.clear()
                }
                c == '\r' -> {
                    if (i + 1 < text.length && text[i + 1] == '\n') i++
                    currentRow.add(currentField.toString())
                    currentField.clear()
                    result.add(currentRow.toList())
                    currentRow.clear()
                }
                c == '\n' -> {
                    currentRow.add(currentField.toString())
                    currentField.clear()
                    result.add(currentRow.toList())
                    currentRow.clear()
                }
                else -> currentField.append(c)
            }
            i++
        }

        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString())
            result.add(currentRow.toList())
        }

        return result
    }
}
