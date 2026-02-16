package com.jcb1973.marginalia.domain.repository

import com.jcb1973.marginalia.domain.model.Quote
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun getQuotesForBook(bookId: Long): Flow<List<Quote>>
    fun searchQuotesForBook(bookId: Long, query: String): Flow<List<Quote>>
    fun searchQuotes(query: String): Flow<List<Quote>>
    fun getRecentQuotes(limit: Int): Flow<List<Quote>>
    fun getQuotesWithCommentForBook(bookId: Long): Flow<List<Quote>>
    suspend fun getQuoteById(id: Long): Quote?
    suspend fun saveQuote(quote: Quote): Long
    suspend fun updateQuote(quote: Quote)
    suspend fun deleteQuote(id: Long)
}
