package com.scrawlsoft.picslider.images

import android.widget.ImageView
import java.net.URL

interface ImageDisplayAndCache {
    fun displayIntoView(url: URL, imageView: ImageView, callback: (Boolean) -> Unit)
    fun loadIntoCache(url: URL)
}
