package com.jcb1973.marginalia.domain.model

data class SearchResult(
    val book: Book,
    val matchReasons: Set<MatchReason>
)

enum class MatchReason(val displayName: String) {
    TITLE("Title"),
    AUTHOR("Author"),
    NOTE("Notes"),
    QUOTE("Quotes"),
    TAG("Tags"),
    ISBN("ISBN")
}
