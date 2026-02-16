package com.jcb1973.marginalia.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jcb1973.marginalia.data.entity.BookAuthorCrossRef
import com.jcb1973.marginalia.data.entity.BookEntity
import com.jcb1973.marginalia.data.entity.BookTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)

    @Delete
    suspend fun delete(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteById(bookId: Long)

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getById(id: Long): BookEntity?

    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    fun getAll(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE status = :status ORDER BY dateAdded DESC")
    fun getByStatus(status: String): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM books WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM books")
    fun getTotalCount(): Flow<Int>

    @Query(
        """
        SELECT * FROM books
        WHERE title LIKE '%' || :query || '%'
        OR isbn LIKE '%' || :query || '%'
        ORDER BY dateAdded DESC
        """
    )
    fun search(query: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY dateAdded DESC LIMIT :limit OFFSET :offset")
    suspend fun getPaginated(limit: Int, offset: Int): List<BookEntity>

    // Cross-ref operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookAuthor(crossRef: BookAuthorCrossRef)

    @Delete
    suspend fun deleteBookAuthor(crossRef: BookAuthorCrossRef)

    @Query("DELETE FROM book_author_cross_ref WHERE bookId = :bookId")
    suspend fun deleteAllAuthorsForBook(bookId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookTag(crossRef: BookTagCrossRef)

    @Delete
    suspend fun deleteBookTag(crossRef: BookTagCrossRef)

    @Query("DELETE FROM book_tag_cross_ref WHERE bookId = :bookId")
    suspend fun deleteAllTagsForBook(bookId: Long)

    @Query(
        """
        SELECT books.* FROM books
        INNER JOIN book_tag_cross_ref ON books.id = book_tag_cross_ref.bookId
        WHERE book_tag_cross_ref.tagId = :tagId
        ORDER BY dateAdded DESC
        """
    )
    fun getBooksByTag(tagId: Long): Flow<List<BookEntity>>

    @Query(
        """
        SELECT books.* FROM books
        INNER JOIN book_author_cross_ref ON books.id = book_author_cross_ref.bookId
        WHERE book_author_cross_ref.authorId = :authorId
        ORDER BY dateAdded DESC
        """
    )
    fun getBooksByAuthor(authorId: Long): Flow<List<BookEntity>>
}
