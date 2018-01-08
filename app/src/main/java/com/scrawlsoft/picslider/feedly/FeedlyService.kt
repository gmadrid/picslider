package com.scrawlsoft.picslider.feedly

import com.scrawlsoft.picslider.ImageService
import io.reactivex.Single
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

/**
 * Convenience wrapper around FeedlyApi.
 * Returned types are raw API data objects.
 * All streams returned will be subscribed on Schedulers.io().
 */
class FeedlyService @Inject constructor(private val feedlyApi: FeedlyApi,
                                        @Named("feedlyUserToken") private val feedlyUserToken: String)
    : ImageService {

    private val authHeader by lazy { "OAuth $feedlyUserToken" }

    override val categories: Single<List<ImageService.Category>> =
            feedlyApi.categories(authHeader)
                    .map { categories ->
                        categories.map {
                            ImageService.Category(it.id, it.label, it.description)
                        }
                    }

    override fun getEntryIdsForCategory(categoryId: String): Single<Pair<String?, List<String>>> =
            feedlyApi.entryIdsForStream(authHeader, categoryId)
                    .map { Pair(it.continuation, it.ids) }

    override fun getEntriesForIds(entryIds: List<String>): Single<List<ImageService.Entry>> {
        return feedlyApi.entriesForIds(entryIds)
                .map {
                    // Convert from JSON rep to FeedlyApiEntry, removing entries without
                    // uris at the same time.
                    it.fold(emptyList<ImageService.Entry>()) { acc, jsonEntry ->
                        val uri = FeedlyService.extractUrl(jsonEntry)
                        if (uri != null) acc + ImageService.Entry(jsonEntry.id, uri) else acc
                    }
                }
    }

//    fun markAsRead(entryId: String): Completable = markAsRead(listOf(entryId))

//    fun markAsRead(entryIds: List<String>): Completable =
//            feedlyApi.mark(authHeader,
//                    FeedlyApiMarkerRequest("markAsRead", "entries", entryIds))

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

        private fun extractUrl(entry: FeedlyApiJSONEntry): URL? {
            val url = entry.visual?.url ?: findUrlInContent(entry.summary?.content ?: "")
            return url?.let {
                try {
                    URL(url)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}

