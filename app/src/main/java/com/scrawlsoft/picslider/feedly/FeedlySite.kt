package com.scrawlsoft.picslider.feedly

import com.scrawlsoft.picslider.site.Entry

//class FeedlyAuthToken internal constructor(private val user: String, private val token: String) : AuthToken
//
//class FeedlyContinuationToken internal constructor(internal val token: String) : ContinuationToken

/*class FeedlySite : Site<FeedlyStream, FeedlyEntry, FeedlyAuthToken, FeedlyContinuationToken> {
    override val name: String = "Feedly"
    override val description: String? = "www.feedly.com"
    override val iconURL: URL? = null

    //private val fetcher = FeedlyFetcher()

    private val authStream = BehaviorSubject.create<FeedlyAuthToken>()

    override val streamList: Observable<List<FeedlyStream>> = authStream.map { auth ->
        val categories = fetcher.fetchCategories()
        categories.getOrElse(emptyList()).map {
            FeedlyStream(it.id, it.label, it.description, null, fetcher)
        }
    }.subscribeOn(Schedulers.io())

    override fun authenticate(): Result<FeedlyAuthToken, Exception> {
        val token = FeedlyAuthToken(DEV_USER, DEV_TOKEN)
        authStream.onNext(token)
        return Result.Success(token)
    }
}*/

/*class FeedlyStream(private val id: String,
                   override val name: String,
                   override val description: String?,
                   override val iconURL: URL?)
    : Stream<FeedlyEntry, FeedlyAuthToken, FeedlyContinuationToken> {

    private val entryListBehavior = BehaviorSubject.createDefault(emptyList<FeedlyEntry>())
    override val entries: Observable<List<FeedlyEntry>> = entryListBehavior.hide().share()
//    override val entries = Observable.create<List<FeedlyEntry>> { subscriber ->
//        val entries = fetcher.fetchEntryIds(id)
//                .flatMap { fetcher.fetchEntriesForIds(it.ids)}
//        val next = entries.getOrElse(emptyList()).map {
//            FeedlyEntry(it.url!!)
//        }
//        subscriber.onNext(next)
//    }

    override fun getMoreEntries(auth: FeedlyAuthToken,
                                continuation: FeedlyContinuationToken)
            : Result<FeedlyContinuationToken, Exception> {
        TODO("not implemented")
        println("${continuation.token}")
    }
}*/

class FeedlyEntry(override val url: String) : Entry
