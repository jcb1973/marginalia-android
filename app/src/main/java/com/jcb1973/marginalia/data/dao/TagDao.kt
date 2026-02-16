package com.jcb1973.marginalia.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jcb1973.marginalia.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): TagEntity?

    @Query("SELECT * FROM tags ORDER BY displayName ASC")
    fun getAll(): Flow<List<TagEntity>>

    @Query(
        """
        SELECT tags.* FROM tags
        INNER JOIN book_tag_cross_ref ON tags.id = book_tag_cross_ref.tagId
        WHERE book_tag_cross_ref.bookId = :bookId
        ORDER BY tags.displayName ASC
        """
    )
    fun getForBook(bookId: Long): Flow<List<TagEntity>>

    @Query(
        """
        SELECT tags.* FROM tags
        INNER JOIN book_tag_cross_ref ON tags.id = book_tag_cross_ref.tagId
        WHERE book_tag_cross_ref.bookId = :bookId
        ORDER BY tags.displayName ASC
        """
    )
    suspend fun getForBookOnce(bookId: Long): List<TagEntity>

    @Query(
        """
        SELECT tags.*, COUNT(book_tag_cross_ref.bookId) as bookCount
        FROM tags
        LEFT JOIN book_tag_cross_ref ON tags.id = book_tag_cross_ref.tagId
        GROUP BY tags.id
        ORDER BY tags.displayName ASC
        """
    )
    fun getAllWithBookCount(): Flow<List<TagWithBookCount>>
}

data class TagWithBookCount(
    val id: Long,
    val name: String,
    val displayName: String,
    val colorName: String?,
    val bookCount: Int
)
