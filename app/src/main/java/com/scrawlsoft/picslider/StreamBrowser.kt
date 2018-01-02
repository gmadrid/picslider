package com.scrawlsoft.picslider

import com.scrawlsoft.picslider.feedly.FeedlyApiEntry
import com.scrawlsoft.picslider.feedly.FeedlyService
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject

class StreamBrowser(prevInput: Observable<Unit>, nextInput: Observable<Unit>) {
    private val service = FeedlyService()

    private val entries = service.getCategories()
            .flatMap { Observable.fromIterable(it) }
            .filter { it.label == "Porn" }
            .flatMap { service.getEntryIdsForCategory(it.id) }
            .flatMap { service.getEntriesForIds(it.ids) }

    private val currentIndex = BehaviorSubject.createDefault(0)

    val currentEntry: Observable<FeedlyApiEntry> =
            Observable.combineLatest(currentIndex, entries, BiFunction { idx, lst -> lst[idx] })

    private val hasPrev = currentIndex.map { it > 0 }
    private val hasNext =
            Observable.combineLatest(currentIndex, entries, BiFunction<Int, List<FeedlyApiEntry>, Boolean> { idx, lst ->
                idx < lst.size - 1
            })

    init {
        nextInput.withLatestFrom(hasNext, BiFunction { _: Unit, bar: Boolean -> bar })
                .filter { it }
                .subscribe { currentIndex.onNext(currentIndex.value + 1) }

        prevInput.withLatestFrom(hasPrev, BiFunction { _: Unit, bar: Boolean -> bar })
                .filter { it }
                .subscribe { currentIndex.onNext(currentIndex.value - 1) }
    }
}
