package com.jcb1973.marginalia.data.repository

import com.jcb1973.marginalia.data.dao.TagDao
import com.jcb1973.marginalia.data.entity.TagEntity
import com.jcb1973.marginalia.domain.model.Tag
import com.jcb1973.marginalia.domain.model.TagColor
import com.jcb1973.marginalia.domain.repository.TagRepository
import com.jcb1973.marginalia.domain.repository.TagWithCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override fun getAllTags(): Flow<List<Tag>> =
        tagDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getTagsForBook(bookId: Long): Flow<List<Tag>> =
        tagDao.getForBook(bookId).map { entities -> entities.map { it.toDomain() } }

    override fun getAllTagsWithBookCount(): Flow<List<TagWithCount>> =
        tagDao.getAllWithBookCount().map { list ->
            list.map { twc ->
                TagWithCount(
                    tag = Tag(
                        id = twc.id,
                        name = twc.name,
                        displayName = twc.displayName,
                        color = TagColor.fromName(twc.colorName)
                    ),
                    bookCount = twc.bookCount
                )
            }
        }

    override suspend fun getTagById(id: Long): Tag? =
        tagDao.getById(id)?.toDomain()

    override suspend fun getTagByName(name: String): Tag? =
        tagDao.getByName(name)?.toDomain()

    override suspend fun saveTag(tag: Tag): Long =
        tagDao.insert(tag.toEntity())

    override suspend fun updateTag(tag: Tag) =
        tagDao.update(tag.toEntity())

    override suspend fun deleteTag(tag: Tag) =
        tagDao.delete(tag.toEntity())

    private fun TagEntity.toDomain(): Tag = Tag(
        id = id,
        name = name,
        displayName = displayName,
        color = TagColor.fromName(colorName)
    )

    private fun Tag.toEntity(): TagEntity = TagEntity(
        id = id,
        name = name,
        displayName = displayName,
        colorName = color?.name
    )
}
