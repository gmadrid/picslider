package com.scrawlsoft.picslider

import com.scrawlsoft.picslider.feedly.FeedlyApiEntry
import com.scrawlsoft.picslider.feedly.FeedlyService
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Exports a List<FeedlyEntry> and it's count.
 * Manages getting more entries if required.
 */
class ListCollector(feedlyService_: FeedlyService) {
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