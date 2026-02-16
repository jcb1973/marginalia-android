package com.jcb1973.marginalia.data.service

object IsbnValidator {

    fun normalize(raw: String): String? {
        val stripped = raw.filter { it.isDigit() || it == 'X' || it == 'x' }
        return when {
            isValidIsbn13(stripped) -> stripped
            isValidIsbn10(stripped) -> convertToIsbn13(stripped)
            else -> null
        }
    }

    fun isValid(raw: String): Boolean {
        val stripped = raw.filter { it.isDigit() || it == 'X' || it == 'x' }
        return isValidIsbn13(stripped) || isValidIsbn10(stripped)
    }

    private fun isValidIsbn13(isbn: String): Boolean {
        if (isbn.length != 13 || !isbn.all { it.isDigit() }) return false
        val sum = isbn.mapIndexed { i, c ->
            val digit = c.digitToInt()
            if (i % 2 == 0) digit else digit * 3
        }.sum()
        return sum % 10 == 0
    }

    private fun isValidIsbn10(isbn: String): Boolean {
        if (isbn.length != 10) return false
        if (!isbn.substring(0, 9).all { it.isDigit() }) return false
        val lastChar = isbn.last().uppercaseChar()
        if (!lastChar.isDigit() && lastChar != 'X') return false

        val sum = isbn.mapIndexed { i, c ->
            val value = when {
                c.isDigit() -> c.digitToInt()
                c.uppercaseChar() == 'X' -> 10
                else -> return false
            }
            value * (10 - i)
        }.sum()
        return sum % 11 == 0
    }

    private fun convertToIsbn13(isbn10: String): String {
        val base = "978" + isbn10.substring(0, 9)
        val sum = base.mapIndexed { i, c ->
            val digit = c.digitToInt()
            if (i % 2 == 0) digit else digit * 3
        }.sum()
        val checkDigit = (10 - (sum % 10)) % 10
        return base + checkDigit
    }
}
