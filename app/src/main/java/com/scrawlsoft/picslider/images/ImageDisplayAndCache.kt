package com.scrawlsoft.picslider.images

import android.widget.ImageView
import io.reactivex.Completable
import java.net.URL

interface ImageDisplayAndCache {
    fun displayIntoView(url: URL, imageView: ImageView)
    fun loadIntoCache(url: URL)
}
