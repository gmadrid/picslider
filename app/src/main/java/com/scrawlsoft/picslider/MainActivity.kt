package com.scrawlsoft.picslider

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.enabled
import com.scrawlsoft.picslider.feedly.FeedlyService
import com.scrawlsoft.picslider.utils.picasso
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.withLatestFrom
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


/*
 * TODO
 * - continuation
 * - better looking buttons
 * - volume controls
 * - save image to Dropbox, converting to _1280 if possible.
 * - pre-load images.
 *   - don't overfill cache
 *   - increase space available for disk cache.
 * - animate moving to new image
 * - swipe to new image
 * - In FeedlyApi, change return types to Single where able.
 */
class MainActivity : AppCompatActivity() {

    @Inject lateinit var feedlyService: FeedlyService

    class ClosureCallback(private val successClosure: () -> Unit,
                          private val errorClosure: () -> Unit = {}) : Callback {
        override fun onSuccess() {
            successClosure()
        }

        override fun onError() {
            errorClosure()
        }
    }

    private fun upscaleTumblrUri(uriString: String): String {
        val uri = Uri.parse(uriString)
        if (uri.host.contains("tumblr")) {
            val re = Regex("_\\d?00.")
            val replaced = re.replace(uri.path, "_1280.")

            val newUri = uri.buildUpon().path(replaced).build()
            return newUri.toString()
        }
        return uriString
    }

    private fun downloadUri(uriString: String) {
        val uri = upscaleTumblrUri(uriString)
        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val req = DownloadManager.Request(Uri.parse(uri))
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        dm.enqueue(req)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as PicSliderApp).appComponent.inject(this)

        setContentView(R.layout.activity_main)

        // We have to load the context into Picasso before we can do anything with it.
        val picasso = Picasso.with(this)
        picasso.setIndicatorsEnabled(true)

        val browser = StreamBrowser(feedlyService, prev_button.clicks(), next_button.clicks())
        browser.currentEntry
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val entryId = it.id
                    picasso().load(it.url)
                            .placeholder(R.drawable.loading_icon)
                            .into(main_image, ClosureCallback(successClosure = {
                                feedlyService.markAsRead(entryId)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeBy(onError = { err ->
                                            println(err.toString())
                                            Toast.makeText(this, "Failed to mark as read", Toast.LENGTH_LONG).show()
                                        }, onComplete = {
                                            Toast.makeText(this, "Marked as read.", Toast.LENGTH_SHORT).show()
                                        })

                            }, errorClosure = {
                                Observable.just("Failed to load image")
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                                        }
                            }))
                }

        browser.hasPrev.subscribe(prev_button.enabled())
        browser.hasNext.subscribe(next_button.enabled())

        save_button.clicks().withLatestFrom(browser.currentEntry) { _, entry -> entry }
                .subscribe {
                    if (it.url != null) {
                        downloadUri(it.url)
                    }
                }
    }
}
