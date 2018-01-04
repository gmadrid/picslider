package com.scrawlsoft.picslider

import android.accounts.AccountManager
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


/*
 * TODO
 * - continuation
 * - better looking buttons
 * - volume controls
 * - save image to Dropbox, converting to _1280 if possible.
 * - when saving, use _1280 version.
 * - pre-load images.
 *   - don't overfill cache
 *   - increase space available for disk cache.
 * - animate moving to new image
 * - swipe to new image
 * - In FeedlyApi, change return types to Single where able.
 */
class MainActivity : AppCompatActivity() {

    class ClosureCallback(private val successClosure: () -> Unit,
                          private val errorClosure: () -> Unit = {}) : Callback {
        override fun onSuccess() {
            successClosure()
        }

        override fun onError() {
            errorClosure()
        }
    }

    fun upscaleTumblrUri(uriString: String): String {
        val uri = Uri.parse(uriString)
        if (uri.getHost().contains("tumblr")) {
            val re = Regex("_\\d?00.")
            val replaced = re.replace(uri.path, "_1280.")

            val newUri = uri.buildUpon().path(replaced).build()
            println("The thing: $newUri")
            return newUri.toString()
        }
        return uriString
    }

    fun downloadUri(uriString: String) {
        val uri = upscaleTumblrUri(uriString)
        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val req = DownloadManager.Request(Uri.parse(uri))
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        dm.enqueue(req)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val dropboxId = resources.getString(R.string.dropbox_app_id)
        val dropboxSecret = resources.getString(R.string.dropbox_app_secret)


        val acctMgr = AccountManager.get(this)
        val accts = acctMgr.getAccountsByType("dropbox")
        println(accts)
        val options = Bundle()
        //acctMgr.getAuthToken()


        // We have to load the context into Picasso before we can do anything with it.
        val picasso = Picasso.with(this)
        picasso.setIndicatorsEnabled(true)

        // The debug version reads our tokens from local.properties with help from Gradle config.
        val userToken = FeedlyService.readTokenFromResources(resources)
        val service = FeedlyService(userToken)

        val browser = StreamBrowser(service, prev_button.clicks(), next_button.clicks())
        browser.currentEntry
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val entryId = it.id
                    picasso().load(it.url)
                            .placeholder(R.drawable.loading_icon)
                            .into(main_image, ClosureCallback(successClosure = {
                                service.markAsRead(entryId)
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
