package com.jcb1973.marginalia.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jcb1973.marginalia.data.dao.AuthorDao
import com.jcb1973.marginalia.data.dao.BookDao
import com.jcb1973.marginalia.data.dao.NoteDao
import com.jcb1973.marginalia.data.dao.QuoteDao
import com.jcb1973.marginalia.data.dao.TagDao
import com.jcb1973.marginalia.data.entity.AuthorEntity
import com.jcb1973.marginalia.data.entity.BookAuthorCrossRef
import com.jcb1973.marginalia.data.entity.BookEntity
import com.jcb1973.marginalia.data.entity.BookTagCrossRef
import com.jcb1973.marginalia.data.entity.NoteEntity
import com.jcb1973.marginalia.data.entity.QuoteEntity
import com.jcb1973.marginalia.data.entity.TagEntity

@Database(
    entities = [
        BookEntity::class,
        AuthorEntity::class,
        NoteEntity::class,
        QuoteEntity::class,
        TagEntity::class,
        BookAuthorCrossRef::class,
        BookTagCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
abstract class MarginaliaDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun authorDao(): AuthorDao
    abstract fun noteDao(): NoteDao
    abstract fun quoteDao(): QuoteDao
    abstract fun tagDao(): TagDao
}
