package com.scrawlsoft.picslider.feedly

import com.scrawlsoft.picslider.base.CategoryId
import com.scrawlsoft.picslider.base.Continuation
import com.scrawlsoft.picslider.base.EntryId
import com.scrawlsoft.picslider.base.ImageService
import com.scrawlsoft.picslider.base.KeyStore
import io.reactivex.Completable
import io.reactivex.Single
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

/**
 * An ImageService for the FeedlyApi.
 * All streams returned will be subscribed on Schedulers.io().
 */
class FeedlyService @Inject constructor(private val feedlyApi: FeedlyApi,
                                        private val feedlyKeyStore: KeyStore)
//                                        @Named("feedlyUserToken") private val feedlyUserToken: String)
    : ImageService {

    private val authHeader by lazy { "OAuth ${feedlyKeyStore.token}" }

    override val categories: Single<List<ImageService.Category>> =
            feedlyApi.categories(authHeader)
                    .map { categories ->
                        categories.map {
                            ImageService.Category(it.id, it.label, it.description)
                        }
                    }

    override fun getEntryIdsForCategory(categoryId: CategoryId, continuation: Continuation)
            : Single<ImageService.EntryIdsResponse> =
            feedlyApi.entryIdsForStream(authHeader, categoryId, continuation = continuation.apiString)
                    .map { resp ->
                        ImageService.EntryIdsResponse(
                                Continuation.fromString(resp.continuation), resp.ids)
                    }

    override fun getEntriesForIds(entryIds: List<EntryId>): Single<List<ImageService.Entry>> {
        return feedlyApi.entriesForIds(entryIds)
                .map {
                    it.mapNotNull { jsonEntry ->
                        // Convert from JSON rep to FeedlyApiEntry,
                        // removing entries without uris at the same time.
                        FeedlyService.extractUrl(jsonEntry)?.let { url ->
                            ImageService.Entry(jsonEntry.id, url)
                        }
                    }
                }
    }

    override fun markAsRead(entryIds: List<String>): Completable =
            feedlyApi.mark(authHeader,
                    FeedlyApiMarkerRequest("markAsRead", "entries", entryIds))

    companion object {
        private fun findUrlInContent(content: String): String? {
            // Look for <img> tags in file with src attrs.
            // BUG: src must be on same line with img tag.
            //
            // The following regex works:
            // <img[^>]* src="()" >
            //
            // Also, note the spurious "redundant character escape" warnings.
            val re = Regex("""<img\s+[^>]*src="([^"]*)"""")
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

