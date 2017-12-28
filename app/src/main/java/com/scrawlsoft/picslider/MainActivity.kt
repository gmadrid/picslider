package com.scrawlsoft.picslider

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.scrawlsoft.picslider.feedly.DEV_TOKEN
import com.scrawlsoft.picslider.feedly.DEV_USER
import com.scrawlsoft.picslider.feedly.FeedlyFetcher
import com.squareup.picasso.Picasso
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

    private var imageFeed: FeedlyImageFeed? = null // =

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val picasso = Picasso.with(this)
        picasso.setIndicatorsEnabled(true)

        val feed = FeedlyImageFeed(FeedlyFetcher(DEV_USER, DEV_TOKEN))
        feed.imageUrlS
                .subscribe {
                    picasso().load(it).into(main_image)
                }
        imageFeed = feed
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickNext(view: View) {
        imageFeed?.next()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickPrev(view: View) {
        imageFeed?.prev()
    }
}
