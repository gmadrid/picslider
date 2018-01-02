package com.scrawlsoft.picslider

import com.scrawlsoft.picslider.maybe.Maybe
import com.scrawlsoft.picslider.maybe.None
import com.scrawlsoft.picslider.maybe.Some
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject

/**
 * Using the Stream interface, implements a browser for its images.
 */
class StreamBrowser<S : Stream<E, T, C>, E : Entry, T : AuthToken, C : ContinuationToken>(
        stream: S,
        prevInput: Observable<Unit>,
        nextInput: Observable<Unit>) {

    private val entries: Observable<List<E>> = stream.entries
    private val currentIndex = BehaviorSubject.createDefault(0)

    val currentEntry: Observable<Maybe<E>> =
            Observable.combineLatest(currentIndex, entries, BiFunction { idx, lst ->
                if (idx >= 0 && idx < lst.size) Some(lst[idx]) else None
            })

    val hasPrev: Observable<Boolean> = currentIndex.map { it > 0 }
    val hasNext: Observable<Boolean> =
            Observable.combineLatest(currentIndex, entries, BiFunction { idx: Int, lst: List<Entry> ->
                idx < lst.size - 1
            })

    init {
        prevInput.withLatestFrom(hasPrev, BiFunction<Unit, Boolean, Boolean> { _, hp -> hp })
                .subscribe {
                    if (it) {
                        currentIndex.onNext(currentIndex.value - 1)
                    }
                }
        nextInput.withLatestFrom(hasNext, BiFunction<Unit, Boolean, Boolean> { _, hn -> hn })
                .subscribe {
                    if (it) {
                        currentIndex.onNext(currentIndex.value + 1)
                    }
                }
    }
}