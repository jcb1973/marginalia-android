package com.jcb1973.marginalia.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jcb1973.marginalia.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteById(noteId: Long)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun getForBook(bookId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE bookId = :bookId ORDER BY createdAt DESC")
    suspend fun getForBookOnce(bookId: Long): List<NoteEntity>

    @Query(
        """
        SELECT * FROM notes
        WHERE bookId = :bookId AND content LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun searchForBook(bookId: Long, query: String): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT * FROM notes
        WHERE bookId = :bookId AND content LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    suspend fun searchForBookOnce(bookId: Long, query: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<NoteEntity>>
}
