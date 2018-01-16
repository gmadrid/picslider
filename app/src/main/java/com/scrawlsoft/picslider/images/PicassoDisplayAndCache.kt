package com.scrawlsoft.picslider.images

import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.net.URL

class PicassoDisplayAndCache(private val picasso: Picasso) : ImageDisplayAndCache {

    override fun displayIntoView(url: URL, imageView: ImageView, callback: (Boolean) -> Unit) =
            // TODO: Add some error checking here.
            picasso.load(url.toExternalForm()).into(imageView, object : Callback {
                override fun onSuccess() = callback(true)
                override fun onError() = callback(false)
            })

    override fun loadIntoCache(url: URL) =
            // TODO: Add some error checking here.
            picasso.load(url.toExternalForm()).fetch()
}