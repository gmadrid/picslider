package com.scrawlsoft.picslider.images

import android.widget.ImageView
import com.squareup.picasso.Picasso
import java.net.URL

class PicassoDisplayAndCache(private val picasso: Picasso) : ImageDisplayAndCache {

    override fun displayIntoView(url: URL, imageView: ImageView) =
            // TODO: Add some error checking here.
            picasso.load(url.toExternalForm()).into(imageView)
    //CompletableCallback(subscriber, Exception("Picasso failed to load: $url")))

    override fun loadIntoCache(url: URL) =
            // TODO: Add some error checking here.
            picasso.load(url.toExternalForm()).fetch()
                    //  CompletableCallback(subscriber, Exception("Failed to fetch: $url")))
}