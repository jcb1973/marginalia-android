package com.jcb1973.marginalia.data.repository

import com.jcb1973.marginalia.data.dao.NoteDao
import com.jcb1973.marginalia.data.entity.NoteEntity
import com.jcb1973.marginalia.domain.model.Note
import com.jcb1973.marginalia.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getNotesForBook(bookId: Long): Flow<List<Note>> =
        noteDao.getForBook(bookId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getNotesForBookOnce(bookId: Long): List<Note> =
        noteDao.getForBookOnce(bookId).map { it.toDomain() }

    override fun searchNotesForBook(bookId: Long, query: String): Flow<List<Note>> =
        noteDao.searchForBook(bookId, query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun searchNotesForBookOnce(bookId: Long, query: String): List<Note> =
        noteDao.searchForBookOnce(bookId, query).map { it.toDomain() }

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.search(query).map { entities -> entities.map { it.toDomain() } }

    override fun getRecentNotes(limit: Int): Flow<List<Note>> =
        noteDao.getRecent(limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getNoteById(id: Long): Note? =
        noteDao.getById(id)?.toDomain()

    override suspend fun saveNote(note: Note): Long =
        noteDao.insert(note.toEntity())

    override suspend fun updateNote(note: Note) =
        noteDao.update(note.toEntity())

    override suspend fun deleteNote(id: Long) =
        noteDao.deleteById(id)

    private fun NoteEntity.toDomain(): Note = Note(
        id = id,
        content = content,
        createdAt = createdAt,
        bookId = bookId
    )

    private fun Note.toEntity(): NoteEntity = NoteEntity(
        id = id,
        content = content,
        createdAt = createdAt,
        bookId = bookId
    )
}
