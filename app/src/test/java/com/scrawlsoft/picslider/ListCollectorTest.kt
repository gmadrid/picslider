@file:Suppress("IllegalIdentifier")

package com.scrawlsoft.picslider

import com.scrawlsoft.picslider.base.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.blockingSubscribeBy
import junit.framework.Assert.*
import org.junit.Test
import java.net.URL

class ListCollectorTest {
    private fun simpleCategory(id: String) = ImageService.Category(id, "name($id)", "desc($id)")
    private fun simpleEntryPair(id: String) = Pair(id, ImageService.Entry(id, idToURL(id)))
    private fun idToURL(id: String) = URL("http://www.fakesite.com/$id")

    inner class FakeService : ImageService {
        val continuation1 = ContinuationToken("continuation1")
        val continuation2 = ContinuationToken("continuation2")

        var categoryResponse = listOf(
                simpleCategory("catOne"),
                simpleCategory("catTwo"),
                simpleCategory("catThree"),
                ImageService.Category("pornId", "Porn", "The fake porn category")
        )

        var entriesByContinuation: MutableMap<Continuation, Map<EntryId, ImageService.Entry>> =
                hashMapOf(
                        NoContinuationToken to hashMapOf(
                                simpleEntryPair("entry1"),
                                simpleEntryPair("entry2"),
                                simpleEntryPair("entry3"),
                                simpleEntryPair("entry4")
                        ),
                        continuation1 to hashMapOf(
                                simpleEntryPair("continuing5"),
                                simpleEntryPair("continuing6"),
                                simpleEntryPair("continuing7"),
                                simpleEntryPair("continuing8"),
                                simpleEntryPair("continuing9")
                        ),
                        continuation2 to hashMapOf(
                                simpleEntryPair("continuing2-10"),
                                simpleEntryPair("continuing2-11"),
                                simpleEntryPair("continuing2-12"),
                                simpleEntryPair("continuing2-13"),
                                simpleEntryPair("continuing2-14"),
                                simpleEntryPair("continuing2-15")
                        )
                )

        // If there is a next continuation, put it here.
        var nextContinuations: List<Continuation> = emptyList()
        var nextContinuationIndex = 0

        override val categories: Single<List<ImageService.Category>> =
                Single.create { it.onSuccess(categoryResponse) }

        override fun getEntryIdsForCategory(categoryId: CategoryId, continuation: Continuation)
                : Single<ImageService.EntryIdsResponse> =
                Single.create<ImageService.EntryIdsResponse> { subscriber ->
                    val entries = entriesByContinuation[continuation] ?: hashMapOf()
                    subscriber.onSuccess(
                            ImageService.EntryIdsResponse(
                                    nextContinuations
                                            .getOrElse(nextContinuationIndex++,
                                                    { NoContinuationToken }),
                                    entries.keys.sorted().toList()))
                }

        override fun getEntriesForIds(entryIds: List<EntryId>): Single<List<ImageService.Entry>> =
                Single.create<List<ImageService.Entry>> { subscriber ->
                    val allEntries: HashMap<EntryId, ImageService.Entry> = hashMapOf()
                    entriesByContinuation.values.forEach { map ->
                        allEntries.putAll(map)
                    }

                    subscriber.onSuccess(entryIds.mapNotNull {
                        allEntries[it]
                    })
                }

        override fun markAsRead(entryIds: List<String>): Completable {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    @Test
    fun `list collector with missing category`() {
        val fakeService = FakeService()
        fakeService.categoryResponse = fakeService.categoryResponse.filter { it.name != "Porn" }
        val collector = ListCollector(fakeService)

        collector.entries.blockingSubscribeBy(
                onError = { e ->
                    val s = "Category 'Porn' not found."
                    assertEquals(s, e.message)
                },
                onComplete = { fail("expected error, not completion") },
                onNext = { foo -> fail("expected error, not result: $foo") })
    }

    @Test
    fun `list collector with empty list`() {
        val fakeService = FakeService()
        fakeService.entriesByContinuation = hashMapOf(NoContinuationToken to hashMapOf())
        val collector = ListCollector(fakeService)

        collector.entries.blockingSubscribeBy { entries ->
            assertTrue(entries.isEmpty())
        }
    }

    @Test
    fun `list collector with first list`() {
        val fakeService = FakeService()
        val collector = ListCollector(fakeService)

        var theEntries: List<ImageService.Entry> = emptyList()
        collector.entries
                .blockingSubscribeBy { entries ->
                    theEntries = entries
                    assertEquals(4, entries.size)
                }
    }

    @Test
    fun `list collector with second list appended`() {
        val fakeService = FakeService()
        fakeService.nextContinuations = listOf(fakeService.continuation1)
        val collector = ListCollector(fakeService)

        var savedEntries: List<ImageService.Entry> = emptyList()
        collector.entries.blockingSubscribeBy(onNext = { entries ->
            savedEntries = entries
        })
        assertEquals(9, savedEntries.size)
    }

    @Test
    fun `list collector with third list`() {
        val fakeService = FakeService()
        fakeService.nextContinuations = listOf(fakeService.continuation1, fakeService.continuation2)
        val collector = ListCollector(fakeService)

        var savedEntries: List<ImageService.Entry> = emptyList()
        collector.entries.blockingSubscribeBy(onNext = { entries ->
            savedEntries = entries
        })
        assertEquals(15, savedEntries.size)
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