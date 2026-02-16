package com.jcb1973.marginalia.domain.model

enum class SortOrder(val displayName: String) {
    TITLE_ASC("Title A–Z"),
    TITLE_DESC("Title Z–A"),
    DATE_ADDED_DESC("Newest First"),
    DATE_ADDED_ASC("Oldest First"),
    RATING_DESC("Highest Rated"),
    RATING_ASC("Lowest Rated"),
    AUTHOR_ASC("Author A–Z"),
    AUTHOR_DESC("Author Z–A")
}
