package com.scrawlsoft.picslider

import com.github.kittinunf.result.Result
import io.reactivex.Observable
import java.net.URL

/**
 * Conceptually, we have these entities:
 *
 * Site - something that will give you images organized in Streams using a protocol.
 *        It may also require it's own auth.
 *        If you have to have a username/password for it, it's a Site.  Each site may include
 *        an icon URL.
 *
 * Stream - each site provides at least one stream. A stream is a list of images that may grow over
 *          time. For example, a subscription or category on Feedly. Or a query on 500px.  Each
 *          stream may include an icon URL.
 *
 * Entry - one image from a stream. It will _always_ have an image URL. It may also include more
 *         stuff, for example, a Tumblr post may have a title, date, summary text, etc.
 */

interface AuthToken

interface Site<S: Stream<E, T, C>, E: Entry, T: AuthToken, C: ContinuationToken> {
    val name: String
    val description: String?
    val iconURL: URL?

    val streamList: Observable<List<S>>

    // TODO: what should this return? Maybe it should just throw.
    fun authenticate(): Result<T, Exception>
}

interface ContinuationToken

interface Stream<E: Entry, in T: AuthToken, C: ContinuationToken> {
    val name: String
    val description: String?
    val iconURL: URL?

    val entries: Observable<List<E>>

    fun getMoreEntries(auth: T, continuation: C): Result<C, Exception>
}

interface Entry {
    val url: String
}

/*
class FeedlyImageFeed(private val fetcher: FeedlyFetcher) {
    val imageUrlS: Observable<String>
    private val imageUrlSubject = BehaviorSubject.create<String>()

    private data class ImageEntry(private val id: String, val url: String)

    private var entries: List<ImageEntry> = listOf()
    private var currIndex = 0
    private var continuation: String? = null

    private val hasNext
        get() = currIndex < entries.size - 1
    private val hasPrev
        get() = currIndex > 0

    fun next() {
        if (hasNext) {
            currIndex++
            imageUrlSubject.onNext(entries[currIndex].url)
        }
    }

    fun prev() {
        if (hasPrev) {
            currIndex--
            imageUrlSubject.onNext(entries[currIndex].url)
        }
    }

    init {
        imageUrlS = imageUrlSubject.hide().share()

        Observable.create<Int> { subscriber ->
            fetcher.fetchCategories()
                    // Find the "Porn" category
                    .flatMap { nonNullResult(it.find { it.label == "Porn" }) }
                    .flatMap { fetcher.fetchEntryIds(it.id) }
                    .flatMap {
                        continuation = it.continuation
                        fetcher.fetchEntriesForIds(it.ids)
                    }
                    .map {
                        entries = entries.plus(it.map {
                            ImageEntry(it.id, it.url!!)
                        })
                        subscriber.onNext(entries.size)
                        entries.forEach {
                            picasso().load(it.url).fetch()
                        }
                    }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    imageUrlSubject.onNext(entries[currIndex].url)
                }

    }
}
        */