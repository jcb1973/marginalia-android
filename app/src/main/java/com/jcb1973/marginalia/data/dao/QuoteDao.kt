package com.jcb1973.marginalia.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jcb1973.marginalia.data.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quote: QuoteEntity): Long

    @Update
    suspend fun update(quote: QuoteEntity)

    @Delete
    suspend fun delete(quote: QuoteEntity)

    @Query("DELETE FROM quotes WHERE id = :quoteId")
    suspend fun deleteById(quoteId: Long)

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getById(id: Long): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun getForBook(bookId: Long): Flow<List<QuoteEntity>>

    @Query(
        """
        SELECT * FROM quotes
        WHERE bookId = :bookId AND (text LIKE '%' || :query || '%' OR comment LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
        """
    )
    fun searchForBook(bookId: Long, query: String): Flow<List<QuoteEntity>>

    @Query(
        """
        SELECT * FROM quotes
        WHERE text LIKE '%' || :query || '%' OR comment LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun search(query: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<QuoteEntity>>

    @Query(
        """
        SELECT * FROM quotes
        WHERE bookId = :bookId AND comment IS NOT NULL AND comment != ''
        ORDER BY createdAt DESC
        """
    )
    fun getWithCommentForBook(bookId: Long): Flow<List<QuoteEntity>>
}
