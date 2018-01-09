@file:Suppress("IllegalIdentifier")

package com.scrawlsoft.picslider

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
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
        var nextContinuation: String = NO_CONTINUATION

        // Entries returned on a first call
        var entriesMap = hashMapOf<String, ImageService.Entry>(
                simpleEntryPair("entryOne"),
                simpleEntryPair("entryTwo"),
                simpleEntryPair("entryThree"),
                simpleEntryPair("entryFour")
        )
        // Entries returned on a call with a continuation string of "continuing"
        var expectedContinuation = "EXPECTEDCONTINUATION"
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
                : Single<Pair<String, List<String>>> {
            val nonNullContinuation: String = continuation ?: NO_CONTINUATION
            println("THE CONT: $nonNullContinuation")
            val result = when (nonNullContinuation) {
                NO_CONTINUATION -> Pair(nextContinuation, entriesMap.keys.toList())
                nextContinuation -> {
                    nextContinuation = NO_CONTINUATION
                    Pair(nextContinuation, continuingMap.keys.toList())
                }
                else -> Pair(NO_CONTINUATION, emptyList())
            }
            println("RESULT: $result")

            return Single.create<Pair<String, List<String>>> {
                it.onSuccess(result)
            }
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
        val collector = ListCollector(fakeService, Observable.empty())

        collector.entries.subscribeBy(
                onError = { e ->
                    val s = "Category 'Porn' not found."
                    assertTrue("At least one of the exceptions should be from ListCollector",
                            e.message == s)
                },
                onComplete = { fail("expected error, not completion") },
                onNext = { fail("expected error, not result") })
    }

    @Test
    fun `list collector with empty list`() {
        val fakeService = FakeService()
        fakeService.entriesMap = hashMapOf()
        val collector = ListCollector(fakeService, Observable.empty())

        Observables.combineLatest(collector.entries, collector.hasContinuation)
                .subscribeBy { (entries, hasContinuation) ->
                    assertTrue(entries.isEmpty())
                    assertFalse(hasContinuation)
                }
    }

    @Test
    fun `list collector with first list`() {
        val fakeService = FakeService()
        val collector = ListCollector(fakeService, Observable.empty())

        with(collector) {
            Observables.combineLatest(entries, hasContinuation)
                    .subscribeBy { (lst, hasCont) ->
                        assertEquals(4, lst.size)
                        assertFalse(hasCont)
                    }
        }
    }

    class MyTestObserver : TestObserver<Triple<List<ImageService.Entry>, Boolean, String>>() {
        lateinit var entries: List<ImageService.Entry>
        var hasContinuation: Boolean? = null
        lateinit var continuation: String

        override fun onNext(t: Triple<List<ImageService.Entry>, Boolean, String>) {
            println("In onNext: $t")
            val (entriesIn, hasContinuationIn, continuationIn) = t
            super.onNext(t)
            entries = entriesIn
            hasContinuation = hasContinuationIn
            continuation = continuationIn
        }
    }

    @Test
    fun `list collector with second list appended`() {
        val fakeService = FakeService()
        val continuationSubject = PublishSubject.create<String>()
        fakeService.nextContinuation = fakeService.expectedContinuation
        val collector = ListCollector(fakeService, Observable.empty())

        val observer = MyTestObserver()

        with(collector) {
            Observables.combineLatest(entries, hasContinuation, continuation)
                    .subscribe(observer)
        }

        with(observer) {
            assertEquals(4, entries.size)
            assertTrue(hasContinuation ?: false)
            assertEquals(fakeService.expectedContinuation, continuation)

            continuationSubject.onNext(continuation)

            assertEquals(9, entries.size)
            assertFalse(hasContinuation ?: true)
            assertEquals(NO_CONTINUATION, continuation)
        }
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