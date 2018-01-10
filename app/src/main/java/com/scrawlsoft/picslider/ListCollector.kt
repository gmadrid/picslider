package com.scrawlsoft.picslider

import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables

/**
 * Exports a List<FeedlyEntry> and its count.
 * Manages getting more entries if required.
 */
class ListCollector(private val imageService: ImageService,
                    continuationRequest: Observable<Continuation>,
                    categoryName: Observable<String> = just("Porn")) {

    // We request categories one time when we start up, then we cache it for all future ops.
    private val categories: Single<List<ImageService.Category>> = imageService.categories.cache()

    // Whenever the user changes the category name, find it in the list.
    // A bad category name will cause an error.
    private val category = Observables.combineLatest(categories.toObservable(), categoryName)
    { lst, name ->
        lst.find { it.name == name } ?:
                throw Exception("Category '$name' not found.")

    }
            .replay(1)
            .autoConnect()

    private val entryIdsResp: Observable<ImageService.EntryIdsResponse> = category.flatMap { cat ->
        imageService.getEntryIdsForCategory(cat.id, NoContinuationToken)
                .toObservable().concatMap { resp ->
            if (resp.continuation == NoContinuationToken) {
                println("FIRST ONE")
                Observable.just(resp)
            } else {
                println("SECOND ONE")
                Observable.just(resp).concatWith {
                    imageService.getEntryIdsForCategory(cat.id, resp.continuation).toObservable()
                }
            }
        }
    }
            .doOnNext { println("NEXT happened") }
            .doOnComplete { println("COMPLETE happened") }

    val entries: Observable<List<ImageService.Entry>> = entryIdsResp
            .concatMap { resp ->
                println("GOT A RESP: $resp")
                imageService.getEntriesForIds(resp.ids).toObservable() }
//            .toList()
//            .map { it.flatten() }
//            .toObservable()
//            .share()
}