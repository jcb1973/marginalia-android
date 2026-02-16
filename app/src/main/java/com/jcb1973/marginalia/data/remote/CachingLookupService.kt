package com.jcb1973.marginalia.data.remote

import android.content.Context
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachingLookupService @Inject constructor(
    private val remote: RemoteLookupService,
    @param:ApplicationContext private val context: Context,
    private val json: Json
) : BookLookupService {

    private val memoryCache = LruCache<String, LookupResult>(50)

    override suspend fun lookupByIsbn(isbn: String): Result<LookupResult> {
        memoryCache.get(isbn)?.let { return Result.success(it) }

        readDiskCache(isbn)?.let { result ->
            memoryCache.put(isbn, result)
            return Result.success(result)
        }

        return remote.lookupByIsbn(isbn).also { result ->
            result.getOrNull()?.let { lookup ->
                memoryCache.put(isbn, lookup)
                writeDiskCache(isbn, lookup)
            }
        }
    }

    private fun getCacheDir(): File {
        val dir = File(context.cacheDir, "isbn_cache")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun readDiskCache(isbn: String): LookupResult? {
        return try {
            val file = File(getCacheDir(), "$isbn.json")
            if (!file.exists()) return null
            val cached = json.decodeFromString<CachedLookup>(file.readText())
            cached.toLookupResult()
        } catch (_: Exception) {
            null
        }
    }

    private fun writeDiskCache(isbn: String, result: LookupResult) {
        try {
            val file = File(getCacheDir(), "$isbn.json")
            file.writeText(json.encodeToString(CachedLookup.fromLookupResult(result)))
        } catch (_: Exception) {
            // Cache write failure is non-critical
        }
    }
}

@kotlinx.serialization.Serializable
private data class CachedLookup(
    val title: String,
    val authors: List<String>,
    val isbn: String,
    val coverImageUrl: String?,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val pageCount: Int?
) {
    fun toLookupResult() = LookupResult(
        title = title,
        authors = authors,
        isbn = isbn,
        coverImageUrl = coverImageUrl,
        publisher = publisher,
        publishedDate = publishedDate,
        description = description,
        pageCount = pageCount
    )

    companion object {
        fun fromLookupResult(r: LookupResult) = CachedLookup(
            title = r.title,
            authors = r.authors,
            isbn = r.isbn,
            coverImageUrl = r.coverImageUrl,
            publisher = r.publisher,
            publishedDate = r.publishedDate,
            description = r.description,
            pageCount = r.pageCount
        )
    }
}
