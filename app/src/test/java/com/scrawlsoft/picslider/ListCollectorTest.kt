@file:Suppress("IllegalIdentifier")

package com.scrawlsoft.picslider

import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import junit.framework.Assert.*
import org.junit.Test
import java.net.URL

class ListCollectorTest {
    fun simpleCategory(id: String) = ImageService.Category(id, "name($id)", "desc($id)")
    fun simpleEntryPair(id: String) = Pair(id, ImageService.Entry(id, idToURL(id)))
    private fun idToURL(id: String) = URL("http://www.fakesite.com/$id")

    inner class FakeService : ImageService {
        var categoryResponse = listOf(
                simpleCategory("catOne"),
                simpleCategory("catTwo"),
                simpleCategory("catThree"),
                ImageService.Category("pornId", "Porn", "The fake porn category")
        )
        // TODO: Test exceptions at every step of list collection
        // This map and continuation is used to generate the responses to both
        // getEntryIdsForCategory and getEntriesForIds.
        var nextContinuation: Continuation = NoContinuationToken

        // Entries returned on a first call
        var entriesMap = hashMapOf(
                simpleEntryPair("entry1"),
                simpleEntryPair("entry2"),
                simpleEntryPair("entry3"),
                simpleEntryPair("entry4")
        )
        // Entries returned on a call with a continuation string of "continuing"
        var expectedContinuation = Continuation.fromString("EXPECTEDCONTINUATION")
        private var continuingMap = hashMapOf(
                simpleEntryPair("continuing5"),
                simpleEntryPair("continuing6"),
                simpleEntryPair("continuing7"),
                simpleEntryPair("continuing8"),
                simpleEntryPair("continuing9")
        )


        override val categories: Single<List<ImageService.Category>> =
                Single.create { it.onSuccess(categoryResponse) }

        override fun getEntryIdsForCategory(categoryId: CategoryId, continuation: Continuation)
                : Single<ImageService.EntryIdsResponse> {
            val result = when (continuation) {
                NoContinuationToken ->
                    ImageService.EntryIdsResponse(nextContinuation, entriesMap.keys.sorted().toList())
                else -> {
                    nextContinuation = NoContinuationToken
                    ImageService.EntryIdsResponse(NoContinuationToken, continuingMap.keys.sorted().toList())
                }
            }

            return Single.create<ImageService.EntryIdsResponse> {
                it.onSuccess(result)
            }
        }

        override fun getEntriesForIds(entryIds: List<EntryId>): Single<List<ImageService.Entry>> =
                Single.create<List<ImageService.Entry>> {
                    it.onSuccess(entryIds.mapNotNull { entryId ->
                        (entriesMap[entryId] ?: continuingMap[entryId])
                    })
                }
    }

// TODO: get this working again.
//    @Test
//    fun `list collector with missing category`() {
//        val fakeService = FakeService()
//        fakeService.categoryResponse = fakeService.categoryResponse.filter { it.name != "Porn" }
//        val collector = ListCollector(fakeService)
//
//        collector.entries.subscribeBy(
//                onError = { e ->
//                    val s = "Category 'Porn' not found."
//                    assertTrue("At least one of the exceptions should be from ListCollector",
//                            e.message == s)
//                },
//                onComplete = { fail("expected error, not completion") },
//                onNext = { fail("expected error, not result") })
//    }

//    @Test
//    fun `list collector with empty list`() {
//        val fakeService = FakeService()
//        fakeService.entriesMap = hashMapOf()
//        val collector = ListCollector(fakeService)
//
//        collector.entries
//                .subscribeBy { entries ->
//                    assertTrue(entries.isEmpty())
//                }
//    }
//
//    @Test
//    fun `list collector with first list`() {
//        val fakeService = FakeService()
//        val collector = ListCollector(fakeService)
//
//        collector.entries
//                .subscribeBy { entries ->
//                    assertEquals(4, entries.size)
//                }
//    }
//
//    @Test
//    fun `list collector with second list appended`() {
//        val fakeService = FakeService()
//        fakeService.nextContinuation = fakeService.expectedContinuation
//        val collector = ListCollector(fakeService)
//
//        val allEntries: MutableList<ImageService.Entry> = mutableListOf()
//        collector.entries.subscribeBy(onNext = { entries ->
//            allEntries.addAll(entries)
//        })
//        assertEquals(9, allEntries.size)
//    }

    @Test
    fun foobar() {
        val foo : List<Int> = emptyList()
        val fakeService = FakeService()
        fakeService.nextContinuation = fakeService.expectedContinuation
        val collector = ListCollector(fakeService)

        assertEquals(9, collector.entries.size)
    }

//    @Test
//    fun `list collector access by index`() {
//    }
//
//    @Test
//    fun `list collector index out of range low`() {
//    }
//
//    @Test
//    fun `list collector index out of range high`() {
//    }
//
//    @Test
//    fun `list collector`() {
//    }
}