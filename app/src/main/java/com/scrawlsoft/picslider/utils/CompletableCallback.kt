package com.scrawlsoft.picslider.utils

import com.squareup.picasso.Callback
import io.reactivex.CompletableEmitter

class CompletableCallback(private val subscriber: CompletableEmitter,
                          private val e: Throwable)
    : Callback {

    override fun onSuccess() {
        subscriber.onComplete()
    }

    override fun onError() {
        subscriber.onError(e)
    }
}