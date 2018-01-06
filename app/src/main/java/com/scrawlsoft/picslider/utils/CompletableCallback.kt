package com.scrawlsoft.picslider.utils

import com.squareup.picasso.Callback
import io.reactivex.CompletableEmitter

class CompletableCallback(private val subscriber: CompletableEmitter) : Callback {
    override fun onSuccess() {
        subscriber.onComplete()
    }

    override fun onError() {
        // TODO: pass this in.
        subscriber.onError(Exception())
    }
}