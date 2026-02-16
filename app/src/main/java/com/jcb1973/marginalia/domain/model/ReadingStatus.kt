package com.jcb1973.marginalia.domain.model

enum class ReadingStatus(val value: String, val displayName: String) {
    TO_READ("toRead", "To Read"),
    READING("reading", "Reading"),
    READ("read", "Read");

    companion object {
        fun fromValue(value: String): ReadingStatus =
            entries.firstOrNull { it.value == value } ?: TO_READ
    }
}
