package com.jcb1973.marginalia.di

import com.jcb1973.marginalia.data.repository.BookRepositoryImpl
import com.jcb1973.marginalia.data.repository.NoteRepositoryImpl
import com.jcb1973.marginalia.data.repository.QuoteRepositoryImpl
import com.jcb1973.marginalia.data.repository.TagRepositoryImpl
import com.jcb1973.marginalia.domain.repository.BookRepository
import com.jcb1973.marginalia.domain.repository.NoteRepository
import com.jcb1973.marginalia.domain.repository.QuoteRepository
import com.jcb1973.marginalia.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindQuoteRepository(impl: QuoteRepositoryImpl): QuoteRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository
}
