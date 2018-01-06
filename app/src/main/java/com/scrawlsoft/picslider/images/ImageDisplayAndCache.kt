package com.scrawlsoft.picslider.images

import android.net.Uri
import android.widget.ImageView
import io.reactivex.Completable

interface ImageDisplayAndCache {
    fun displayIntoView(uri: Uri, imageView: ImageView): Completable
    fun loadIntoCache(uri: Uri): Completable
}
