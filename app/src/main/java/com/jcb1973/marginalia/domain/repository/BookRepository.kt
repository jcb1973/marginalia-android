package com.jcb1973.marginalia.domain.repository

import com.jcb1973.marginalia.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    fun getBooksByStatus(status: String): Flow<List<Book>>
    fun getCountByStatus(status: String): Flow<Int>
    fun getTotalCount(): Flow<Int>
    fun searchBooks(query: String): Flow<List<Book>>
    suspend fun getBookById(id: Long): Book?
    suspend fun saveBook(book: Book): Long
    suspend fun updateBook(book: Book)
    suspend fun deleteBook(id: Long)
    suspend fun getBooksPaginated(limit: Int, offset: Int): List<Book>
}
