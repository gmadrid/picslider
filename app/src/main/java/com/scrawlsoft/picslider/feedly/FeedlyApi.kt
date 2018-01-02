package com.scrawlsoft.picslider.feedly

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

/**
 * A fetcher for Feedly data.
 * Nomenclature is Feedly specific (Categories, Entries).
 *
 * All calls are synchronous.
 */
class FeedlyFetcher() {

    data class FeedlyCategory(val id: String, val label: String, val description: String?)
    data class FeedlyEntryIdsResponse(val ids: List<String>, val continuation: String?)

    data class FeedlyEntry(
            val id: String,
            val url: String?,
            internal val visual: FeedlyEntryVisual?,
            internal val summary: FeedlyEntrySummary?)

    data class FeedlyEntryVisual(val url: String, val width: Int, val height: Int, val contentType: String)
    data class FeedlyEntrySummary(val content: String)

    data class BadResultException(val code: Int) : Exception()
    data class MissingBodyException(val value: Int = 0) : Exception()

    private val service: FeedlyApi

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl(defaultBaseUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        service = retrofit.create(FeedlyApi::class.java)
    }

    private fun <T : Any, E : Exception> returnResult(resp: Response<T>): Result<T, E> {
        if (resp.code() != 200) {
            throw BadResultException(resp.code())
        }
        val body = resp.body()
        return if (body == null) {
            throw MissingBodyException()
        } else {
            Result.Success(body)
        }
    }

    fun fetchCategories(): Result<List<FeedlyCategory>, Exception> {
        val resp = service.categories().execute()
        return returnResult(resp)
    }

    fun fetchEntryIds(streamId: String): Result<FeedlyEntryIdsResponse, Exception> {
        val resp = service.entriesForStream(streamId).execute()
        return returnResult(resp)
    }

    private fun findUrlInContent(content: String): String? {
        // <img[^>]* src="()" >
        val re = Regex("<img\\s+[^>]*src=\\\"([^\\\"]*)\"")
        val f = re.find(content)
        if (f == null) {
            println("NOREFOUND")
        } else {
            println("REMATCH: ${f.groupValues.size} ${f.groupValues[1]}")
            if (f.groupValues.size > 1) {
                return f.groupValues[1]
            }
        }
        return null
    }

    private fun extractUrl(entry: FeedlyEntry): FeedlyEntry {
        var url = entry.visual?.url
        if (url == null) {
            url = findUrlInContent(entry.summary?.content ?: "")
            println("GOT ONE: $url")
        }

        return FeedlyEntry(entry.id, url, entry.visual, entry.summary)
    }

    private fun extractUrls(entries: List<FeedlyEntry>): List<FeedlyEntry> {
        return entries.map { extractUrl(it) }
    }

    fun fetchEntriesForIds(entryIds: List<String>): Result<List<FeedlyEntry>, Exception> {
        val resp = service.entriesForIds(entryIds).execute()
        val result: Result<List<FeedlyEntry>, Exception> = returnResult(resp)

        val processed = result.map { extractUrls(it) }
        // Since we are not yet processing the summary content,
        // find the first one with a visual.url.
        // Also filter out entries with a visual url of "none".
        return processed.map {
            it.filter { it.url != null && it.url != "none" }
        }
    }
}

private val defaultBaseUrl = "https://cloud.feedly.com/"

// TODO: make the authentication non-const

private const val AUTH_HEADER = "Authorization: OAuth Ay_DjsKr3hvAlo8Iki1JLIim01bIgQAFmV_Mhc25FXX2anbywwMe03dv1tF6160_Ouajc-1yHH4GZH5QZ_IRhq-h2aM0gAucae3K-mPXNXv_YzlNDTalg52DiOgnO3jvBtFlhvM1ix52GZ-n2X4qPGiqbC0RuwuiMx7wbaD338rdIWfPrAP8jXD30BUG6pALcnkw-86UlLnutPvq2YO4DBrZTm3DlnrjbdFV7qqaFdxOSeCxwmhzJrzoGZ7Xdg:feedlydev"

private interface FeedlyApi {
    @Headers(AUTH_HEADER)
    @GET("/v3/categories")
    fun categories(): Call<List<FeedlyFetcher.FeedlyCategory>>

    @Headers(AUTH_HEADER)
    @GET("/v3/streams/ids")
    fun entriesForStream(@Query("streamId") streamId: String)
            : Call<FeedlyFetcher.FeedlyEntryIdsResponse>

    @POST("/v3/entries/.mget")
    fun entriesForIds(@Body body: List<String>): Call<List<FeedlyFetcher.FeedlyEntry>>
}
