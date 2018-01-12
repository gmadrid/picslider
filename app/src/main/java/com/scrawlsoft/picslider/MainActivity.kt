package com.scrawlsoft.picslider

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View.FOCUS_LEFT
import android.view.View.FOCUS_RIGHT
import android.widget.Toast
import com.jakewharton.rxbinding2.view.clicks
import com.scrawlsoft.picslider.feedly.FeedlyService
import com.scrawlsoft.picslider.images.DownloadMgr
import com.scrawlsoft.picslider.images.ImageDisplayAndCache
import com.trello.rxlifecycle2.android.lifecycle.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


/*
 * TODO
 * - put some caching back in
 * - disposeBins everywhere
 * - onError everywhere
 * - continuation
 * - better looking buttons
 * - save image to Dropbox, converting to _1280 if possible.
 * - pre-load images.
 *   - make sure next images are in memory
 *   - pre-scale images in cache
 *   - don't overfill cache
 *   - increase space available for disk cache.
 * - animate moving to new image
 * - swipe to new image
 */
class MainActivity : AppCompatActivity() {

    @Inject lateinit var downloader: DownloadMgr
    @Inject lateinit var feedlyService: FeedlyService
    @Inject lateinit var imageDisplay: ImageDisplayAndCache

    private val volumeSubject = PublishSubject.create<Int>()

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    volumeSubject.onNext(KeyEvent.KEYCODE_VOLUME_UP)
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    volumeSubject.onNext(KeyEvent.KEYCODE_VOLUME_DOWN)
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as PicSliderApp).appComponent.inject(this)
        setContentView(R.layout.activity_main)

        val collector = ListCollector(feedlyService)

        println(main_pager.adapter)
        collector.entries.observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribeBy(onError = { e ->
                    Toast.makeText(this, "FOUND AN EXC", Toast.LENGTH_SHORT).show()
                    println(e.localizedMessage)
                }) { newList ->
                    if (main_pager.adapter == null) {
                        if (newList.isNotEmpty()) {
                            main_pager.adapter = ImagePageAdapter(this, feedlyService, imageDisplay).apply {
                                entries = newList
                            }
                            // TODO: DRY
                            (main_pager.adapter as ImagePageAdapter).entries = newList
                        }
                    } else {
                        (main_pager.adapter as ImagePageAdapter).entries = newList
                    }
                }

        val volPrev = volumeSubject.filter { it == KeyEvent.KEYCODE_VOLUME_UP }.map { Unit }
        val volNext = volumeSubject.filter { it == KeyEvent.KEYCODE_VOLUME_DOWN }.map { Unit }
        val prevStream = Observable.merge(volPrev, prev_button.clicks()).map { FOCUS_LEFT }
        val nextStream = Observable.merge(volNext, next_button.clicks()).map { FOCUS_RIGHT }
        Observable.merge(prevStream, nextStream)
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe { main_pager.arrowScroll(it) }



//        browser.hasPrev.bindToLifecycle(this).subscribe(prev_button.enabled())
//        browser.hasNext.bindToLifecycle(this).subscribe(next_button.enabled())
//
//        save_button.clicks().withLatestFrom(browser.currentEntry) { _, entry -> entry }
//                .bindToLifecycle(this)
//                .subscribe {
//                    downloader.downloadUri(it.uri.toString())
//                }
    }
}
