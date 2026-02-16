package com.jcb1973.marginalia.domain.repository

import com.jcb1973.marginalia.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    suspend fun getAllTagsOnce(): List<Tag>
    fun getTagsForBook(bookId: Long): Flow<List<Tag>>
    fun getAllTagsWithBookCount(): Flow<List<TagWithCount>>
    suspend fun getTagById(id: Long): Tag?
    suspend fun getTagByName(name: String): Tag?
    suspend fun saveTag(tag: Tag): Long
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(tag: Tag)
}

data class TagWithCount(
    val tag: Tag,
    val bookCount: Int
)
