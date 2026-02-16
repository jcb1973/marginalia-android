package com.jcb1973.marginalia.data.repository

import com.jcb1973.marginalia.data.dao.QuoteDao
import com.jcb1973.marginalia.data.entity.QuoteEntity
import com.jcb1973.marginalia.domain.model.Quote
import com.jcb1973.marginalia.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepositoryImpl @Inject constructor(
    private val quoteDao: QuoteDao
) : QuoteRepository {

    override fun getQuotesForBook(bookId: Long): Flow<List<Quote>> =
        quoteDao.getForBook(bookId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getQuotesForBookOnce(bookId: Long): List<Quote> =
        quoteDao.getForBookOnce(bookId).map { it.toDomain() }

    override fun searchQuotesForBook(bookId: Long, query: String): Flow<List<Quote>> =
        quoteDao.searchForBook(bookId, query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun searchQuotesForBookOnce(bookId: Long, query: String): List<Quote> =
        quoteDao.searchForBookOnce(bookId, query).map { it.toDomain() }

    override fun searchQuotes(query: String): Flow<List<Quote>> =
        quoteDao.search(query).map { entities -> entities.map { it.toDomain() } }

    override fun getRecentQuotes(limit: Int): Flow<List<Quote>> =
        quoteDao.getRecent(limit).map { entities -> entities.map { it.toDomain() } }

    override fun getQuotesWithCommentForBook(bookId: Long): Flow<List<Quote>> =
        quoteDao.getWithCommentForBook(bookId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getQuotesWithCommentForBookOnce(bookId: Long): List<Quote> =
        quoteDao.getWithCommentForBookOnce(bookId).map { it.toDomain() }

    override suspend fun getQuoteById(id: Long): Quote? =
        quoteDao.getById(id)?.toDomain()

    override suspend fun saveQuote(quote: Quote): Long =
        quoteDao.insert(quote.toEntity())

    override suspend fun updateQuote(quote: Quote) =
        quoteDao.update(quote.toEntity())

    override suspend fun deleteQuote(id: Long) =
        quoteDao.deleteById(id)

    private fun QuoteEntity.toDomain(): Quote = Quote(
        id = id,
        text = text,
        comment = comment,
        pageNumber = pageNumber,
        createdAt = createdAt,
        bookId = bookId
    )

    private fun Quote.toEntity(): QuoteEntity = QuoteEntity(
        id = id,
        text = text,
        comment = comment,
        pageNumber = pageNumber,
        createdAt = createdAt,
        bookId = bookId
    )
}
