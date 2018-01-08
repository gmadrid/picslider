package com.scrawlsoft.picslider

import io.reactivex.Observable

/**
 * Exports a List<FeedlyEntry> and its count.
 * Manages getting more entries if required.
 */
class ListCollector(private val imageService: ImageService) {

    private val theCategory = imageService.categories
            .flatMapObservable { Observable.fromIterable(it) }
            .filter { category -> category.name == "Porn" }
            .firstOrError()
            .onErrorReturn { throw Exception("Category 'Porn' not found.") }

    private val entryIdsResp = theCategory
            .flatMap { category -> imageService.getEntryIdsForCategory(category.id) }
            .cache()

    val entries: Observable<List<ImageService.Entry>> = entryIdsResp
            .flatMap { pair -> imageService.getEntriesForIds(pair.second) }
            .toObservable()

    private val continuation = entryIdsResp.flatMapObservable {
        Observable.just("")
    }

    val hasContinuation = continuation.map { it != "" }

    fun continueRequest() {}


//    val feedlyService = feedlyService_
//    val entries = BehaviorSubject.create<List<FeedlyApiEntry>>()
//    val entryCount = entries.map { it.size }
//
//    val theCategory = feedlyService.getCategories()
//            .flatMapObservable { Observable.fromIterable(it) }
//            .filter { category -> category.label == "Porn"}
//            .firstOrError()

//    val entriesResponse = theCategory.flatMap { category -> feedlyService.getEntryIdsForCategory(category.id) }
//            .flatMap { category -> feedlyService.getEntryIdsForCategory(category.id) }
//            .flatMap { entryIds -> feedlyService.getEntriesForIds(entryIds)}


    init {
        //feedlyService.entries
    }
}