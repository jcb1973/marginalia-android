package com.jcb1973.marginalia.domain.error

sealed interface BookLookupError {
    data object InvalidIsbn : BookLookupError
    data class NetworkError(val cause: Throwable) : BookLookupError
    data object NotFound : BookLookupError
    data class DecodingError(val cause: Throwable) : BookLookupError
    data object RateLimited : BookLookupError
}
