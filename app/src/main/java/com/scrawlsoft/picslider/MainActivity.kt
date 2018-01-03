package com.scrawlsoft.picslider

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


/*
 * TODO
 * - continuation
 * - better looking buttons
 * - volume controls
 * - content grepping for image url
 * - download to files then load file urls
 * - save image to Dropbox, converting to _1280 if possible.
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

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
                    val image = main_image.drawable as BitmapDrawable
                    val bitmap = image.bitmap
                    val outStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                    val bytes = outStream.toByteArray()

                    val filename = "girl-${it.id}.png"
                    val file = File(getExternalFilesDir(null), filename)
                    val fileOut = FileOutputStream(file)
                    fileOut.write(bytes)
                    fileOut.close()

                    Toast.makeText(this, "Wrote $filename", Toast.LENGTH_LONG).show()
                }
    }
}
