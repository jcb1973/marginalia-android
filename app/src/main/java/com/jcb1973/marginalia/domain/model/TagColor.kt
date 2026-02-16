package com.jcb1973.marginalia.domain.model

import androidx.compose.ui.graphics.Color

enum class TagColor(val displayName: String, val color: Color) {
    RED("Red", Color(0xFFE53935)),
    ORANGE("Orange", Color(0xFFFB8C00)),
    YELLOW("Yellow", Color(0xFFFDD835)),
    GREEN("Green", Color(0xFF43A047)),
    MINT("Mint", Color(0xFF26A69A)),
    TEAL("Teal", Color(0xFF00897B)),
    BLUE("Blue", Color(0xFF1E88E5)),
    PURPLE("Purple", Color(0xFF8E24AA)),
    PINK("Pink", Color(0xFFD81B60)),
    BROWN("Brown", Color(0xFF6D4C41));

    companion object {
        fun fromName(name: String?): TagColor? =
            if (name == null) null else entries.firstOrNull { it.name == name }
    }
}
