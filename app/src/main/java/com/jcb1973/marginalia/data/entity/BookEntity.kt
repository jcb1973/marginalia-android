package com.jcb1973.marginalia.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    indices = [
        Index("title"),
        Index("status"),
        Index("dateAdded")
    ]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isbn: String? = null,
    val coverImageUrl: String? = null,
    val coverImagePath: String? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val description: String? = null,
    val pageCount: Int? = null,
    val status: String = "toRead",
    val rating: Int? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateStarted: Long? = null,
    val dateFinished: Long? = null
)
