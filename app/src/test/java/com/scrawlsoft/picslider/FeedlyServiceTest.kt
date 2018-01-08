@file:Suppress("IllegalIdentifier")

package com.scrawlsoft.picslider

import com.scrawlsoft.picslider.feedly.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import junit.framework.Assert.*
import org.junit.Test

class FeedlyServiceTest {
    class FakeApi : FeedlyApi {

        var cats: List<FeedlyApiCategory> = listOf(FeedlyApiCategory("one", "oneLabel", description = "oneDesc"),
                FeedlyApiCategory("two", "twoLabel", "twoDesc"),
                FeedlyApiCategory("three", "threeLabel", "threeDesc"))
        var theException: Throwable? = null
        var continuation: String? = null
        var entryIds: List<String> = listOf("e1", "e2", "e3", "e4")
        private var entryLookup = hashMapOf(
                "e1" to FeedlyApiJSONEntry("e1", null, simpleVisual("http://foobar.com/e1"), null),
                "e2" to FeedlyApiJSONEntry("e2", null, simpleVisual("http://foobar.com/e2"), null),
                "e3" to FeedlyApiJSONEntry("e3", null, simpleVisual("http://foobar.com/e3"), null),
                "missingUrl" to FeedlyApiJSONEntry("missingUrl", null, null, null),
                "contentUrl" to FeedlyApiJSONEntry("contentUrl", null, null, fakeSummary("http://foobar.com/contentUrl"))
        )

        override fun categories(authHeader: String): Single<List<FeedlyApiCategory>> =
                Single.create<List<FeedlyApiCategory>> { subscriber ->
                    if (theException != null) throw theException!!
                    subscriber.onSuccess(cats)
                }

        override fun entryIdsForStream(authHeader: String, streamId: String, unreadOnly: Boolean, count: Int, continuation: String?)
                : Single<FeedlyApiEntryIdsResponse> {
            return Single.create<FeedlyApiEntryIdsResponse> { subscriber ->
                if (theException != null) throw theException!!
                subscriber.onSuccess(FeedlyApiEntryIdsResponse(if (streamId != "valid") {
                    emptyList()
                } else {
                    entryIds
                }, continuation))
            }
        }

        private fun simpleVisual(url: String): FeedlyEntryVisual {
            return FeedlyEntryVisual(url, 100, 100, "context-type")
        }

        private fun fakeSummary(url: String): FeedlyEntrySummary {
            val str = "<p>Some text<p>\n" +
                    "foobar<img src=\"$url\">\n" +
                    "Sometrailingtext"
            return FeedlyEntrySummary(str)
        }

        override fun entriesForIds(entryIds: List<String>): Single<List<FeedlyApiJSONEntry>> {
            if (theException != null) {
                return Single.create<List<FeedlyApiJSONEntry>> { it.onError(theException!!) }
            }
            val outputList = entryIds.fold(emptyList<FeedlyApiJSONEntry>()) { acc, entryId ->
                val found = entryLookup[entryId]
                if (found != null) {
                    acc + found
                } else {
                    acc
                }
            }
            return Single.create<List<FeedlyApiJSONEntry>> { it.onSuccess(outputList) }
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
        assertEquals(3, lst.size)
        listOf("one", "two", "three").forEach { searchId ->
            assertNotNull(lst.find { it.id == searchId })
        }
        assertNull(lst.find { it.id == "NotFound" })
    }

    @Test
    fun `test empty categories`() {
        val api = FakeApi()
        api.cats = emptyList()
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
        val resp = service.getEntryIdsForCategory("invalid").blockingGet()
        assertNull(resp.first)
        assertEquals(0, resp.second.size)
    }

    @Test
    fun `test entry id request with good category id`() {
        val api = FakeApi()
        val service = FeedlyService(api, "feedlyUserToken")
        val resp = service.getEntryIdsForCategory("valid").blockingGet()
        assertNull(resp.first)
        assertEquals(4, resp.second.size)
        api.entryIds.forEach { entryId -> assertNotNull(resp.second.find { it == entryId }) }
    }

    @Test
    fun `test entry id request with good category id and continuation`() {
        val theContinuation = "acontinuation"
        val api = FakeApi().apply {
            continuation = theContinuation
        }
        val service = FeedlyService(api, "feedlyUserToken")
        val resp = service.getEntryIdsForCategory("valid").blockingGet()
        assertEquals(resp.first, theContinuation)
        assertEquals(4, resp.second.size)
        api.entryIds.forEach { entryId -> assertNotNull(resp.second.find { it == entryId }) }
    }

    @Test
    fun `test entry id request has error`() {
        val api = FakeApi()
        val msg = "some sort of error"
        api.theException = Exception(msg)
        val service = FeedlyService(api, "feedlyUserToken")
        service.getEntryIdsForCategory("someid")
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
}