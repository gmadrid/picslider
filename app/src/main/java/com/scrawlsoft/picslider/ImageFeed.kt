package com.scrawlsoft.picslider

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map
import com.scrawlsoft.picslider.feedly.FeedlyFetcher
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class FeedlyImageFeed(private val fetcher: FeedlyFetcher) {
    val imageUrlS: Observable<String>
    private val imageUrlSubject = BehaviorSubject.create<String>()

    private data class ImageEntry(private val id: String, val url: String)

    private var entries: List<ImageEntry> = listOf()
    private var currIndex = 0
//    private var continuation: String? = null

    private fun hasNext() = currIndex < entries.size - 1
    private fun hasPrev() = currIndex > 0

    fun next() {
        if (hasNext()) {
            currIndex++
            imageUrlSubject.onNext(entries[currIndex].url)
        }
    }

    fun prev() {
        if (hasPrev()) {
            currIndex--
            imageUrlSubject.onNext(entries[currIndex].url)
        }
    }

    private fun <T : Any> nonNullResult(value: T?, e: Exception = Exception()): Result<T, Exception> {
        return if (value == null) {
            Result.Failure(e)
        } else {
            Result.Success(value)
        }
    }

    init {
        imageUrlS = imageUrlSubject.hide().share()

        Observable.create<Int> { subscriber ->
            fetcher.fetchCategories()
                    // Find the "Porn" category
                    .flatMap { nonNullResult(it.find { it.label == "Porn" }) }
                    .flatMap { fetcher.fetchEntryIds(it.id) }
                    .flatMap { fetcher.fetchEntriesForIds(it.ids) }
                    .map {
                        entries = entries.plus(it.map {
                            ImageEntry(it.id, it.visual?.url ?: "")
                        })
                        subscriber.onNext(entries.size)
                    }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    println("RSOITENORSITENOISRTENORISTENSRTOIENRSOTITENTSOIEN: $it")
                    println("XXX: ${entries[currIndex].url}")
                    imageUrlSubject.onNext(entries[currIndex].url)
                }

    }
}