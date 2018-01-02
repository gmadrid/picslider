package com.scrawlsoft.picslider.feedly

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

private val defaultBaseUrl = "https://cloud.feedly.com/"

/**
 * Convenience wrapper around FeedlyApi.
 * Returned types are raw API data objects.
 * All streams returned will be subscribed on Schedulers.io().
 */
class FeedlyService() {
    @Suppress("unused")
    private val user = DEV_USER
    private val token = DEV_TOKEN
    private val authHeader = "OAuth $token"

    private val api: FeedlyApi = Retrofit.Builder()
            .baseUrl(defaultBaseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(FeedlyApi::class.java)

    fun getCategories(): Observable<List<FeedlyApiCategory>> {
        return api.categories(authHeader)
                .subscribeOn(Schedulers.io())
    }

    fun getEntryIdsForCategory(streamId: String): Observable<FeedlyApiEntryIdsResponse> {
        return api.entryIdsForStream(authHeader, streamId)
                .subscribeOn(Schedulers.io())
    }

    fun getEntriesForIds(entryIds: List<String>): Observable<List<FeedlyApiEntry>> {
        return api.entriesForIds(entryIds)
                .map { it.map { FeedlyService.extractUrl(it) } }
                .subscribeOn(Schedulers.io())
    }


    companion object {
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

        private fun extractUrl(entry: FeedlyApiEntry): FeedlyApiEntry {
            var url = entry.visual?.url
            if (url == null) {
                url = findUrlInContent(entry.summary?.content ?: "")
                println("GOT ONE: $url")
            }

            return FeedlyApiEntry(entry.id, url, entry.visual, entry.summary)
        }

    }

}

