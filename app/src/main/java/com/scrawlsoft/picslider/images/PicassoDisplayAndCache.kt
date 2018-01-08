package com.scrawlsoft.picslider.images

import android.widget.ImageView
import com.scrawlsoft.picslider.utils.CompletableCallback
import com.squareup.picasso.Picasso
import io.reactivex.Completable
import java.net.URL

class PicassoDisplayAndCache(private val picasso: Picasso) : ImageDisplayAndCache {

    override fun displayIntoView(url: URL, imageView: ImageView): Completable =
            Completable.create { subscriber ->
                picasso.load(url.toExternalForm()).into(imageView,
                        CompletableCallback(subscriber, Exception("Picasso failed to load: $url")))
            }

    override fun loadIntoCache(url: URL): Completable =
            Completable.create { subscriber ->
                picasso.load(url.toExternalForm()).fetch(
                        CompletableCallback(subscriber, Exception("Failed to fetch: $url")))
            }
}