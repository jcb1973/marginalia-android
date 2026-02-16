package com.jcb1973.marginalia.data.remote

import com.jcb1973.marginalia.domain.error.BookLookupError

data class LookupResult(
    val title: String,
    val authors: List<String>,
    val isbn: String,
    val coverImageUrl: String?,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val pageCount: Int?
)

interface BookLookupService {
    suspend fun lookupByIsbn(isbn: String): Result<LookupResult>
}

fun BookLookupError.toResultFailure(): Result<LookupResult> = Result.failure(
    when (this) {
        is BookLookupError.InvalidIsbn -> IllegalArgumentException("Invalid ISBN")
        is BookLookupError.NetworkError -> cause
        is BookLookupError.NotFound -> NoSuchElementException("Book not found")
        is BookLookupError.DecodingError -> cause
        is BookLookupError.RateLimited -> RuntimeException("Rate limited")
    }
)
