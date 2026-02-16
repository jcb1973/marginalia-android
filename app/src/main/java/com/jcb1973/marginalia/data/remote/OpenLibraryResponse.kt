package com.jcb1973.marginalia.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenLibraryBookData(
    val title: String? = null,
    val authors: List<OpenLibraryAuthor>? = null,
    val publishers: List<OpenLibraryPublisher>? = null,
    @SerialName("publish_date") val publishDate: String? = null,
    @SerialName("number_of_pages") val numberOfPages: Int? = null,
    val cover: OpenLibraryCover? = null,
    val notes: String? = null
)

@Serializable
data class OpenLibraryAuthor(
    val name: String? = null
)

@Serializable
data class OpenLibraryPublisher(
    val name: String? = null
)

@Serializable
data class OpenLibraryCover(
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null
)
