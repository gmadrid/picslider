package com.scrawlsoft.picslider

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.scrawlsoft.picslider.feedly.FeedlyService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy

/*
 * TODO
 * - continuation
 * - better looking buttons
 * - volume controls
 * - content grepping for image url
 * - download to files then load file urls
 * - perhaps be a file viewer. Probably a bad and lazy decision.
 * - then you will have to persist shit and sync ugh
 * - ensure that Picasso instance is created before it's used.
 */
class MainActivity : AppCompatActivity() {

//    private var imageFeed: FeedlyImageFeed? = null // =

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val service = FeedlyService()
        service.getCategories()
                .flatMap { Observable.fromIterable(it) }
                .filter { it.label == "Porn" }
                .flatMap { service.getEntryIdsForCategory(it.id) }
                .flatMap { service.getEntriesForIds(it.ids) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = { println("ERROR: $it") }) {
                    println(it)
                }

//        val site = FeedlySite()
//        site.authenticate().fold({ println(it) }, { println(it) })
//        site.streamList.map { it.filter { it.name == "Porn"} }
//                .subscribe {
//                    var browser = StreamBrowser(it[0],
//                            Observable.just(Unit),
//                            Observable.just(Unit))
//                    println(browser)
//                }

//        val picasso = Picasso.with(this)
//        picasso.setIndicatorsEnabled(true)
//
//        val feed = FeedlyImageFeed(FeedlyFetcher(DEV_USER, DEV_TOKEN))
//        feed.imageUrlS
//                .subscribe {
//                    picasso().load(it).into(main_image)
//                }
//        imageFeed = feed
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickNext(view: View) {
//        imageFeed?.next()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickPrev(view: View) {
//        imageFeed?.prev()
    }
}
