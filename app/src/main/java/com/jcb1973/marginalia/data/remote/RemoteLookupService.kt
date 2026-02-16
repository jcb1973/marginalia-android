package com.jcb1973.marginalia.data.remote

import com.jcb1973.marginalia.data.service.IsbnValidator
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteLookupService @Inject constructor(
    private val httpClient: HttpClient,
    private val json: Json
) : BookLookupService {

    override suspend fun lookupByIsbn(isbn: String): Result<LookupResult> {
        val normalized = IsbnValidator.normalize(isbn)
            ?: return Result.failure(IllegalArgumentException("Invalid ISBN"))

        return tryOpenLibrary(normalized)
            .recoverCatching { tryGoogleBooks(normalized).getOrThrow() }
    }

    private suspend fun tryOpenLibrary(isbn: String): Result<LookupResult> {
        return try {
            val response: HttpResponse = httpClient.get(
                "https://openlibrary.org/api/books?bibkeys=ISBN:$isbn&format=json&jscmd=data"
            )
            when (response.status) {
                HttpStatusCode.TooManyRequests -> return Result.failure(RuntimeException("Rate limited"))
                HttpStatusCode.OK -> { /* continue */ }
                else -> return Result.failure(RuntimeException("HTTP ${response.status}"))
            }
            val bodyText: String = response.body()
            val jsonObject = json.decodeFromString<JsonObject>(bodyText)
            val bookData = jsonObject["ISBN:$isbn"]
                ?: return Result.failure(NoSuchElementException("Not found on Open Library"))
            val data = json.decodeFromJsonElement<OpenLibraryBookData>(bookData)

            Result.success(
                LookupResult(
                    title = data.title ?: return Result.failure(NoSuchElementException("No title")),
                    authors = data.authors?.mapNotNull { it.name } ?: emptyList(),
                    isbn = isbn,
                    coverImageUrl = data.cover?.large ?: data.cover?.medium ?: data.cover?.small,
                    publisher = data.publishers?.firstOrNull()?.name,
                    publishedDate = data.publishDate,
                    description = data.notes,
                    pageCount = data.numberOfPages
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun tryGoogleBooks(isbn: String): Result<LookupResult> {
        return try {
            val response: HttpResponse = httpClient.get(
                "https://www.googleapis.com/books/v1/volumes?q=isbn:$isbn"
            )
            when (response.status) {
                HttpStatusCode.TooManyRequests -> return Result.failure(RuntimeException("Rate limited"))
                HttpStatusCode.OK -> { /* continue */ }
                else -> return Result.failure(RuntimeException("HTTP ${response.status}"))
            }
            val bodyText: String = response.body()
            val gbResponse = json.decodeFromString<GoogleBooksResponse>(bodyText)
            val volumeInfo = gbResponse.items?.firstOrNull()?.volumeInfo
                ?: return Result.failure(NoSuchElementException("Not found on Google Books"))

            val coverUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
                ?: volumeInfo.imageLinks?.smallThumbnail?.replace("http://", "https://")

            Result.success(
                LookupResult(
                    title = volumeInfo.title
                        ?: return Result.failure(NoSuchElementException("No title")),
                    authors = volumeInfo.authors ?: emptyList(),
                    isbn = isbn,
                    coverImageUrl = coverUrl,
                    publisher = volumeInfo.publisher,
                    publishedDate = volumeInfo.publishedDate,
                    description = volumeInfo.description,
                    pageCount = volumeInfo.pageCount
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
