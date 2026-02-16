package com.jcb1973.marginalia.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Library : Screen("library?status={status}") {
        fun createRoute(status: String? = null): String =
            if (status != null) "library?status=$status" else "library"
    }
    data object BookDetail : Screen("book/{bookId}") {
        fun createRoute(bookId: Long): String = "book/$bookId"
    }
    data object BookForm : Screen("bookform?bookId={bookId}") {
        fun createRoute(bookId: Long? = null): String =
            if (bookId != null) "bookform?bookId=$bookId" else "bookform"
    }
    data object AllNotes : Screen("notes/{bookId}") {
        fun createRoute(bookId: Long): String = "notes/$bookId"
    }
    data object NoteEditor : Screen("note-editor/{bookId}?noteId={noteId}") {
        fun createRoute(bookId: Long, noteId: Long? = null): String =
            if (noteId != null) "note-editor/$bookId?noteId=$noteId" else "note-editor/$bookId"
    }
    data object AllQuotes : Screen("quotes/{bookId}") {
        fun createRoute(bookId: Long): String = "quotes/$bookId"
    }
    data object QuoteEditor : Screen("quote-editor/{bookId}?quoteId={quoteId}") {
        fun createRoute(bookId: Long, quoteId: Long? = null): String =
            if (quoteId != null) "quote-editor/$bookId?quoteId=$quoteId" else "quote-editor/$bookId"
    }
    data object Scanner : Screen("scanner")
    data object ManageTags : Screen("manage-tags")
}
