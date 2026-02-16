package com.jcb1973.marginalia.domain.repository

import com.jcb1973.marginalia.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotesForBook(bookId: Long): Flow<List<Note>>
    suspend fun getNotesForBookOnce(bookId: Long): List<Note>
    fun searchNotesForBook(bookId: Long, query: String): Flow<List<Note>>
    suspend fun searchNotesForBookOnce(bookId: Long, query: String): List<Note>
    fun searchNotes(query: String): Flow<List<Note>>
    fun getRecentNotes(limit: Int): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    suspend fun saveNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Long)
}
