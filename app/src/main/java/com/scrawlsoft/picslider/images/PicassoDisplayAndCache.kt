package com.scrawlsoft.picslider.images

import android.net.Uri
import android.widget.ImageView
import com.scrawlsoft.picslider.utils.CompletableCallback
import com.squareup.picasso.Picasso
import io.reactivex.Completable

class PicassoDisplayAndCache(private val picasso: Picasso) : ImageDisplayAndCache {

    override fun displayIntoView(uri: Uri, imageView: ImageView): Completable {
        return Completable.create { subscriber ->
            picasso.load(uri).into(imageView, CompletableCallback(subscriber))
        }
    }

    override fun loadIntoCache(uri: Uri): Completable {
        return Completable.create { subscriber ->
            picasso.load(uri).fetch(CompletableCallback(subscriber))
        }
    }
}