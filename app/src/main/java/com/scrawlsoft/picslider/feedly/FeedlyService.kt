package com.scrawlsoft.picslider.feedly

import android.content.res.Resources
import com.scrawlsoft.picslider.R
import com.scrawlsoft.picslider.utils.picasso
import io.reactivex.Completable
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
class FeedlyService(userToken: String) {
    private val authHeader = "OAuth $userToken"

    private val api: FeedlyApi = Retrofit.Builder()
            .baseUrl(defaultBaseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(FeedlyApi::class.java)

    fun getCategories(): Observable<List<FeedlyApiCategory>> {
        return api.categories(authHeader)
    }

    fun getEntryIdsForCategory(streamId: String): Observable<FeedlyApiEntryIdsResponse> {
        return api.entryIdsForStream(authHeader, streamId)
    }

    fun getEntriesForIds(entryIds: List<String>): Observable<List<FeedlyApiEntry>> {
        return api.entriesForIds(entryIds)
                .map {
                    it.map {
                        val entry = FeedlyService.extractUrl(it)
                        if (entry.url != null) {
                            // Pre-fetch the image data.
                            // TODO: move this after the filter call.
                            picasso().load(entry.url).fetch()
                            // TODO: consider caching in reverse order.
                        }
                        entry
                    }.filter { it.url != null }
                }
    }

    fun markAsRead(entryId: String): Completable {
        return markAsRead(listOf(entryId))
    }

    fun markAsRead(entryIds: List<String>): Completable {
        val req = FeedlyApiMarkerRequest("markAsRead", "entries", entryIds)
        return api.mark(authHeader, req)
    }

    companion object {
        fun readTokenFromResources(resources: Resources): String {
            return resources.getString(R.string.user_token)
        }

        private fun findUrlInContent(content: String): String? {
            // Look for <img> tags in file with src attrs.
            // BUG: src must be on same line with img tag.
            //
            // The following URL works:
            // <img[^>]* src="()" >
            //
            // Also, note the spurious "redundant character escape" warnings.
            val re = Regex("<img\\s+[^>]*src=\\\"([^\\\"]*)\"")
            val f = re.find(content)
            if (f == null) {
                println("NOREFOUND")
            } else {
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
            }

            return FeedlyApiEntry(entry.id, url, entry.visual, entry.summary)
        }
    }
}

