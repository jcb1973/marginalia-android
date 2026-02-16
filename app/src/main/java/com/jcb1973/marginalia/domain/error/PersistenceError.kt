package com.jcb1973.marginalia.domain.error

sealed interface PersistenceError {
    data class SaveFailed(val cause: Throwable) : PersistenceError
    data class DeleteFailed(val cause: Throwable) : PersistenceError
    data class QueryFailed(val cause: Throwable) : PersistenceError
}
