@file:Suppress("IllegalIdentifier")

package com.scrawlsoft.picslider

import com.scrawlsoft.picslider.base.NoContinuationToken
import com.scrawlsoft.picslider.feedly.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import junit.framework.Assert.*
import org.junit.Test

class FeedlyServiceTest {
    class FakeApi : FeedlyApi {
        // Functions to help build mock data.
        private fun basicCategory(entryId: String): FeedlyApiCategory =
                FeedlyApiCategory(entryId, "${entryId}Label", "${entryId}Desc")

        private fun basicEntryPair(entryId: String): Pair<String, FeedlyApiJSONEntry> =
                entryId to basicEntry(entryId)

        private fun basicEntry(entryId: String): FeedlyApiJSONEntry =
                FeedlyApiJSONEntry(entryId,
                        null,
                        basicVisual("http://feedly.url/$entryId"),
                        null)

        private fun basicVisual(url: String): FeedlyEntryVisual {
            return FeedlyEntryVisual(url, 100, 100, "context-type")
        }

        private fun fakeSummary(url: String): FeedlyEntrySummary {
            val str = "<p>Some text<p>\n" +
                    "foobar<img src=\"$url\">\n" +
                    "Sometrailingtext"
            return FeedlyEntrySummary(str)
        }

        // Mock data.
        var categoryData: List<FeedlyApiCategory> = (1..4).map {
            basicCategory("category$it")
        }

        // Entry data: categoryId ==> entryId ==> Entry
        private var entriesData: Map<String, Map<String, FeedlyApiJSONEntry>> =
                mapOf("category1" to
                        mapOf(basicEntryPair("e1"),
                                basicEntryPair("e2"),
                                basicEntryPair("e3"),
                                "missingUrl" to FeedlyApiJSONEntry("missingUrl", null, null, null),
                                "contentUrl" to FeedlyApiJSONEntry(
                                        "contentUrl",
                                        null,
                                        null,
                                        fakeSummary("http://foobar.com/contentUrl"))))

        // If this is non-null, then the next API call will throw it.
        var theException: Throwable? = null

        // If this is non-null, then the next entryIdsForStream call will return it.
        var nextContinuation: String? = null

        override fun categories(authHeader: String): Single<List<FeedlyApiCategory>> =
                Single.create<List<FeedlyApiCategory>> { subscriber ->
                    theException?.let { throw it }

                    subscriber.onSuccess(categoryData)
                }

        override fun entryIdsForStream(authHeader: String, streamId: String, unreadOnly: Boolean, count: Int, ranked: String, continuation: String?)
                : Single<FeedlyApiEntryIdsResponse> {
            return Single.create<FeedlyApiEntryIdsResponse> { subscriber ->
                theException?.let { throw it }

                subscriber.onSuccess(FeedlyApiEntryIdsResponse(
                        entriesData[streamId]?.keys?.toList()?.sorted() ?: emptyList(),
                        nextContinuation))
            }
        }

        override fun entriesForIds(entryIds: List<String>): Single<List<FeedlyApiJSONEntry>> {
            return Single.create<List<FeedlyApiJSONEntry>> { subscriber ->
                theException?.let { throw it }

                // There is currently only one category with data.
                val allEntries = entriesData["category1"] ?: emptyMap()

                val entries = entryIds.mapNotNull { allEntries[it] }
                subscriber.onSuccess(entries)
            }
        }

