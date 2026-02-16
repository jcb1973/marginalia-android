package com.jcb1973.marginalia.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class GoogleBooksResponse(
    val totalItems: Int = 0,
    val items: List<GoogleBooksItem>? = null
)

@Serializable
data class GoogleBooksItem(
    val volumeInfo: GoogleBooksVolumeInfo? = null
)

@Serializable
data class GoogleBooksVolumeInfo(
    val title: String? = null,
    val authors: List<String>? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val description: String? = null,
    val pageCount: Int? = null,
    val imageLinks: GoogleBooksImageLinks? = null,
    val industryIdentifiers: List<GoogleBooksIdentifier>? = null
)

@Serializable
data class GoogleBooksImageLinks(
    val smallThumbnail: String? = null,
    val thumbnail: String? = null
)

@Serializable
data class GoogleBooksIdentifier(
    val type: String? = null,
    val identifier: String? = null
)
