@file:Suppress("IllegalIdentifier")

package com.scrawlsoft.picslider

import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import io.reactivex.rxkotlin.subscribeBy
import junit.framework.Assert.*
import org.junit.Test
import java.net.URL

class ListCollectorTest {
    fun simpleCategory(id: String) = ImageService.Category(id, "name($id)", "desc($id)")
    fun simpleEntryPair(id: String) = Pair(id, ImageService.Entry(id, idToURL(id)))
    fun idToURL(id: String) = URL("http://www.fakesite.com/$id")

    inner class FakeService : ImageService {
        var categoryResponse = listOf<ImageService.Category>(
                simpleCategory("catOne"),
                simpleCategory("catTwo"),
                simpleCategory("catThree"),
                ImageService.Category("pornId", "Porn", "The fake porn category")
        )
        // TODO: Test exceptions at every step of list collection
        // This map and continuation is used to generate the responses to both
        // getEntryIdsForCategory and getEntriesForIds.
        var nextContinuation: String? = null

        // Entries returned on a first call
        var entriesMap = hashMapOf<String, ImageService.Entry>(
                simpleEntryPair("entryOne"),
                simpleEntryPair("entryTwo"),
                simpleEntryPair("entryThree"),
                simpleEntryPair("entryFour")
        )
        // Entries returned on a call with a continuation string of "continuing"
        var continuingMap = hashMapOf<String, ImageService.Entry>(
                simpleEntryPair("continuingFive"),
                simpleEntryPair("continuingSix"),
                simpleEntryPair("continuingSeven"),
                simpleEntryPair("continuingEight"),
                simpleEntryPair("continuingNine")
        )


        override val categories: Single<List<ImageService.Category>> =
                Single.create { it.onSuccess(categoryResponse) }

        override fun getEntryIdsForCategory(categoryId: String, continuation: String?)
                : Single<Pair<String?, List<String>>> =
                Single.create<Pair<String?, List<String>>> {
                    it.onSuccess(Pair(nextContinuation, entriesMap.keys.toList()))
                }

        override fun getEntriesForIds(entryIds: List<String>): Single<List<ImageService.Entry>> =
                Single.create<List<ImageService.Entry>> {
                    it.onSuccess(entryIds.mapNotNull { entryId -> entriesMap[entryId] })
                }
    }

    @Test
    fun `list collector with missing category`() {
        val fakeService = FakeService()
        fakeService.categoryResponse = fakeService.categoryResponse.filter { it.name != "Porn" }
        val collector = ListCollector(fakeService)

        collector.entries.subscribeBy(
                onError = { e ->
                    val ce = e as CompositeException
                    assertTrue("At least one of the exceptions should be from ListCollector",
                            ce.exceptions.any { it.message == "Category 'Porn' not found." })
                },
                onComplete = { fail("expected error, not completion") },
                onNext = { fail("expected error, not result") })
    }

    @Test
    fun `list collector with empty list`() {
        val fakeService = FakeService()
        fakeService.entriesMap = hashMapOf()
        val collector = ListCollector(fakeService)

        val entries = collector.entries.blockingFirst();
        assertEquals(0, entries.size)
    }

    @Test
    fun `list collector with first list`() {
        val fakeService = FakeService()
        val collector = ListCollector(fakeService)

        val entries = collector.entries.blockingFirst()
        assertEquals(4, entries.size)

        assertEquals(false, collector.hasContinuation.blockingFirst())
    }

    @Test
    fun `list collector with second list appended`() {
        val fakeService = FakeService()
        fakeService.nextContinuation = "FOOBAR"
        val collector = ListCollector(fakeService)

        val entries = collector.entries.blockingFirst()
        assertEquals(4, entries.size)

        assertTrue(collector.hasContinuation.blockingFirst())
        collector.continueRequest()
        assertEquals(9, entries.size)
    }

    @Test
    fun `list collector access by index`() {
    }

    @Test
    fun `list collector index out of range low`() {
    }

    @Test
    fun `list collector index out of range high`() {
    }

    @Test
    fun `list collector`() {
    }
}