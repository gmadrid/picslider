package com.scrawlsoft.picslider

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.widget.Toast
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.enabled
import com.scrawlsoft.picslider.feedly.FeedlyService
import com.scrawlsoft.picslider.images.DownloadMgr
import com.scrawlsoft.picslider.images.ImageDisplayAndCache
import com.trello.rxlifecycle2.android.lifecycle.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.withLatestFrom
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

    private fun upscaleTumblrUri(uriString: String): String {
        val uri = Uri.parse(uriString)
        if (uri.host.contains("tumblr")) {
            // TODO: should I escape this '.'?
            val re = Regex("_\\d?00.")
            val replaced = re.replace(uri.path, "_1280.")

            val newUri = uri.buildUpon().path(replaced).build()
            return newUri.toString()
        }
        return uriString
    }

    // TODO: pass Uri here instead of String
    private fun downloadUri(uriString: String) {
        val uri = upscaleTumblrUri(uriString)
        val req = DownloadManager.Request(Uri.parse(uri))
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        downloader.enqueue(req)
    }

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

        val volPrev = volumeSubject.filter { it == KeyEvent.KEYCODE_VOLUME_UP }.map { Unit }
        val volNext = volumeSubject.filter { it == KeyEvent.KEYCODE_VOLUME_DOWN }.map { Unit }
        val prevStream = Observable.merge(volPrev, prev_button.clicks())
        val nextStream = Observable.merge(volNext, next_button.clicks())
        val browser = StreamBrowser(feedlyService, prevStream, nextStream)
        browser.currentEntry
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe {
                    val entryId = it.id
                    val uri = it.uri
                    imageDisplay.displayIntoView(uri, main_image)
                            .andThen(feedlyService.markAsRead(entryId))
                            .observeOn(AndroidSchedulers.mainThread())
                            // TODO: Figure out now to get rid of nested subscribes.
                            .subscribeBy(onError = { _ ->
                                Toast.makeText(this, "Failed to mark as read", Toast.LENGTH_SHORT).show()
                            }) {
                                //Toast.makeText(this, "Marked as read", Toast.LENGTH_SHORT).show()
                            }
                }

        browser.hasPrev.bindToLifecycle(this).subscribe(prev_button.enabled())
        browser.hasNext.bindToLifecycle(this).subscribe(next_button.enabled())

        save_button.clicks().withLatestFrom(browser.currentEntry) { _, entry -> entry }
                .bindToLifecycle(this)
                .subscribe {
                    downloadUri(it.uri.toString())
                }
    }
}
