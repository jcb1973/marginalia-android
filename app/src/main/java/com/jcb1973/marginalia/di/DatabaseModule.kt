package com.jcb1973.marginalia.di

import android.content.Context
import androidx.room.Room
import com.jcb1973.marginalia.data.MarginaliaDatabase
import com.jcb1973.marginalia.data.dao.AuthorDao
import com.jcb1973.marginalia.data.dao.BookDao
import com.jcb1973.marginalia.data.dao.NoteDao
import com.jcb1973.marginalia.data.dao.QuoteDao
import com.jcb1973.marginalia.data.dao.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MarginaliaDatabase {
        return Room.databaseBuilder(
            context,
            MarginaliaDatabase::class.java,
            "marginalia.db"
        ).build()
    }

    @Provides
    fun provideBookDao(db: MarginaliaDatabase): BookDao = db.bookDao()

    @Provides
    fun provideAuthorDao(db: MarginaliaDatabase): AuthorDao = db.authorDao()

    @Provides
    fun provideNoteDao(db: MarginaliaDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideQuoteDao(db: MarginaliaDatabase): QuoteDao = db.quoteDao()

    @Provides
    fun provideTagDao(db: MarginaliaDatabase): TagDao = db.tagDao()
}
