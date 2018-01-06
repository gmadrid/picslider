package com.scrawlsoft.picslider.feedly

import com.squareup.picasso.Picasso
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

/**
 * Convenience wrapper around FeedlyApi.
 * Returned types are raw API data objects.
 * All streams returned will be subscribed on Schedulers.io().
 */
class FeedlyService @Inject constructor() {
    @Inject lateinit var feedlyApi: FeedlyApi
    @Inject lateinit var picasso: Picasso
    @Inject
    @field:Named("feedlyUserToken") lateinit var feedlyUserToken: String

    private val authHeader by lazy { "OAuth $feedlyUserToken" }

    fun getCategories(): Single<List<FeedlyApiCategory>> {
        return feedlyApi.categories(authHeader)
    }

    fun getEntryIdsForCategory(streamId: String): Single<FeedlyApiEntryIdsResponse> {
        return feedlyApi.entryIdsForStream(authHeader, streamId)
    }

    fun getEntriesForIds(entryIds: List<String>): Single<List<FeedlyApiEntry>> {
        return feedlyApi.entriesForIds(entryIds)
                .map {
                    it.map {
                        // TODO: Use that cool apply() thing here.
                        val entry = FeedlyService.extractUrl(it)
                        // Pre-fetch the image data.
                        // TODO: move this after the filter call.
                        // TODO: consider caching in reverse order.
                        if (entry.url != null) picasso.load(entry.url).fetch()
                        entry
                    }.filter { it.url != null }
                }
    }

    fun markAsRead(entryId: String): Completable {
        return markAsRead(listOf(entryId))
    }

    fun markAsRead(entryIds: List<String>): Completable {
        val req = FeedlyApiMarkerRequest("markAsRead", "entries", entryIds)
        return feedlyApi.mark(authHeader, req)
    }

    companion object {
        private fun findUrlInContent(content: String): String? {
            // Look for <img> tags in file with src attrs.
            // BUG: src must be on same line with img tag.
            //
            // The following regex works:
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