        override fun mark(authHeader: String, body: FeedlyApiMarkerRequest): Completable {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    @Test
    fun `test categories`() {
        val api = FakeApi()
        val feedlyUserToken = "userToken"
        val service = FeedlyService(api, feedlyUserToken)

        val lst = service.categories.blockingGet()
        assertEquals(4, lst.size)
        listOf("category1", "category2", "category3", "category4").forEach { searchId ->
            assertNotNull(lst.find { it.id == searchId })
        }
        assertNull(lst.find { it.id == "NotFound" })
    }

    @Test
    fun `test empty categories`() {
        val api = FakeApi()
        api.categoryData = emptyList()
        val feedlyUserToken = "userToken"
        val service = FeedlyService(api, feedlyUserToken)

        val lst = service.categories.blockingGet()
        assertEquals(0, lst.size)
    }

    @Test
    fun `test categories has error`() {
        val api = FakeApi()
        api.theException = Exception("Category exception")
        val feedlyUserToken = "userToken"
        val service = FeedlyService(api, feedlyUserToken)

        service.categories.subscribeBy(
                onSuccess = { fail("Expected exception") },
                onError = {})
    }

    @Test
    fun `test entry id request with bad category id`() {
        val api = FakeApi()
        val service = FeedlyService(api, "feedlyUserToken")

        val resp = service.getEntryIdsForCategory("invalid", NoContinuationToken)
                .blockingGet()

        assertEquals(NoContinuationToken, resp.continuation)
        assertTrue(resp.ids.isEmpty())
    }

    @Test
    fun `test entry id request with good category id`() {
        val api = FakeApi()
        val service = FeedlyService(api, "feedlyUserToken")

        // Only "category1" has any data.
        val resp = service.getEntryIdsForCategory("category1", NoContinuationToken)
                .blockingGet()

        assertEquals(NoContinuationToken, resp.continuation)
        val expectedIds = listOf("e1", "e2", "e3", "missingUrl", "contentUrl")
        assertEquals(expectedIds.size, resp.ids.size)
        expectedIds.forEach { entryId -> assertNotNull(resp.ids.find { it == entryId }) }
    }

    @Test
    fun `test entry id request with good category id and continuation`() {
        val continuationToken = "acontinuation"
        val api = FakeApi().apply {
            nextContinuation = continuationToken
        }
        val service = FeedlyService(api, "feedlyUserToken")

        val resp = service.getEntryIdsForCategory("category1", NoContinuationToken).blockingGet()

        assertEquals(resp.continuation.apiString, continuationToken)
        val expectedIds = listOf("e1", "e2", "e3", "missingUrl", "contentUrl")
        assertEquals(expectedIds.size, resp.ids.size)
        expectedIds.forEach { entryId -> assertNotNull(resp.ids.find { it == entryId }) }
    }

    @Test
    fun `test entry id request has error`() {
        val msg = "some sort of error"

        val api = FakeApi()
        api.theException = Exception(msg)
        val service = FeedlyService(api, "feedlyUserToken")

        // TODO: figure out how to make this fail when exceptions are thrown
        service.getEntryIdsForCategory("someid", NoContinuationToken)
                .subscribeBy(onError = { assertEquals(msg, it.message) },
                        onSuccess = { fail("expected error") })
    }

    @Test
    fun `test entries for ids`() {
        val api = FakeApi()
        val service = FeedlyService(api, "feedlyUserToken")

        val entryIds = listOf("e1", "e2", "e3")
        val lst = service.getEntriesForIds(entryIds).blockingGet()

        assertEquals(3, lst.size)
        entryIds.forEach { entryId ->
            assertNotNull(lst.find { it.id == entryId })
        }
    }

    @Test
    fun `test entry without url is skipped`() {
        val api = FakeApi()
        val service = FeedlyService(api, "feedlyUserToken")

        val lst = service.getEntriesForIds(listOf("e1", "e2", "e3", "missingUrl")).blockingGet()

        assertEquals(3, lst.size)
        listOf("e1", "e2", "e3").forEach { entryId ->
            assertNotNull(lst.find { it.id == entryId })
        }
    }

    @Test
    fun `test entry gets url from content`() {
        val api = FakeApi()
        val service = FeedlyService(api, "feedlyUserToken")
        val entryIds = listOf("e1", "e2", "e3", "contentUrl")
        val lst = service.getEntriesForIds(entryIds).blockingGet()
        assertEquals(4, lst.size)
        entryIds.forEach { entryId ->
            assertNotNull(lst.find { it.id == entryId })
        }
    }

    @Test
    fun `test entries for ids has error`() {
        val message = "entries for ids failed"
        val api = FakeApi().apply {
            theException = Exception(message)
        }
        val service = FeedlyService(api, "feedlyUserToken")
        service.getEntriesForIds(emptyList()).subscribeBy({}, { fail("expected error") })
    }

    // TODO: test marking.
}