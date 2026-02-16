package com.jcb1973.marginalia.data.repository

import com.jcb1973.marginalia.data.dao.AuthorDao
import com.jcb1973.marginalia.data.dao.BookDao
import com.jcb1973.marginalia.data.dao.TagDao
import com.jcb1973.marginalia.data.entity.AuthorEntity
import com.jcb1973.marginalia.data.entity.BookAuthorCrossRef
import com.jcb1973.marginalia.data.entity.BookEntity
import com.jcb1973.marginalia.data.entity.BookTagCrossRef
import com.jcb1973.marginalia.domain.model.Book
import com.jcb1973.marginalia.domain.model.ReadingStatus
import com.jcb1973.marginalia.domain.model.Tag
import com.jcb1973.marginalia.domain.model.TagColor
import com.jcb1973.marginalia.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val authorDao: AuthorDao,
    private val tagDao: TagDao
) : BookRepository {

    override fun getAllBooks(): Flow<List<Book>> =
        bookDao.getAll().map { entities -> entities.map { enrichBook(it) } }

    override fun getBooksByStatus(status: String): Flow<List<Book>> =
        bookDao.getByStatus(status).map { entities -> entities.map { enrichBook(it) } }

    override fun getCountByStatus(status: String): Flow<Int> =
        bookDao.getCountByStatus(status)

    override fun getTotalCount(): Flow<Int> =
        bookDao.getTotalCount()

    override fun searchBooks(query: String): Flow<List<Book>> =
        bookDao.search(query).map { entities -> entities.map { enrichBook(it) } }

    override suspend fun getBookById(id: Long): Book? {
        val entity = bookDao.getById(id) ?: return null
        return enrichBook(entity)
    }

    override suspend fun saveBook(book: Book): Long {
        val entity = book.toEntity()
        val bookId = bookDao.insert(entity)

        for (authorName in book.authors) {
            val author = authorDao.getByName(authorName)
                ?: AuthorEntity(name = authorName)
            val authorId = if (author.id == 0L) authorDao.insert(author) else author.id
            bookDao.insertBookAuthor(BookAuthorCrossRef(bookId, authorId))
        }

        for (tag in book.tags) {
            bookDao.insertBookTag(BookTagCrossRef(bookId, tag.id))
        }

        return bookId
    }

    override suspend fun updateBook(book: Book) {
        bookDao.update(book.toEntity())

        bookDao.deleteAllAuthorsForBook(book.id)
        for (authorName in book.authors) {
            val author = authorDao.getByName(authorName)
                ?: AuthorEntity(name = authorName)
            val authorId = if (author.id == 0L) authorDao.insert(author) else author.id
            bookDao.insertBookAuthor(BookAuthorCrossRef(book.id, authorId))
        }

        bookDao.deleteAllTagsForBook(book.id)
        for (tag in book.tags) {
            bookDao.insertBookTag(BookTagCrossRef(book.id, tag.id))
        }
    }

    override suspend fun deleteBook(id: Long) {
        bookDao.deleteById(id)
    }

    override suspend fun getBooksPaginated(limit: Int, offset: Int): List<Book> {
        return bookDao.getPaginated(limit, offset).map { enrichBook(it) }
    }

    private suspend fun enrichBook(entity: BookEntity): Book {
        val authors = authorDao.getForBookOnce(entity.id)
        val tags = tagDao.getForBookOnce(entity.id)
        return entity.toDomain(
            authors = authors.map { it.name },
            tags = tags.map { it.toDomainTag() }
        )
    }

    private fun BookEntity.toDomain(authors: List<String>, tags: List<Tag>): Book = Book(
        id = id,
        title = title,
        isbn = isbn,
        coverImageUrl = coverImageUrl,
        coverImagePath = coverImagePath,
        publisher = publisher,
        publishedDate = publishedDate,
        description = description,
        pageCount = pageCount,
        status = ReadingStatus.fromValue(status),
        rating = rating,
        dateAdded = dateAdded,
        dateStarted = dateStarted,
        dateFinished = dateFinished,
        authors = authors,
        tags = tags
    )

    private fun com.jcb1973.marginalia.data.entity.TagEntity.toDomainTag(): Tag = Tag(
        id = id,
        name = name,
        displayName = displayName,
        color = TagColor.fromName(colorName)
    )

    private fun Book.toEntity(): BookEntity = BookEntity(
        id = id,
        title = title,
        isbn = isbn,
        coverImageUrl = coverImageUrl,
        coverImagePath = coverImagePath,
        publisher = publisher,
        publishedDate = publishedDate,
        description = description,
        pageCount = pageCount,
        status = status.value,
        rating = rating,
        dateAdded = dateAdded,
        dateStarted = dateStarted,
        dateFinished = dateFinished
    )
}
