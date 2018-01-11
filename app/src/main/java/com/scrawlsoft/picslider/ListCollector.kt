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

    private fun downloadPagedResponse(category: CategoryId, continuation: Continuation)
            : Observable<ImageService.EntryIdsResponse> {
        return imageService.getEntryIdsForCategory(category, continuation).toObservable()
                .concatMap { response ->
                    if (response.continuation == NoContinuationToken) {
                        just(response)
                    } else {
                        just(response).concatWith(
                                imageService.getEntryIdsForCategory(category, response.continuation)
                                        .toObservable())
                    }
                }
    }

    private val entryIdsResp: Observable<ImageService.EntryIdsResponse> =
            category.flatMap { category ->
                downloadPagedResponse(category.id, NoContinuationToken)
            }

    var entries: Observable<List<ImageService.Entry>> = entryIdsResp
            .concatMap { resp -> imageService.getEntriesForIds(resp.ids).toObservable() }
            .scan { acc, newList -> acc + newList }
}
