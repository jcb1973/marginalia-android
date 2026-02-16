package com.jcb1973.marginalia.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jcb1973.marginalia.data.entity.AuthorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(author: AuthorEntity): Long

    @Update
    suspend fun update(author: AuthorEntity)

    @Delete
    suspend fun delete(author: AuthorEntity)

    @Query("SELECT * FROM authors WHERE id = :id")
    suspend fun getById(id: Long): AuthorEntity?

    @Query("SELECT * FROM authors WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): AuthorEntity?

    @Query("SELECT * FROM authors ORDER BY name ASC")
    fun getAll(): Flow<List<AuthorEntity>>

    @Query("SELECT * FROM authors WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<AuthorEntity>>

    @Query(
        """
        SELECT authors.* FROM authors
        INNER JOIN book_author_cross_ref ON authors.id = book_author_cross_ref.authorId
        WHERE book_author_cross_ref.bookId = :bookId
        ORDER BY authors.name ASC
        """
    )
    fun getForBook(bookId: Long): Flow<List<AuthorEntity>>

    @Query(
        """
        SELECT authors.* FROM authors
        INNER JOIN book_author_cross_ref ON authors.id = book_author_cross_ref.authorId
        WHERE book_author_cross_ref.bookId = :bookId
        ORDER BY authors.name ASC
        """
    )
    suspend fun getForBookOnce(bookId: Long): List<AuthorEntity>
}
