package com.scrawlsoft.picslider

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.clicks
import com.scrawlsoft.picslider.utils.picasso
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // We have to load the context into Picasso before we can do anything with it.
        val picasso = Picasso.with(this)
        picasso.setIndicatorsEnabled(true)

        val browser = StreamBrowser(prev_button.clicks(), next_button.clicks())
        browser.currentEntry
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    picasso().load(it.url)
                            .placeholder(R.drawable.loading_icon)
                            .into(main_image)
                }

        browser.hasPrev.subscribe { prev_button.isEnabled = it }
        browser.hasNext.subscribe { next_button.isEnabled = it }
    }
}
