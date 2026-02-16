package com.jcb1973.marginalia.domain.model

data class Book(
    val id: Long = 0,
    val title: String,
    val isbn: String? = null,
    val coverImageUrl: String? = null,
    val coverImagePath: String? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val description: String? = null,
    val pageCount: Int? = null,
    val status: ReadingStatus = ReadingStatus.TO_READ,
    val rating: Int? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateStarted: Long? = null,
    val dateFinished: Long? = null,
    val authors: List<String> = emptyList(),
    val tags: List<Tag> = emptyList()
)

data class Tag(
    val id: Long = 0,
    val name: String,
    val displayName: String,
    val color: TagColor? = null
)

data class Note(
    val id: Long = 0,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val bookId: Long
)

data class Quote(
    val id: Long = 0,
    val text: String,
    val comment: String? = null,
    val pageNumber: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val bookId: Long
)
