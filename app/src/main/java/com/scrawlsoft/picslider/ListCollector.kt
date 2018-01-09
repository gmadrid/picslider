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
                    continuationRequest: Observable<String>,
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

    private val entryIdsResp: Observable<Pair<String, List<String>>> =
            Observables.combineLatest(category, continuationRequest.startWith(NO_CONTINUATION))
                    .flatMap { (category, continuationString) ->
                        println("IN FLATMAP: $continuationString")
                        val cString = if (continuationString != NO_CONTINUATION) continuationString else null
                        imageService.getEntryIdsForCategory(category.id, cString).toObservable()
                    }

    val entries: Observable<List<ImageService.Entry>> = entryIdsResp
            .flatMap { resp -> imageService.getEntriesForIds(resp.second).toObservable() }
            .share()

    val continuation: Observable<String> = entryIdsResp.map { it.first }
    val hasContinuation: Observable<Boolean> = continuation.map { it != NO_CONTINUATION }


}