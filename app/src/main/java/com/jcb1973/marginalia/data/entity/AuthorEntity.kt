package com.jcb1973.marginalia.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "authors",
    indices = [Index("name")]
)
data class AuthorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
